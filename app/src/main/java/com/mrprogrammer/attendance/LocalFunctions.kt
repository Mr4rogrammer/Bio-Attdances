package com.mrprogrammer.attendance

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class LocalFunctions {
    companion object {

        fun getTime(): String? {
            val date = Date()
            val formatter = SimpleDateFormat("hh-mm-ss")
            return formatter.format(date)
        }

        fun activityAnimation(context: Context, fromFront: Boolean) {
            if (context is Activity && fromFront) {
                context.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                return
            }
            (context as Activity).overridePendingTransition(
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
        }

        fun createUniqueKey(): String? {
            val date = Date()
            val uuiValue = UUID.randomUUID().toString().replace("-", "")
            val formatter = SimpleDateFormat("yyyyMMddHHmmssS")
            return formatter.format(date) + uuiValue
        }



        fun getDate(): String? {
            val date = Date()
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            return formatter.format(date)
        }



        fun isConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val info = connectivityManager.allNetworkInfo
                if (info != null) {
                    for (networkInfo in info) {
                        if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        fun logout(context: Context) {
            MaterialAlertDialogBuilder(context)
                .setMessage("Do you want to logout...")
                .setPositiveButton("Yes") { dialog, _ ->
                    (context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }



        fun openInWhatsApp(context: Context, number: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$number")
            context.startActivity(intent)
        }

        fun showConnectionDialog(context: Context,negativeButton: () -> Unit, positiveButton: () -> Unit) {
            MaterialAlertDialogBuilder(context)
                .setMessage("Please connect to internet .")
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    negativeButton()
                }
                .setPositiveButton("Re-try") { dialog, _ ->
                    dialog.dismiss()
                    positiveButton()
                }
                .show()
        }

        fun openUrlInBrowser(context: Context,url:String){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }


        fun String.convertToBoolean():Boolean{
            return this.toUpperCase() == "TRUE"
        }


        fun openWhatsAppGroup(context: Context) {
            val intentWhatsAppGroup = Intent(Intent.ACTION_VIEW)
            val uri: Uri = Uri.parse("https://chat.whatsapp.com/Ln6UtM2ZCKeKee6QxmZNmn")
            intentWhatsAppGroup.data = uri
            intentWhatsAppGroup.setPackage("com.whatsapp")
            context.startActivity(intentWhatsAppGroup)
        }

        fun convertStringIntoBoolean(text:String):Boolean{
            if(text == "true") {
                return true
            }
            return false
        }

        fun shareText(text: String, title: String, context: Context,onlyWhatsapp:Boolean = false) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, text)
            if(onlyWhatsapp) {
               intent.setPackage("com.whatsapp")
            }
            context.startActivity(Intent.createChooser(intent, title))
        }
    }
}