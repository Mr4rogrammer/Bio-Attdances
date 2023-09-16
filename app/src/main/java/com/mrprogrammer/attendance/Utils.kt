package com.mrprogrammer.attendance

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import java.util.*


class Utils {
    companion object {
        fun getAddress(context: Context, location:Location): Address? {
            val addresses: List<Address>?
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            addresses?.get(0)?.let { ObjectHolder.setAddress(address = it) }
           return addresses?.get(0)
        }

        fun showDialog(context: Context,message:String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

}