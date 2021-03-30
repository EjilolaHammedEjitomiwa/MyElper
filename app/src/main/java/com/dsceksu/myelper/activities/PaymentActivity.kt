package com.dsceksu.myelper.activities


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge

import com.dsceksu.myelper.Models.*
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_payment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PaymentActivity : AppCompatActivity() {
    private var totalPrice = 0
    private var transactionID: String = ""
    private var publicKey = ""
    private var cardNumber = ""
    private var expMonth = ""
    private var expYear =""
    private var cvv =""
    private var card:Card? = null
   // private var charge:Charge? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        PaystackSdk.initialize(applicationContext)

        totalPrice = intent.getIntExtra("total_payment",0)
        payment_totalPrice.setText(totalPrice.toString())

        var charges = ((Constants.paystackCharge / 100) * totalPrice).toInt()

        if (totalPrice >= 2000) {
            charges += 100
        }

        if (charges >= 3000) {
            charges = 3000
        }
        totalPrice+=charges

        payment_charges.setText(charges.toString())

        payment_payNowBtn.text = "PAY $totalPrice NOW"

        payment_payNowBtn.setOnClickListener {
            cardNumber =  pay_cardNumber.text.toString()
            expMonth = pay_expMonth.text.toString()
            expYear = pay_expYear.text.toString()
            cvv = pay_cvv.text.toString()

            when{
                TextUtils.isEmpty(cardNumber) -> pay_cardNumber.error =  "Card number is required"
                TextUtils.isEmpty(expMonth) -> pay_expMonth.error =  "Expiry month is required"
                TextUtils.isEmpty(expYear) -> pay_expYear.error =  "Expiry year is required"
                TextUtils.isEmpty(cvv) -> pay_cvv.error =  "Expiry year is required"
                else ->{
                    Utils.showLoader(this,"processing, don't close the app")
                    startPayment()
                }
            }
        }

    }

    private fun startPayment() {
        card = Card(cardNumber, expMonth.toInt(), expYear.toInt(), cvv)
        if (card!!.isValid) {
            val apiService = ServiceBuilder.buildService(ApiService::class.java)
            val requestCall = apiService.startproductpurchase("Bearer ${Utils.currentUserID()}")
            requestCall.enqueue(object : Callback<myelperresponse> {
                override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        when(res!!.code){
                            Constants.oneO3 ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneFourtyFive ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"Products not exist", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneFourtyFour ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"Empty carts", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneSixtyTwo ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"User banned", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneSixtyThree ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"User suspended", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneFourtyEight ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"Transaction not exist", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneTwentyOne ->{
                                Utils.dismissLoader()
                                Toasty.error(this@PaymentActivity,"Invalid transaction", Toast.LENGTH_LONG).show()
                            }
                            Constants.oneFourtySix ->{
                                transactionID =  res.transactionID!!
                                publicKey = res.paystackPublickKey!!
                                pay()
                            }
                        }
                    } else {
                        Toasty.error(this@PaymentActivity, "error occur", Toasty.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                    Toasty.error(this@PaymentActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
                }
            })

        }
        else {
            Utils.dismissLoader()
            Toasty.error(this,"Invalid card details, please check again",Toasty.LENGTH_LONG).show()
        }
    }

    private fun pay() {
        PaystackSdk.setPublicKey(publicKey)
        val charge = Charge()
        charge.amount =  totalPrice * 100
        charge.email = FirebaseAuth.getInstance().currentUser!!.email
        charge.reference = transactionID
        charge.currency = "NGN"
        charge.card = card

        PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction?) {
                if (transaction != null){
                  completeOrder()
                }else{
                    Utils.dismissLoader()
                    finishAndRemoveTask()
                }
            }

            override fun beforeValidate(transaction: Transaction?) {
            }

            override fun onError(error: Throwable?, transaction: Transaction?) {
                if (transaction != null){
                    completeOrder()
                }else{
                    Utils.dismissLoader()
                    finishAndRemoveTask()
                }
            }
        })
    }

    private fun completeOrder() {
        val query = HashMap<String,Any>()
        query["transactionID"] = transactionID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.completeorder("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Toasty.error(this@PaymentActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Toasty.error(this@PaymentActivity,"User not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"User banned", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"User suspended", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFourtyEight ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"Transaction not exist", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyOne ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"Invalid transaction", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"Amount recieved not valid with product purchased", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneTwentyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"Error: ${res.message}", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtySix ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentActivity,"Empty product list", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwenty ->{
                           Utils.dismissLoader()
                            val intent = Intent(this@PaymentActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            Toasty.success(this@PaymentActivity,"Order placed successfuly",Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@PaymentActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@PaymentActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
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
