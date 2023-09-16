package com.mrprogrammer.attendance

import android.app.ProgressDialog
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mrprogrammer.Utils.CommonFunctions.CommonFunctions
import com.mrprogrammer.Utils.Interface.CompleteHandler
import java.time.LocalDate
import java.time.LocalTime

class PostAttdance {

    companion object {
        fun post(context: Context, completeHandler: CompleteHandler){

            val result = isBefore9AMOrAfter9AM()

            if (!result) {
                completeHandler.onFailure("Session Closed....")
                return
            }
            val user = FirebaseAuth.getInstance().currentUser
            val progressDialog: ProgressDialog = ProgressDialog(context)
            progressDialog.setMessage("Posting ...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            val db = FirebaseDatabase.getInstance().getReference("data").child(CommonFunctions.firebaseClearString(user?.email).toString()).child(getKey())
            db.child("time").setValue(LocalFunctions.getTime())
            db.child("date").setValue(LocalFunctions.getDate())
            db.child("valid").setValue("true").addOnCompleteListener {
                progressDialog.dismiss()
                completeHandler.onSuccess("")
            }

        }

       private fun getKey(): String {
            val date = LocalDate.now()
            return "${date.dayOfMonth}${date.monthValue}${date.year}"
        }

        fun isBefore9AMOrAfter9AM(): Boolean {
            val currentTime = LocalTime.now()
            val time9AM = LocalTime.of(9, 0) // 9 AM

            return currentTime.isBefore(time9AM) || currentTime == time9AM
        }
    }

}