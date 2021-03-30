package com.dsceksu.myelper.user_authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.androidstudy.networkmanager.Tovuti
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.auth.FirebaseAuth
import com.onurkagan.ksnack_lib.Animations.Slide
import com.onurkagan.ksnack_lib.KSnack.KSnack
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {
    var isConnectedToInternet = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        forgot_password_resetBtn.setOnClickListener {
            if (isConnectedToInternet){
                val email = forgot_password_email.text.toString().trim()
                if (email.isNotEmpty()) {
                    Utils.showLoader(this, "Please wait..")
                    try {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    Utils.dismissLoader()
                                    finish()
                                    Toasty.success(this@ForgotPasswordActivity, "Successful, please check your email", Toasty.LENGTH_LONG).show()
                                }
                                .addOnFailureListener {
                                    Utils.dismissLoader()
                                    Toasty.error(this, "Error ${it.message}", Toasty.LENGTH_LONG).show()
                                }

                    } catch (e: Exception) {
                    }
                } else {
                    forgot_password_email.error = "Required"
                }
            }
        }
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
