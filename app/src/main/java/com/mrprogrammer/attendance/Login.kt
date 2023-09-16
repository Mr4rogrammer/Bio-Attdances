package com.mrprogrammer.attendance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.mrprogrammer.Utils.CommonFunctions.CommonFunctions
import com.mrprogrammer.Utils.CommonFunctions.LocalSharedPreferences
import com.mrprogrammer.attendance.databinding.ActivityLoginBinding
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import java.util.*

class Login : AppCompatActivity() {
    lateinit var root: ActivityLoginBinding
    private var mGoogleSignClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 123
    private var mAuth: FirebaseAuth? = null
    private var reference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(root.root)
        mAuth = FirebaseAuth.getInstance()
        createRequest()
        root.login.setOnClickListener {
            root.login.setLoaderStatus(true)
            loginWithGoogle()
        }

    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            LocalFunctions.activityAnimation(this, true)
            finish()
        }
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignClient = GoogleSignIn.getClient(this, gso)
    }

    fun loginWithGoogle() {
        val sign = mGoogleSignClient?.signInIntent
        if (sign != null) {
            startActivityForResult(sign, RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account.email.toString().contains("@bitsathy.ac.in")) {
                    firebaseAuthWithGoogle(account)
                } else {
                    mGoogleSignClient?.signOut()?.addOnCompleteListener {
                        root.login.setLoaderStatus(false)
                    }
                    MaterialAlertDialogBuilder(this)
                        .setMessage("Use your collage mail id to login.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }

            } catch (e: ApiException) {
                root.login.setLoaderStatus(false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth!!.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = mAuth?.currentUser
                val clearedEmailString: String? = CommonFunctions.firebaseClearString(user!!.email)
                reference = FirebaseDatabase.getInstance().getReference("Userdata")
                if(clearedEmailString == null) return@addOnCompleteListener
                try {
                    reference?.child(clearedEmailString)?.child("Username")?.setValue(user.displayName)
                    reference?.child(clearedEmailString)?.child("Email")?.setValue(user.email)
                    reference?.child(clearedEmailString)?.child("Imageurl")?.setValue(Objects.requireNonNull(user.photoUrl).toString())
                    reference?.child(clearedEmailString)?.child("block")?.setValue("false")
                    subscribeAndChangeActivity(user)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                root.login.setLoaderStatus(false)
                ObjectHolder.MrToast().error(this, task.exception.toString())
            }
        }
    }


    private fun subscribeAndChangeActivity(user: FirebaseUser) {
        val email = CommonFunctions.firebaseClearString(user.email)
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("All")
            FirebaseMessaging.getInstance().subscribeToTopic(CommonFunctions.firebaseClearString(user.email).toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        saveUserLocallyAndChangeActivity(user)
    }

    private fun saveUserLocallyAndChangeActivity(user: FirebaseUser, changeActivity:Boolean = true) {
        LocalSharedPreferences.saveUserLocally(this, user.displayName, user.email, user.photoUrl.toString())
        root.login.setLoaderStatus(false)
       if(changeActivity) {
           startActivity(Intent(this, MainActivity::class.java))
           LocalFunctions.activityAnimation(this, true)
           finish()
       }
    }




}