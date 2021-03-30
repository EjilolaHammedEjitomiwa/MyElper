package com.dsceksu.myelper.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.androidstudy.networkmanager.Tovuti
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.onurkagan.ksnack_lib.Animations.Slide
import com.onurkagan.ksnack_lib.KSnack.KSnack
import com.podcopic.animationlib.library.AnimationType
import com.podcopic.animationlib.library.StartSmartAnimation

class SplashScreenActivity : AppCompatActivity() {
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 500
    private var isConnectedToInternet = false
    private val mRunnable: Runnable = Runnable {

        if (!isFinishing) {
            checkUserAccountStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)

        StartSmartAnimation.startAnimation(findViewById(R.id.splash_screen_omes), AnimationType.FadeIn, 3000, 0, true)
    }

    private fun checkUserAccountStatus() {
        if (isConnectedToInternet){
            if (Utils.currentUser() != null) {
                Utils.database()
                        .collection(Constants.users)
                        .document(Utils.currentUserID())
                        .get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                val user = it.toObject(UsersModel::class.java)
                                if (user!!.banned!!) {
                                    val intent = Intent(this, AccountBannedActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    Animatoo.animateFade(this)
                                }else{
                                    val intent = Intent(applicationContext, GetStartedActivity::class.java)
                                    startActivity(intent)
                                    Animatoo.animateFade(this)
                                    finish()
                                }
                            }
                        }
            }
            else {
                val intent =  Intent(this@SplashScreenActivity, GetStartedActivity::class.java)
                startActivity(intent)
                Animatoo.animateFade(this)
                finish()
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


    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }


}
