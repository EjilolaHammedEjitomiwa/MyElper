package com.dsceksu.myelper.user_authentication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidstudy.networkmanager.Tovuti
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.VerifyCodeActivity
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.auth.FirebaseAuth
import com.jaredrummler.materialspinner.MaterialSpinner
import com.onurkagan.ksnack_lib.Animations.Slide
import com.onurkagan.ksnack_lib.KSnack.KSnack
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.d_verifiy_account_dialogue.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

class RegistrationActivity : AppCompatActivity() {

    private var phoneNumber: String? = null

    @SuppressLint("HardwareIds")
    var isConnectedToInternet = false
    var campus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        phoneNumber = intent.getStringExtra(Constants.number)

        if (phoneNumber != null) {
            reg_activity_phoneNumber.setText(phoneNumber)
        }

        reg_activity_createAccountBtn.setOnClickListener {
            if (isConnectedToInternet) {
                val fullname = reg_activity_fullname.text.toString()
                val username = reg_activity_username.text.toString().toLowerCase(Locale.ROOT)
                val email = reg_activity_email.text.toString().trim()
                val password = reg_activity_password.text.toString().trim()
                phoneNumber = reg_activity_phoneNumber.text.toString()
                when {
                    TextUtils.isEmpty(fullname) -> reg_activity_fullname.error = "Required"
                    TextUtils.isEmpty(username) -> reg_activity_username.error = "Required"
                    TextUtils.isEmpty(email) -> reg_activity_email.error = "Required"
                    TextUtils.isEmpty(password) -> reg_activity_password.error = "Required"
                    TextUtils.isEmpty(phoneNumber) -> reg_activity_phoneNumber.error = "Required"
                    campus == "" -> Toasty.info(this, "Select your campus", Toasty.LENGTH_LONG).show()
                    reg_activity_password.text.length < 6 -> reg_activity_password.error = "Enter 6 minimum character as password"
                    else -> {
                        Utils.showLoader(this, "loading")
                        createUserAccount(fullname, username, email, password, phoneNumber!!, campus)
                    }
                }
            }

        }

        reg_activity_signin.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
        selectCampus()
    }

    private fun selectCampus() {
        reg_activity_selectCampus.setItems("Ekiti State University", "Federal University of Oye Ekiti", "Federal Poly Ado", "Afe Babalola University", "College of Education IKERE")
        reg_activity_selectCampus.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(view: MaterialSpinner?, position: Int, id: Long, item: String?) {
                campus = item!!
            }
        })
    }


    private fun createUserAccount(fullname: String, username: String, email: String, password: String, phoneNumber: String, campus: String) {
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        saveUserInfoToDatabase(fullname, username, email, phoneNumber, campus)
                    }.addOnFailureListener {
                        Utils.dismissLoader()
                        Toasty.error(this, "error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("HardwareIds")
    private fun saveUserInfoToDatabase(fullname: String, username: String, email: String, phoneNumber: String, campus: String) {
        FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
                .addOnSuccessListener {
                    val currentUserAuthIDToken = it.token.toString()
                    val currentUserID = Utils.currentUserID()
                    FirebaseAuth.getInstance().signOut()

                    val query = HashMap<String, Any>()
                    query["fullname"] = fullname
                    query["username"] = username
                    query["email"] = email
                    query["phone_number"] = phoneNumber
                    query["campus"] = campus
                    query["device_id"] = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                    val apiService = ServiceBuilder.buildService(ApiService::class.java)
                    val requestCall = apiService.saveNewUser("Bearer $currentUserAuthIDToken", query)

                    requestCall.enqueue(object : Callback<myelperresponse> {
                        override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                            if (response.isSuccessful) {
                                val res = response.body()
                                when (res!!.code) {
                                    Constants.oneO3 -> {
                                        Utils.dismissLoader()
                                        Toasty.error(this@RegistrationActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                                    }
                                    Constants.oneSeventeen -> {
                                        Utils.dismissLoader()
                                        Toasty.error(this@RegistrationActivity, "Invalid token", Toast.LENGTH_LONG).show()
                                    }
                                    Constants.oneEighteen -> {
                                        Utils.dismissLoader()
                                        reg_activity_username.error = "Username already exist, use another"
                                    }
                                    Constants.oneTwentyTwo -> {
                                        Utils.dismissLoader()
                                        val mDialogueView = LayoutInflater.from(this@RegistrationActivity).inflate(R.layout.d_verifiy_account_dialogue, null)
                                        val mBuilder = AlertDialog.Builder(this@RegistrationActivity).setView(mDialogueView)
                                        val mAlertDualogue = mBuilder.show()
                                        mAlertDualogue.setCancelable(false)
                                        mAlertDualogue.verify_dialogue_ok.setOnClickListener {
                                            mAlertDualogue.dismiss()
                                            val intent = Intent(this@RegistrationActivity, VerifyCodeActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            intent.putExtra("userID", currentUserID)
                                            startActivity(intent)
                                            Animatoo.animateSwipeLeft(this@RegistrationActivity)
                                        }
                                    }
                                    else -> {
                                        Utils.dismissLoader()
                                        Toasty.error(this@RegistrationActivity, "Error", Toasty.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Utils.dismissLoader()
                                Toasty.error(this@RegistrationActivity, "error occur", Toasty.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                            Utils.dismissLoader()
                            Toasty.error(this@RegistrationActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
                        }
                    })

                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this, "error occur", Toast.LENGTH_LONG).show()
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
