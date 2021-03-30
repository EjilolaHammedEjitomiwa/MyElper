package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.ProductPurchasedAdapter
import com.dsceksu.myelper.Models.ProductPurchasedModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import kotlinx.android.synthetic.main.activity_transaction_details.*

class TransactionDetailsActivity : AppCompatActivity() {
    private var TAG = "TransactionDetailsActivity"
    var transactionTitle: String? = null
    var transactionID: String? = null
    var transactionDate: String? = null
    var transactionAmount: String? = null
    var transactionStatus: String? = null
    var transactionMessage: String? = null
    var transactionPaymentRef: String? = null

    var productTitle: String? = null
    var productPrice: String? = null
    var productQuantity: String? = null
    var productDeliveryCharge: String? = null
    var productImage: String? = null
    var amountSettled:String? = null

    var sponsoredWeeks: String? = null
    var postTitle: String? = null
    var postContent: String? = null
    var postImage: String? = null
    var contactNumber: String? = null
    var contactEmail: String? = null
    var contactWebsite: String? = null

    var slot: String? = null

    private var productPurchased: List<String>? = null
    private var productPurchasedList = ArrayList<ProductPurchasedModel>()
    private var productPurchasedAdapter: ProductPurchasedAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        productPurchasedAdapter = ProductPurchasedAdapter(this, productPurchasedList)

        transaction_history_allProductPurchasedRecyclerview.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        transaction_history_allProductPurchasedRecyclerview.layoutManager = layoutManager
        transaction_history_allProductPurchasedRecyclerview.adapter = productPurchasedAdapter

        transactionTitle = intent.getStringExtra(Constants.transactionsTitle)
        transactionID = intent.getStringExtra(Constants.transactionID)
        transactionDate = intent.getStringExtra(Constants.transactionDate)
        transactionAmount = intent.getStringExtra(Constants.transactionAmount)
        transactionStatus = intent.getStringExtra(Constants.transactionStatus)
        transactionMessage = intent.getStringExtra(Constants.transactionMessage)
        transactionPaymentRef = intent.getStringExtra(Constants.transactionPaymentRef)

        productTitle = intent.getStringExtra(Constants.productTitle)
        productPrice = intent.getStringExtra(Constants.productPrice)
        productQuantity = intent.getStringExtra(Constants.productQuantity)
        productDeliveryCharge = intent.getStringExtra(Constants.productDeliveryCharge)
        productImage = intent.getStringExtra(Constants.productImage)
        amountSettled = intent.getStringExtra(Constants.amountSettled)

        sponsoredWeeks = intent.getStringExtra(Constants.sponsoredWeeks)

        postTitle = intent.getStringExtra(Constants.postTitle)
        postContent = intent.getStringExtra(Constants.postContent)
        postImage = intent.getStringExtra(Constants.postImage)
        contactNumber = intent.getStringExtra(Constants.contactNumber)
        contactEmail = intent.getStringExtra(Constants.contactEmail)
        contactWebsite = intent.getStringExtra(Constants.contactWebsite)

        slot = intent.getStringExtra(Constants.slot)

        productPurchased = intent.getStringArrayListExtra(Constants.productPurchase)

        //setting value

        transaction_history_transactioTitle.text = transactionTitle
        transaction_history_transactioID.text = transactionID
        transaction_history_date.text = transactionDate
        transaction_history_amount.text = transactionAmount
        transaction_history_status.text = transactionStatus
        transaction_history_transactionMessage.text = transactionMessage
        transaction_history_paymentRef.text = transactionPaymentRef

        when (transactionTitle) {

            Constants.orderDeliveredPayment, Constants.orderCancelRefund -> {
                transaction_history_productTitleContainer.visibility = View.VISIBLE
                transaction_history_productPriceContainer.visibility = View.VISIBLE
                transaction_history_productQtyContainer.visibility = View.VISIBLE
                transaction_history_productDeliveryChargeContainer.visibility = View.VISIBLE
                transaction_history_productImageContainer.visibility = View.VISIBLE
                transaction_history_amountSettledContainer.visibility = View.VISIBLE

                transaction_history_productTitle.text = productTitle
                transaction_history_productPrice.text = productPrice
                transaction_history_productQty.text = productQuantity
                transaction_history_productDeliveryCharge.text = productDeliveryCharge
                transaction_history_amountSettled.text = amountSettled

                if (productImage != null && productImage != "") {
                    Utils.loadImage(this, productImage!!, transaction_history_productImage)
                }
            }

            Constants.sponsoredProductPayment -> {
                transaction_history_sponsoredWeekContainer.visibility = View.VISIBLE
                transaction_history_sponsoredWeek.text = "${sponsoredWeeks} weeks"
            }

            Constants.sponsoredPostPayment -> {
                transaction_history_sponsoredWeekContainer.visibility = View.VISIBLE
                transaction_history_postTitleContainer.visibility = View.VISIBLE
                transaction_history_postContentContainer.visibility = View.VISIBLE
                transaction_history_postImageContainer.visibility = View.VISIBLE
                transaction_history_contactNumberContainer.visibility = View.VISIBLE
                transaction_history_contactEmailContainer.visibility = View.VISIBLE
                transaction_history_contactWebsiteContainer.visibility = View.VISIBLE

                transaction_history_sponsoredWeek.text = "${sponsoredWeeks} weeks"
                transaction_history_postTitle.text = postTitle
                transaction_history_postContent.text = postContent

                if (postImage != "" && postImage != null) {
                    Utils.loadImage(this, postImage!!, transaction_history_postImage)
                }
                transaction_history_contactNumber.text = contactNumber
                transaction_history_contactEmail.text = contactEmail
                transaction_history_contactWebsite.text = contactWebsite
            }
            Constants.productPurchase -> {
                transaction_history_productOrderedContainer.visibility = View.VISIBLE
                getProductPurchased()
            }
            Constants.AdSlotPurchase ->{
                transaction_history_slotContainer.visibility = View.VISIBLE
                transaction_history_slot.text = slot

            }
        }

        if (transactionStatus == Constants.successful) {
            transaction_history_status.setTextColor(resources.getColor(R.color.green))
            transaction_history_transactionMessage.setTextColor(resources.getColor(R.color.green))
        } else {
            transaction_history_status.setTextColor(resources.getColor(R.color.alizarin_crimson))
            transaction_history_transactionMessage.setTextColor(resources.getColor(R.color.alizarin_crimson))
        }

        transaction_history_copyTransactioID.setOnClickListener {
            Utils.copyValue(this,transaction_history_transactioID.text.toString())
        }

    }

    private fun getProductPurchased() {
        productPurchased!!.forEach {
            if (it != ""){
                val productDetails = it.split("|")
                val productID = productDetails[0]
                val quantityPurchased =  productDetails[1]
                val price =  productDetails[2]
                val deliveryCharge =  productDetails[3]
                val seller =  productDetails[4]
                val image =  productDetails[5]
                val title =  productDetails[6]

                productPurchasedList.add(ProductPurchasedModel(image,price,quantityPurchased,deliveryCharge))
                Log.d(TAG, "IMAGE: $image")
            }
        }
        productPurchasedAdapter!!.notifyDataSetChanged()
    }

}