package com.dsceksu.myelper.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_edit_shipping_address.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditShippingAddress : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_shipping_address)
        edit_address_backIcon.setOnClickListener {
            finishAndRemoveTask()
        }
        edit_address_state.isEnabled = false
        loadUserDetails()
        edit_address_saveBtn.setOnClickListener {
            val fullAddress = edit_address_address.text.toString()
            val city = edit_address_city.text.toString()
            when {
                TextUtils.isEmpty(fullAddress) -> Toasty.info(this, "full address cannot be empty", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(city) -> Toasty.info(this, "city cannot be empty", Toast.LENGTH_LONG).show()
                else -> {
                    Utils.showLoader(this,"Saving info..")
                    saveAddresInfo(fullAddress,city)
                }
            }
        }
    }

    private fun loadUserDetails() {
        Utils.database()
                .collection(Constants.users)
                .document(Utils.currentUserID())
                .collection("shipping_address")
                .document("shipping_address")
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val address = it.toObject(UsersModel::class.java)
                        edit_address_address.setText(address!!.address)
                        edit_address_city.setText(address.city)
                    }
                }
    }

    private fun saveAddresInfo(fullAddress: String, city: String) {
        val query = HashMap<String,Any>()
        query["address"] = fullAddress
        query["city"] = city
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.setshippingaddress("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@EditShippingAddress,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            Toasty.error(this@EditShippingAddress,"User not found", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneFourtySeven ->{
                            Utils.dismissLoader()
                            finishAndRemoveTask()
                            Animatoo.animateFade(this@EditShippingAddress)
                        }
                    }
                } else {
                    Toasty.error(this@EditShippingAddress, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@EditShippingAddress, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Animatoo.animateFade(this@EditShippingAddress)
    }

    override fun onResume() {
        super.onResume()
        Utils.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        Utils.setUserOffline()
    }
}
