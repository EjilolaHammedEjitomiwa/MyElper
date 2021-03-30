package com.dsceksu.myelper.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.myelper.user_authentication.SigninActivity
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_verify_code.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyCodeActivity : AppCompatActivity() {
    var otp = ""
    var currentUserID:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        currentUserID =  intent.getStringExtra("userID")

        verify_code_backIcon.setOnClickListener {
            finish()
        }

        verify_code_confirm.setOnClickListener {
            otp =  verify_code_otp.text.toString()
            if (otp.isNotEmpty()){
                Utils.showLoader(this,"Verifying....")
                verifyOTP()
            }else{
                verify_code_otp.error = "Enter the OTP you recieved in your email"
            }
        }
        verify_code_resend.setOnClickListener {
            Utils.showLoader(this,"Please wait..")
            resendOTP()
        }
    }

    private fun verifyOTP() {
        val query = HashMap<String, Any>()
        query["otp"] = otp
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.verifyOtp("Bearer $currentUserID", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@VerifyCodeActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentySix ->{
                            Utils.dismissLoader()
                            Toasty.error(this@VerifyCodeActivity,"Invalid or expired token, please generate another one", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            reg_activity_username.error = "User not found"
                        }
                        Constants.oneTwentyTwo ->{
                            Utils.dismissLoader()
                            Toasty.success(this@VerifyCodeActivity,"Successful",Toast.LENGTH_LONG).show()
                            val intent =  Intent(this@VerifyCodeActivity, SigninActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            Animatoo.animateSwipeLeft(this@VerifyCodeActivity)
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@VerifyCodeActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@VerifyCodeActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
    private fun resendOTP() {
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.resendOTP("Bearer $currentUserID")
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 -> {
                            Utils.dismissLoader()
                            Toasty.error(this@VerifyCodeActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            reg_activity_username.error = "User not found"
                        }

                        Constants.oneTwentyTwo ->{
                            Utils.dismissLoader()
                            Toasty.success(this@VerifyCodeActivity,"Successful",Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@VerifyCodeActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@VerifyCodeActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
}