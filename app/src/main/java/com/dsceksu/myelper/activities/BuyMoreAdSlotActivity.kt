package com.dsceksu.myelper.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_buy_more_ad_slot.*
import kotlinx.android.synthetic.main.payforsponsoredpost_dialogue.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BuyMoreAdSlotActivity : AppCompatActivity() {
    private var totalPrice = 0.0
    private var transactionID: String? = null
    private var card: Card? = null
    private var cardNumber = ""
    private var expMonth = ""
    private var expYear = ""
    private var cvv = ""
    private var publicKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_more_ad_slot)

        PaystackSdk.initialize(applicationContext)

        var paystackCharge = (3.0 / 100) * 5000
        paystackCharge += 100
        totalPrice = 5000 + paystackCharge

        buy_slot_buyNow.setOnClickListener {
            val mDialogueView = LayoutInflater.from(this).inflate(R.layout.payforsponsoredpost_dialogue, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogueView)
            val mAlertDualogue = mBuilder.show()
            mAlertDualogue.title.text ="Total + charges = $totalPrice"
            mAlertDualogue.sponsored_product_design_image.visibility = View.GONE

            mAlertDualogue.sponsored_product_payBtn.setOnClickListener {
                cardNumber = mAlertDualogue.sponsored_product_cardNumber.text.toString()
                expMonth = mAlertDualogue.sponsored_product_expMonth.text.toString()
                expYear = mAlertDualogue.sponsored_product_expYear.text.toString()
                cvv = mAlertDualogue.sponsored_product_cvv.text.toString()

                when {
                    TextUtils.isEmpty(cardNumber) -> mAlertDualogue.sponsored_product_cardNumber.error = "Required"
                    TextUtils.isEmpty(expMonth) -> mAlertDualogue.sponsored_product_expMonth.error = "Required"
                    TextUtils.isEmpty(expYear) -> mAlertDualogue.sponsored_product_expYear.error = "Required"
                    TextUtils.isEmpty(cvv) -> mAlertDualogue.sponsored_product_cvv.error = "Required"
                    else -> {
                        mAlertDualogue.dismiss()
                        Utils.showLoader(this, "Procesing payment..")
                        startPayment()
                    }
                }
            }
        }

        buy_slot_backIcon.setOnClickListener {
            finishAndRemoveTask()
        }

    }

    private fun startPayment() {
        card = Card(cardNumber, expMonth.toInt(), expYear.toInt(), cvv)
        if (card!!.isValid) {
            val apiService = ServiceBuilder.buildService(ApiService::class.java)
            val requestCall = apiService.buyadslot("Bearer ${Utils.currentUserID()}")
            requestCall.enqueue(object : Callback<myelperresponse> {
                override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        when (res!!.code) {
                            Constants.oneO3 -> {
                                Utils.dismissLoader()
                                Toasty.error(this@BuyMoreAdSlotActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneTwentyFive -> {
                                Utils.dismissLoader()
                                Toasty.error(this@BuyMoreAdSlotActivity, "User not found", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneFourtySix -> {
                                transactionID = res.transactionID!!
                                publicKey = res.paystackPublickKey!!
                                pay()
                            }
                        }
                    } else {
                        Toasty.error(this@BuyMoreAdSlotActivity, "error occur", Toasty.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                    Toasty.error(this@BuyMoreAdSlotActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
                }
            })

        } else {
            Utils.dismissLoader()
            Toasty.error(this, "Invalid card details, please check again", Toasty.LENGTH_LONG).show()
        }
    }

    private fun pay() {
        PaystackSdk.setPublicKey(publicKey)
        val charge = Charge()
        charge.amount =  totalPrice.toInt() * 100
        charge.email = FirebaseAuth.getInstance().currentUser!!.email
        charge.reference = transactionID
        charge.currency = "NGN"
        charge.card = card

        PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction?) {
                if (transaction != null){
                    validatePayment()
                }else{
                    Utils.dismissLoader()
                    finishAndRemoveTask()
                }
            }

            override fun beforeValidate(transaction: Transaction?) {
            }

            override fun onError(error: Throwable?, transaction: Transaction?) {
                if (transaction != null){
                    validatePayment()
                }else{
                    Utils.dismissLoader()
                    finishAndRemoveTask()
                }
            }
        })
    }

    private fun validatePayment() {
        val query = HashMap<String,Any>()
        query["transactionID"] = transactionID!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.completeadslotpurchase("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@BuyMoreAdSlotActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            Toasty.error(this@BuyMoreAdSlotActivity,"User not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFourtyEight ->{
                            Utils.dismissLoader()
                            Toasty.error(this@BuyMoreAdSlotActivity,"Transaction not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyOne ->{
                            Utils.dismissLoader()
                            Toasty.error(this@BuyMoreAdSlotActivity,"Error: ${res.message}", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSeventyThree ->{
                            Utils.dismissLoader()
                            val intent = Intent(this@BuyMoreAdSlotActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            Toasty.success(this@BuyMoreAdSlotActivity,"Successful",Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@BuyMoreAdSlotActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@BuyMoreAdSlotActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
}