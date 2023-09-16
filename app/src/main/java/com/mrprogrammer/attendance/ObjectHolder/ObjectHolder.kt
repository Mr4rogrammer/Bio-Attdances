package com.mrprogrammer.mrshop.ObjectHolder

import android.location.Address
import android.provider.Telephony.Mms.Addr
import com.mrprogrammer.Utils.Toast.MrToast

object ObjectHolder {
    private  var mrToast: MrToast? = null
    private  var imageUrl: String? = null
    private  var address: Address? = null

    fun setAddress(address: Address) {
        this.address = address
    }

    fun setImageUrl(image: String) {
        this.imageUrl = imageUrl
    }

    fun getImageUrl(): String? {
        return  this.imageUrl
    }

    fun getAddress(): Address? {
        return  this.address
    }




    fun MrToast():MrToast{
        if(mrToast == null){
            mrToast = com.mrprogrammer.Utils.Toast.MrToast()
        }
        return com.mrprogrammer.Utils.Toast.MrToast()
    }

}