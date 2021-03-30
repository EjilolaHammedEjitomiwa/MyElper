package com.dsceksu.myelper.Notifications

import android.annotation.SuppressLint
import android.provider.Settings
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class MyFirebaseInstanceIdService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateToken(task.result!!.token)
                        }
                    }
        }
    }

    @SuppressLint("HardwareIds")
    private fun updateToken(refreshToken: String?) {
        val query = HashMap<String, Any>()
        query["token"] = refreshToken!!
        query["deviceID"] = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updatedevicetoken("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
            }
        })
    }
}