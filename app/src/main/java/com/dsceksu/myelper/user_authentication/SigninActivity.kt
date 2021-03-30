package com.dsceksu.myelper.user_authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import com.androidstudy.networkmanager.Tovuti
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.*
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.activities.CreatePinActivity
import com.dsceksu.myelper.activities.VerifyCodeActivity
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.auth.FirebaseAuth
import com.onurkagan.ksnack_lib.Animations.Slide
import com.onurkagan.ksnack_lib.KSnack.KSnack
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_signin.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SigninActivity : AppCompatActivity() {
   var currentUserID:String? = null
    var isConnectedToInternet = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        login_activity_register.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        login_forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        login_activity_loginBtn.setOnClickListener {
            if (isConnectedToInternet){
                val email =  login_activity_email.text.toString().trim()
                val password = login_activity_password.text.toString().trim()
                when {
                    TextUtils.isEmpty(email) -> Toasty.info(this, "email cannot be empty", Toast.LENGTH_LONG).show()
                    TextUtils.isEmpty(password) -> Toasty.info(this, "password cannot be empty", Toast.LENGTH_LONG).show()
                    else -> {
                        Utils.showLoader(this,"please wait...")
                        //signin to get the ID
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnSuccessListener {
                            currentUserID =  Utils.currentUserID()
                            FirebaseAuth.getInstance().signOut()
                            signInUser(email, password)
                        }.addOnFailureListener {
                            Toasty.error(this,it.message.toString(),Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun signInUser(email: String, password: String) {
        val query = HashMap<String,Any>()
        query["device_id"] = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.logginguser("Bearer $currentUserID",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@SigninActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirty ->{
                            Utils.dismissLoader()
                            Toasty.error(this@SigninActivity,"You can't logged in on two devices at the same time, please logout from the previous one",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyEight ->{
                            Utils.dismissLoader()
                            Toasty.error(this@SigninActivity,"Please enter the OTP your received in your email", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@SigninActivity,VerifyCodeActivity::class.java)
                            intent.putExtra("userID",currentUserID)
                            startActivity(intent)
                        }
                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            Toasty.error(this@SigninActivity,"User not found",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyTwo ->{
                            try {
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Utils.dismissLoader()
                                        val pinPref = getSharedPreferences(Constants.pinPref, Context.MODE_PRIVATE)
                                        val appPin = pinPref.getString(Constants.pin, null)
                                        if(appPin == null){
                                            val intent = Intent(this@SigninActivity, CreatePinActivity::class.java)
                                            intent.putExtra(Constants.isCreatePin, true)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                            Animatoo.animateFade(this@SigninActivity)
                                        }
                                        else{
                                            val intent = Intent(this@SigninActivity, CreatePinActivity::class.java)
                                            intent.putExtra(Constants.isLoginWIthPin, true)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                            Animatoo.animateFade(this@SigninActivity)
                                        }
                                    } else {
                                        Toasty.error(this@SigninActivity, "error occur try again", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                        else ->{
                            Utils.dismissLoader()
                            Toasty.error(this@SigninActivity,"Error",Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@SigninActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@SigninActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun manageInternetConnection() {
        val kSnack = KSnack(this)
        Tovuti.from(this).monitor { connectionType, isConnected, isFast ->
            isConnectedToInternet = isConnected
            if (!isConnected) {
                kSnack.setMessage("NO INTERNET CONNECTION")
                        .setTextColor(android.R.color.white)
                        .setButtonTextColor(android.R.color.white)
                        .setBackgrounDrawable(R.drawable.snackbar_bg)
                        .setAnimation(Slide.Up.getAnimation(kSnack.getSnackView()), Slide.Down.getAnimation(kSnack.getSnackView()))
                        .show()
            } else {
                kSnack.dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        manageInternetConnection()
    }
}
