package com.mrprogrammer.attendance;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mrprogrammer.Utils.CommonFunctions.LocalSharedPreferences;
import com.mrprogrammer.Utils.Interface.CompleteHandler;
import com.mrprogrammer.Utils.Widgets.ProgressButton;
import com.mrprogrammer.attendance.databinding.ActivityMainBinding;
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    SupportMapFragment supportMapFragment;
    private GoogleMap googleMapView = null;
    FusedLocationProviderClient mFusedLocationClient;
    Location location = null;
    List<Marker> markers = new ArrayList<>();

    protected LatLng start = null;
    protected LatLng end = null;

    int PERMISSION_ID = 44;

    ProgressButton button;

    CircleOptions circleOptions = null;

    private Circle mCircle;

    boolean isFirst = true;

    ImageView mylocation, collage;

    ActivityMainBinding root;
    private CancellationSignal cancellationSignal = null;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(root.getRoot());
        try {
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
            assert supportMapFragment != null;
            supportMapFragment.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        button = findViewById(R.id.button);
        mylocation = findViewById(R.id.mylocation);
        collage = findViewById(R.id.collage);

        mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check()){
                    showMyLocation(new LatLng(location.getLatitude(), location.getLongitude()));

                }
            }
        });

        collage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(Check()) {
                   LatLng l = new LatLng(11.496213, 77.276925);
                   showMyLocation(l);
               }
            }
        });


        root.appBar.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalFunctions.Companion.logout(MainActivity.this);
            }
        });

        List<String> user = LocalSharedPreferences.Companion.getLocalSavedUser(this);
        Glide.with(this).load(user.get(2)).into(root.appBar.profile);
        ObjectHolder.INSTANCE.setImageUrl(user.get(2));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check()) {
                    HashMap<String, String> map =  checkIamInside(location);
                    String status = map.get("status");
                    String toast = map.get("toast");
                    if(Objects.equals(status, "0")) {
                        ObjectHolder.INSTANCE.MrToast().warning(MainActivity.this,toast,Toast.LENGTH_LONG);
                    }else {
                        markMyAttdance();
                    }
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(
                        int errorCode, CharSequence errString)
                {
                    super.onAuthenticationError(errorCode, errString);
                    notifyUser("Authentication Error : " + errString);
                }
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result)
                {
                    super.onAuthenticationSucceeded(result);
                    post();
                }
            };
        }
    }

    private void post() {
        PostAttdance.Companion.post(MainActivity.this, new CompleteHandler() {
            @Override
            public void onSuccess(@NonNull Object o) {
                ObjectHolder.INSTANCE.MrToast().success(MainActivity.this,"Success",Toast.LENGTH_LONG);
            }

            @Override
            public void onFailure(@NonNull String s) {
                notifyUser(s);
            }
        });
    }

    private void notifyUser(String message)
    {
        Utils.Companion.showDialog(MainActivity.this,message);

    }
    private CancellationSignal getCancellationSignal()
    {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(
                new CancellationSignal.OnCancelListener() {
                    @Override public void onCancel()
                    {
                        notifyUser("Authentication was Cancelled by the user");
                    }
                });
        return cancellationSignal;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private Boolean checkBiometricSupport()
    {
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        }
        else
            return true;
    }


    private void markMyAttdance() {
        BiometricPrompt biometricPrompt = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt = new BiometricPrompt
                    .Builder(getApplicationContext())
                    .setTitle("Title of Prompt")
                    .setSubtitle("Subtitle")
                    .setDescription("Uses FP")
                    .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                        @Override
                        public void
                        onClick(DialogInterface dialogInterface, int i)
                        {
                            notifyUser("Authentication Cancelled");
                        }
                    }).build();

            biometricPrompt.authenticate(
                    getCancellationSignal(),
                    getMainExecutor(),
                    authenticationCallback);
        }
    }


    private boolean Check() {
        if(location == null) {
            ObjectHolder.INSTANCE.MrToast().warning(MainActivity.this,"Please wait while processing....",Toast.LENGTH_LONG);
            return  false;
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                               @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private void getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location == null) {
                    requestNewLocationData();
                } else {
                    try {
                        requestNewLocationData();
                    } catch (Exception e) {
                        locationData(location);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            locationData(mLastLocation);
        }
    };

    private void locationData(Location locations) {
        location = locations;
        Address address = Utils.Companion.getAddress(this,locations);
        root.appBar.lcoation.setText(address.getLocality());
        updateLocationForUser();
    }

    private void updateLocationForUser() {
        googleMapView.clear();
        LatLng locations = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(locations)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        googleMapView.addMarker(markerOptions);
        collagePoint();
        if(isFirst) {
            showMyLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            isFirst = false;
        }
    }


    void showMyLocation(LatLng location) {
        googleMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    private void updateLocationForLocation(LatLng latLng) {
        LatLng locations = new LatLng(latLng.latitude, latLng.longitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(locations)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        googleMapView.addMarker(markerOptions);
    }

    private void drawCircle(LatLng point){
        circleOptions = new CircleOptions();
        circleOptions.center(point);
        circleOptions.radius(120);
        circleOptions.strokeColor(0xffff0000);
        circleOptions.fillColor(0x44ff0000);
        circleOptions.strokeWidth(8);
        googleMapView.addCircle(circleOptions);
        mCircle = googleMapView.addCircle(circleOptions);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapView = googleMap;
     //   googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        getLastLocation();
    }

    private void collagePoint() {
        drawCircle(new LatLng(11.496213, 77.276925));
        updateLocationForLocation(new LatLng(11.496213, 77.276925));
    }
    private HashMap<String, String> checkIamInside(Location location) {
        HashMap<String, String> map = new HashMap<>();
        map.put("status", "0");
        map.put("toast", "Please wait wait Processing");

        float[] distance = new float[2];
        if(mCircle == null || location == null ){
            return map;
        }

        Location.distanceBetween( location.getLatitude(), location.getLongitude(), mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);

        if( distance[0] > mCircle.getRadius() ){
            map.put("status", "0");
            map.put("toast", "Outside, from Access Point.");
        } else {
            map.put("status", "1");
            map.put("toast", "Inside, Access Point.");
        }
        return map;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFirst = true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
