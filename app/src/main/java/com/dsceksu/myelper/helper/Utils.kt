package com.dsceksu.myelper.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object Utils {
    var loader: AlertDialog? = null

    fun currentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun setRecentlyViewedCategory(context: Context, category: String) {
        context.getSharedPreferences("RecentlyViewCategoryPref", MODE_PRIVATE).edit {
            putString("category", category)
            apply()
        }
    }

    fun getRecentlyViewedCategory(context: Context): String {
        val category = context.getSharedPreferences("RecentlyViewCategoryPref", MODE_PRIVATE).getString("category", "")
        return category!!
    }

    fun databaseRef(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    fun database(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun formatTime(timeInMilliseconds: Long): String {
        val formatedDate = TimeAgo.using(timeInMilliseconds)
        return formatedDate
    }

    fun copyValue(context: Activity, value: String) {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("", value)
        clipboard.setPrimaryClip(clip)
        Toasty.info(context, "copied", Toast.LENGTH_LONG, true).show()
    }

    fun setUserOnline() {
        val query = HashMap<String, Any>()
        query["status"] = "online"
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updateuserstatus("Bearer ${currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
            }
        })
    }

    fun setUserOffline() {
        val query = HashMap<String, Any>()
        query["status"] = System.currentTimeMillis().toString()
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updateuserstatus("Bearer ${currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
            }
        })
    }

    fun loadImage(context: Context, src: Any, view: ImageView) {
        try {
            Glide.with(context).load(src).into(view)
        } catch (e: IllegalArgumentException) {
        }

    }

    fun setDeductPercentage(oldPrice: String, newPrice: String, view: TextView) {
        var percent = 0.0
        try {
            percent = (100 * (oldPrice.toDouble() - newPrice.toDouble())) / newPrice.toDouble()
        } catch (e: NumberFormatException) {
        }
        try {
            view.text = "- ${String.format("%.1f", percent)}%"
        } catch (e: java.lang.Exception) {
        }
    }

    fun sendEmail(title: String, message: String, to: String) {
//        GMailSender.withAccount("geoapps01@gmail.com", "60051588DI")
//            .withTitle(title)
//            .withBody(message)
//            .withSender("OMES")
//            .toEmailAddress(to)
//            .send()
    }

    fun showLoader(context: Context, title: String) {
        loader = SpotsDialog.Builder()
                .setContext(context)
                .setMessage(title)
                .setCancelable(false)
                .build()
                .apply {
                }
        if (loader!!.isShowing) {
            try {
                loader!!.dismiss()
            }catch (e:Exception){}

        }else{
            try {
                loader!!.show()
            }catch (e:Exception){}
        }

    }

    fun dismissLoader() {
        if (loader!!.isShowing) {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    try {
                        loader!!.cancel()
                    } catch (e: java.lang.Exception) {
                    }
                }
            }, 3000)
        }

    }


    fun userInChatActivity(context: Context) {
        val pinPref = context.getSharedPreferences("in-chat-pref", Context.MODE_PRIVATE)
        pinPref.edit().apply {
            putBoolean("in-chat", true)
            apply()
        }
    }

    fun userLeftChatActivity(context: Context) {
        val pinPref = context.getSharedPreferences("in-chat-pref", Context.MODE_PRIVATE)
        pinPref.edit().apply {
            putBoolean("in-chat", false)
            apply()
        }
    }

    fun isUserInChatActivity(context: Context): Boolean {
        val pref = context.getSharedPreferences("in-chat-pref", Context.MODE_PRIVATE)
        return pref.getBoolean("in-chat", false)
    }
}