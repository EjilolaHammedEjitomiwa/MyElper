package com.dsceksu.myelper.activities

import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.Adapter.BankVariationAdapter
import com.dsceksu.myelper.Models.SettlementAccountModel
import com.dsceksu.myelper.Models.bankLists.Banks
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils

import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_payment_information.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import studio.carbonylgroup.textfieldboxes.ExtendedEditText
import java.lang.Exception

class PaymentInformationActivity : AppCompatActivity() {
    var bankCode = ""
    var bankName = ""
    var bankListRecyclerView: RecyclerView? = null
    var bankEditText: ExtendedEditText? = null
    var accountNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_information)

        bankListRecyclerView = findViewById(R.id.payment_info_banksRecyclerview)
        bankEditText = findViewById(R.id.payment_info_bankName)

        bankListRecyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        bankListRecyclerView!!.layoutManager = layoutManager

        loadContent().execute()
        payment_info_chooseBank.setOnClickListener {
            bankListRecyclerView!!.visibility = View.VISIBLE
        }

        payment_info_saveBtn.setOnClickListener {
            accountNumber = payment_info_acctNumber.text.toString()
            when {
                bankCode == "" -> Toasty.info(this, "please choose bank", Toasty.LENGTH_LONG).show()
                TextUtils.isEmpty(accountNumber) -> payment_info_acctNumber.error = "Required"
                else -> {
                    Utils.showLoader(this, "please wait..")
                    saveBankDetails()
                }
            }
        }
    }
    inner class loadContent : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            Utils.showLoader(this@PaymentInformationActivity, "loading banks...")
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            getBankList()
            loadSettlementAccountDetails()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Utils.dismissLoader()
        }
    }
    private fun getBankList() {
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getBankList("Bearer ${Utils.currentUserID()}")
        requestCall.enqueue(object : Callback<Banks> {
            override fun onResponse(call: Call<Banks>, response: Response<Banks>) {
                if (response.isSuccessful) {
                    val itemLists = response.body()!!
                    bankListRecyclerView!!.adapter = BankVariationAdapter(this@PaymentInformationActivity, itemLists)
                    Utils.dismissLoader()
                } else {
                    // Utils.dismissLoader()
                    Toasty.error(this@PaymentInformationActivity, "Error loading", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Banks>, t: Throwable) {
                Toasty.error(this@PaymentInformationActivity, "Error loading", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun saveBankDetails() {
        val query = HashMap<String, Any>()
        query["accountNumber"] = accountNumber
        query["bankCode"] = bankCode
        query["bankName"] = bankName

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.savesettlementaccount("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){

                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentInformationActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentInformationActivity,"User not found", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneTwentyOne ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentInformationActivity,"${res.message}", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixty ->{
                            Toasty.success(this@PaymentInformationActivity,"Settlement account saved successfully",Toasty.LENGTH_LONG).show()
                            loadSettlementAccountDetails()
                        }
                        Constants.oneSixtyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentInformationActivity,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(this@PaymentInformationActivity,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@PaymentInformationActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@PaymentInformationActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun loadSettlementAccountDetails() {
        Utils.database()
                .collection(Constants.users)
                .document(Utils.currentUserID())
                .collection(Constants.settlementAccount)
                .document(Constants.settlementAccount)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        payment_info_acctNameContainer.visibility = View.VISIBLE
                        val data = it.toObject(SettlementAccountModel::class.java)
                        payment_info_bankName.setText(data!!.bankName.toString())
                        bankName = data.bankName!!
                        bankCode = data.bankCode!!
                        payment_info_acctNumber.setText(data.accountNumber!!)
                        payment_info_acctName.setText(data.accountName)
                        try {
                            Utils.dismissLoader()
                        }catch (e:Exception){}
                    }
                }
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