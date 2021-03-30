package com.dsceksu.myelper.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.Adapter.GetStartedAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.GetStartedImageModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.myelper.user_authentication.RegistrationActivity
import com.dsceksu.myelper.user_authentication.SigninActivity
import kotlinx.android.synthetic.main.activity_get_started.*

class GetStartedActivity : AppCompatActivity(){
    private var itemList = ArrayList<GetStartedImageModel>()
    private var adapter: GetStartedAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        adapter = GetStartedAdapter(this, itemList)
        get_started_slider.setSliderAdapter(adapter!!)

        get_started_design_signup.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        get_started_design_login.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    private fun checkUser() {
        if (Utils.currentUser() != null) {
            val pinPref = getSharedPreferences(Constants.pinPref, Context.MODE_PRIVATE)
            val appPin = pinPref.getString(Constants.pin, null)
            if(appPin == null){
                val intent = Intent(this, CreatePinActivity::class.java)
                intent.putExtra(Constants.isCreatePin, true)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Animatoo.animateFade(this)
            }
            else{
                val intent = Intent(this, CreatePinActivity::class.java)
                intent.putExtra(Constants.isLoginWIthPin, true)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Animatoo.animateFade(this)
            }
        }
    }

    //TODO load image offline not online

//    private fun getSliderImages() {
//        try {
//            Utils.databaseRef().child(Constants.getStartedImages).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(p0: DataSnapshot) {
//                    if (p0.exists()) {
//                        itemList.clear()
//                        for (snapshot in p0.children) {
//                            val item = snapshot.getValue(GetStartedImageModel::class.java)
//                            itemList.add(item!!)
//                        }
//
//                        adapter!!.notifyDataSetChanged()
//                        try {
//                            get_started_slider.setIndicatorAnimation(IndicatorAnimations.WORM)
//                        } catch (e: NullPointerException) { }
//                        try {
//                            get_started_slider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
//                        } catch (e: NullPointerException) { }
//                        try {
//                            get_started_slider.startAutoCycle()
//                        } catch (e: NullPointerException) { }
//                    }
//                }
//                override fun onCancelled(p0: DatabaseError) {
//                }
//            })
//        } catch (e: Exception) {
//        }
//    }

}
