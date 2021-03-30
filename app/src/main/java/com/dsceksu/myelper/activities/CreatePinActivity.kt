package com.dsceksu.myelper.activities

import android.content.Context
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_pin.*

@Suppress("DEPRECATION")
class CreatePinActivity : AppCompatActivity() {
    var count = 0
    var pin = ""
    var isCreatPin: Boolean? = null
    var isLoginWithPin: Boolean? = null
    var isUpdatePin: Boolean? = null
    var isSetNewPin: Boolean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pin)
        isCreatPin = intent.getBooleanExtra(Constants.isCreatePin, false)
        isLoginWithPin = intent.getBooleanExtra(Constants.isLoginWIthPin, false)
        isUpdatePin = intent.getBooleanExtra(Constants.isUpdatePin, false)
        isSetNewPin = intent.getBooleanExtra(Constants.isSetNewPin, false)

        if (isLoginWithPin!!) {
            create_pin_title.text = "Enter your pin"
        }
        if (isUpdatePin!!) {
            create_pin_title.text = "Enter old pin"
        }
        if (isSetNewPin!!) {
            create_pin_title.text = "Enter new pin"
        }


        btn_clear.setOnClickListener {
            count = 0
            pin = ""
            enableBtn()
        }
        btn_go.setOnClickListener {
            if (isCreatPin!!) {
                if (count == 4) {
                    Utils.showLoader(this,"creating your pin")
                    createPin()
                } else {
                    Toasty.info(this, "You need to set 4 characters as pin", Toast.LENGTH_SHORT).show()
                }
            }
            if (isLoginWithPin!!) {
                if (count == 4) {
                    Utils.showLoader(this,"loading")
                    loginWithPin()
                } else {
                    Toasty.info(this, "Enter 4 characters pin", Toast.LENGTH_SHORT).show()
                }
            }
            if (isUpdatePin!!) {
                if (count == 4) {
                    Utils.showLoader(this,"loading")
                    checkOldPin("%")
                } else {
                    Toasty.info(this, "Enter 4 characters pin", Toast.LENGTH_SHORT).show()
                }

            }
            if (isSetNewPin!!) {
                if (count == 4) {
                    Utils.showLoader(this,"loading")
                    setNewPin()
                } else {
                    Toasty.info(this, "Enter 4 characters pin", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setNewPin() {
        val pinPref = getSharedPreferences(Constants.pinPref, Context.MODE_PRIVATE)
        pinPref.edit().apply {
            putString(Constants.pin, pin)
            putBoolean(Constants.lockAppWithPin, true)
            apply()
            Handler().postDelayed(object : Runnable {
                override fun run() {
                   Utils.dismissLoader()
                    val intent = Intent(this@CreatePinActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    Toasty.success(this@CreatePinActivity, "Pin changed successfully", Toast.LENGTH_SHORT).show()
                }
            }, 2000)
        }
    }

    private fun checkOldPin(s: String) {
        val pinPref = getSharedPreferences(Constants.pinPref, Context.MODE_PRIVATE)
        val oldPin = pinPref.getString(Constants.pin, null)
        if (oldPin == pin) {
          Utils.dismissLoader()
            val i = Intent(this, CreatePinActivity::class.java)
            i.putExtra(Constants.isSetNewPin, true)
            startActivity(i)
            finish()
        } else {
            Toasty.error(this, "Incorrect pin", Toast.LENGTH_SHORT).show()
           Utils.dismissLoader()
        }
    }

    private fun loginWithPin() {
        val pinPref = getSharedPreferences(Constants.pinPref, Context.MODE_PRIVATE)
        val oldPin = pinPref.getString(Constants.pin, null)
        if (oldPin == pin) {
           Utils.dismissLoader()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Animatoo.animateFade(this)
        } else {
            Toasty.error(this, "Incorrect pin", Toast.LENGTH_SHORT).show()
            Utils.dismissLoader()
        }
    }

    private fun createPin() {
        val pinPref = getSharedPreferences(Constants.pinPref, Context.MODE_PRIVATE)
        pinPref.edit().apply {
            putString(Constants.pin, pin)
            putBoolean(Constants.lockAppWithPin, true)
            apply()
            Handler().postDelayed(object : Runnable {
                override fun run() {
               Utils.dismissLoader()
                    val intent = Intent(this@CreatePinActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    Animatoo.animateFade(this@CreatePinActivity)
                    Toasty.success(this@CreatePinActivity, "Pin created successfully", Toast.LENGTH_SHORT).show()
                }
            }, 2000)
        }
    }

    private fun enableBtn() {
        val allBtns = listOf<View>(btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9)
        for (i in allBtns) {
            i.isEnabled = true
            i.alpha = 1f
        }
        //hide all dots
        val allDots = listOf<View>(dot_1, dot_2, dot_3, dot_4)
        for (i in allDots) {
            i.visibility = View.INVISIBLE
        }
    }

    fun btnClick(view: View) {
        val btnClicked = view as TextView
        when (btnClicked) {
            btn_1 -> pin = pin.plus(btn_1.text.toString())
            btn_2 -> pin = pin.plus(btn_2.text.toString())
            btn_3 -> pin = pin.plus(btn_3.text.toString())
            btn_4 -> pin = pin.plus(btn_4.text.toString())
            btn_5 -> pin = pin.plus(btn_5.text.toString())
            btn_6 -> pin = pin.plus(btn_6.text.toString())
            btn_7 -> pin = pin.plus(btn_7.text.toString())
            btn_8 -> pin = pin.plus(btn_8.text.toString())
            btn_9 -> pin = pin.plus(btn_9.text.toString())
            btn_0 -> pin = pin.plus(btn_0.text.toString())
        }
        inCrementCount()
        vibrate()
    }

    fun vibrate(){
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }else{
            vibrator.vibrate(50)
        }
    }

    private fun inCrementCount() {
        count++
        when (count) {
            1 -> dot_1.visibility = View.VISIBLE
            2 -> dot_2.visibility = View.VISIBLE
            3 -> dot_3.visibility = View.VISIBLE
            4 -> {
                //disable all buttons
                dot_4.visibility = View.VISIBLE
                val allBtns = listOf<View>(btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9)
                for (i in allBtns) {
                    i.isEnabled = false
                    i.alpha = 0.2f
                }
            }
        }
    }


    override fun onBackPressed() {
       finishAndRemoveTask()
        Animatoo.animateSwipeRight(this)

    }
}