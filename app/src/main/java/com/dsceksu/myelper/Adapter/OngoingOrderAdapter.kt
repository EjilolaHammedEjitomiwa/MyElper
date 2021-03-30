package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.developer.kalert.KAlertDialog
import com.dsceksu.myelper.Models.*
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.activities.OngoingOrderActivity
import com.dsceksu.myelper.activities.ServiceDetailsActivity
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.MainActivity
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.kaopiz.kprogresshud.KProgressHUD
import com.mikhaellopez.circularimageview.CircularImageView
import com.podcopic.animationlib.library.AnimationType
import com.podcopic.animationlib.library.StartSmartAnimation
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.d_rate_user_design.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import java.text.SimpleDateFormat
import java.util.*

class OngoingOrderAdapter(val context: Context, val itemLists: ArrayList<OngoingOrderModel>, val isSellerView: Boolean) : RecyclerView.Adapter<OngoingOrderAdapter.ViewHolder?>() {
    private var progress: KProgressHUD? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_ongoing_order_design, parent, false)
        progress = KProgressHUD(context)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemLists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = itemLists[position]
        try {
            if (isSellerView) {
                holder.buyerDetailsText.text = "Buyer Details"
            } else {
                holder.buyerDetailsText.text = "Seller Details"
            }
            holder.title.text = product.title
            holder.price.text = product.productPrice
            holder.cancelledPrice.text = product.cancelledPrice
            holder.quantity.text = product.quantity
            holder.totalPrice.text = product.orderTotalAmount
            holder.cancelledPrice.paintFlags = holder.cancelledPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            Utils.loadImage(context, product.productImage!!, holder.image)
            loadExpectedDeliveryDays(holder.expectedDeliveryDays, product.date.toString(), product.deliveryDays!!)
            if (isSellerView) {
                loadSellerOrBuyerDetails(product.buyer!!, holder.buyerProfileImage, holder.buyerEmail, holder.buyerName, holder.buyerPhoneNumber, holder.buyerAddress, holder.city)
            } else {
                loadSellerOrBuyerDetails(product.seller!!, holder.buyerProfileImage, holder.buyerEmail, holder.buyerName, holder.buyerPhoneNumber, holder.buyerAddress, holder.city)
            }
            if (product.received!!) {
                holder.orderRecievedSwitch.setBackgroundResource(R.drawable.d_order_status_true_bg)
            } else {
                holder.orderRecievedSwitch.setBackgroundResource(R.drawable.d_order_status_false_bg)
            }
            if (product.shipped!!) {
                holder.orderShippedSwitch.setBackgroundResource(R.drawable.d_order_status_true_bg)
            } else {
                holder.orderShippedSwitch.setBackgroundResource(R.drawable.d_order_status_false_bg)
            }
            if (product.delivered!!) {
                holder.orderDeliveredSwitch.setBackgroundResource(R.drawable.d_order_status_true_bg)
                holder.confirmOrFlagContainer.visibility = View.VISIBLE
                if (isSellerView) {
                    holder.confirmText.text = "Waiting for buyer confirmation"
                    holder.flagText.visibility = View.GONE
                } else {
                    holder.confirmText.text = "Confirm delivery"
                    GuideView.Builder(context)
                            .setTitle("Order status")
                            .setContentText("This product have been delivered please confirm")
                            .setGravity(Gravity.auto)
                            .setDismissType(DismissType.anywhere)
                            .setTargetView(holder.confirmText)
                            .setContentTextSize(12)
                            .setTitleTextSize(14)
                            .build()
                            .show()
                }
            } else {
                holder.orderDeliveredSwitch.setBackgroundResource(R.drawable.d_order_status_false_bg)
                holder.confirmOrFlagContainer.visibility = View.GONE
            }
            if (!isSellerView) {
                if (product.shipped) {
                    holder.deleteIcon.visibility = View.GONE
                } else {
                    holder.deleteIcon.visibility = View.VISIBLE
                }
            }

        } catch (e: java.lang.Exception) {
        }

        holder.orderRecievedSwitch.setOnClickListener {
            if (isSellerView) {
                if (!product.received!!) {
                    KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning")
                            .setContentText("You are confirming that you have received the order, this process cannot be undone")
                            .setConfirmText("Confirm")
                            .setCancelText("Cancel")
                            .setConfirmClickListener {
                                it.dismiss()
                                if (context is OngoingOrderActivity) {
                                    context.showLoader("updating order status...")
                                }
                                recieveOrder(product.orderID!!, holder.orderRecievedSwitch)
                            }.setCancelClickListener {
                                it.dismiss()
                            }
                            .show()
                } else {
                    Toasty.info(context, "you have recieved this order thanks", Toasty.LENGTH_LONG).show()
                }

            } else {

                val message: String = if (product.received!!) {
                    "Your order has been received by the seller"
                } else {
                    "Seller has not confirm that he has received the order"
                }
                GuideView.Builder(context)
                        .setTitle("Order status")
                        .setContentText(message)
                        .setGravity(Gravity.auto)
                        .setDismissType(DismissType.anywhere)
                        .setTargetView(holder.orderRecievedSwitch)
                        .setContentTextSize(12)
                        .setTitleTextSize(14)
                        .build()
                        .show()
            }
        }

        holder.orderShippedSwitch.setOnClickListener {
            if (isSellerView) {
                if (product.received!! && !product.shipped!!) {
                    KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning")
                            .setContentText("You are confirming that you have shipped the order, this process cannot be undone")
                            .setConfirmText("Confirm")
                            .setCancelText("Cancel")
                            .setConfirmClickListener {
                                it.dismiss()
                                if (context is OngoingOrderActivity) {
                                    context.showLoader("updating order status...")
                                }
                                shippedOrder(product.orderID!!, holder.orderShippedSwitch)
                            }.setCancelClickListener {
                                it.dismiss()
                            }
                            .show()
                } else {
                    if (!product.received) {
                        Toasty.info(context, "please confirm that you have recieved the order first", Toasty.LENGTH_LONG).show()
                    }
                    if (product.shipped!!) {
                        Toasty.info(context, "you have shipped the order thanks", Toasty.LENGTH_LONG).show()
                    }
                }

            } else {
                val message: String = if (product.shipped!!) {
                    "Your order has been shipped by the seller"
                } else {
                    "Seller has not confirm that he has shipped the order"
                }
                GuideView.Builder(context)
                        .setTitle("Order status")
                        .setContentText(message)
                        .setGravity(Gravity.auto)
                        .setDismissType(DismissType.anywhere)
                        .setTargetView(holder.orderShippedSwitch)
                        .setContentTextSize(12)
                        .setTitleTextSize(14)
                        .build()
                        .show()
            }
        }
        holder.orderDeliveredSwitch.setOnClickListener {
            if (isSellerView) {
                if (!product.delivered!! && product.received!! && product.shipped!!) {
                    KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning")
                            .setContentText("You are confirming that you have delivered the order, this process cannot be undone")
                            .setConfirmText("Confirm")
                            .setCancelText("Cancel")
                            .setConfirmClickListener {
                                it.dismiss()
                                if (context is OngoingOrderActivity) {
                                    context.showLoader("updating order status...")
                                }
                                deliverOrder(product.orderID!!, holder.orderDeliveredSwitch)
                            }.setCancelClickListener {
                                it.dismiss()
                            }
                            .show()
                } else {
                    if (!product.received!!) {
                        Toasty.info(context, "please confirm that you have recieved the order first", Toasty.LENGTH_LONG).show()
                    }
                    if (!product.shipped!!) {
                        Toasty.info(context, "please confirm that you have shipped the order first", Toasty.LENGTH_LONG).show()
                    }
                    if (product.delivered) {
                        Toasty.info(context, "you have delivered this product thanks", Toasty.LENGTH_LONG).show()
                    }
                }
            } else {
                val message: String = if (product.delivered!!) {
                    "Your order has been delivered by the seller please confirm"
                } else {
                    "Seller has not confirm that he has delivered your  order"
                }
                GuideView.Builder(context)
                        .setTitle("Order status")
                        .setContentText(message)
                        .setGravity(Gravity.auto)
                        .setDismissType(DismissType.anywhere)
                        .setTargetView(holder.orderDeliveredSwitch)
                        .setContentTextSize(12)
                        .setTitleTextSize(14)
                        .build()
                        .show()
            }
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ServiceDetailsActivity::class.java)
            intent.putExtra("category", product.category)
            intent.putExtra("id", product.productID)
            intent.putExtra("seller_id", product.seller)
            context.startActivity(intent)
        }

        holder.deleteIcon.setOnClickListener {
            if (isSellerView) {
                KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                        .setTitleText("Warning")
                        .setContentText("You want to cancel the order, this process cannot be undone")
                        .setConfirmText("Confirm")
                        .setCancelText("Cancel")
                        .setConfirmClickListener {
                            it.dismiss()
                            if (context is OngoingOrderActivity) {
                                context.showLoader("updating order status...")
                            }
                            cancelOrder(product.orderID!!)
                        }.setCancelClickListener {
                            it.dismiss()
                        }
                        .show()
            } else {
                if (!product.shipped!! || !product.delivered!!) {
                    KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning")
                            .setContentText("You want to cancel the order, this process cannot be undone")
                            .setConfirmText("Confirm")
                            .setCancelText("Cancel")
                            .setConfirmClickListener {
                                it.dismiss()

                                if (context is OngoingOrderActivity) {
                                    context.showLoader("updating order status...")
                                }
                                cancelOrder(product.orderID!!)
                            }.setCancelClickListener {
                                it.dismiss()
                            }
                            .show()
                }
            }
        }

        holder.confirmText.setOnClickListener {
            if (!isSellerView) {
                KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                        .setTitleText("Warning")
                        .setContentText("You are confirming that you have received this product and you are okay with it, this process cannot be undone.")
                        .setConfirmText("Confirm")
                        .setCancelText("Cancel")
                        .setConfirmClickListener {
                            it.dismiss()
                            if (context is OngoingOrderActivity) {
                                context.showLoader("updating order status...")
                            }
                            confirmOrder(product.orderID!!, product.productImage, product.productID)
                        }.setCancelClickListener {
                            it.dismiss()
                        }
                        .show()
            } else {
                Toasty.info(context, "Buyer has not confirm this order", Toasty.LENGTH_LONG).show()
            }
        }
    }

    private fun cancelOrder(orderID: String) {
        val query = HashMap<String, Any>()
        query["orderID"] = orderID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.cancelorder("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "User not found", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneFiftyFour -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Product not exist", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFiftySeven -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "You can't cancel this order", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneFiftyFive -> {
                            Toasty.success(context, "Order cancel successfully", Toasty.LENGTH_LONG).show()
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                                context.getItemList()
                            }
                        }
                    }
                } else {
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmOrder(orderID: String, productImage: String?, productID: String?) {
        val query = HashMap<String, Any>()
        query["orderID"] = orderID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.confirmorder("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }

                            Toasty.error(context, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "User not found", Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneSixtyEight -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Either order not received,shipped or delivered", Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneFiftyFour -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Order not exist", Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneSixtyNine -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Only the buyer is allowed to confirm order", Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneFiftyThree -> {
                            Toasty.success(context, "Order confirmed successfully", Toasty.LENGTH_LONG).show()
                            var isRate = false
                            var productRate = 0.0f
                            val mDialogueView = LayoutInflater.from(context).inflate(R.layout.d_rate_user_design, null)
                            val mBuilder = androidx.appcompat.app.AlertDialog.Builder(context).setView(mDialogueView)
                            mBuilder.setCancelable(false)
                            val mAlertDualogue = mBuilder.show()
                            Utils.loadImage(context, productImage!!, mAlertDualogue.rate_user_design_profileImage)
                            StartSmartAnimation.startAnimation(mAlertDualogue.findViewById(R.id.rate_user_design_container), AnimationType.RollIn, 2000, 0, true)
                            mAlertDualogue.rate_user_design_ratingBar.setOnRatingChangeListener { _, rating, _ ->
                                isRate = true
                                productRate = rating
                            }
                            mAlertDualogue.rate_user_design_publishBtn.setOnClickListener {
                                val review = mAlertDualogue.rate_user_design_commentEditText.text.toString()
                                when {
                                    TextUtils.isEmpty(review) -> Toasty.info(context, "please provide your review", Toasty.LENGTH_LONG).show()
                                    !isRate -> Toasty.info(context, "Please provide your rating", Toast.LENGTH_LONG).show()
                                    else -> {
                                        if (context is OngoingOrderActivity) {
                                            context.showLoader("Loading...")
                                        }
                                        rateProduct(productRate, productID, review)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun rateProduct(productRate: Float, productID: String?, review: String) {
        val query = HashMap<String, Any>()
        query["productID"] = productID!!
        query["reviewMessage"] = review
        query["rating"] = productRate

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.rateproduct("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {

                        Constants.oneO3 -> {
                            Toasty.error(context, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneFiftySix -> {
                            Toasty.success(context, "Successful", Toasty.LENGTH_LONG).show()

                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                                val intent = Intent(context, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                                Animatoo.animateFade(context)
                            }
                        }
                    }
                } else {
                    Toasty.error(context, "error occur: ", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun deliverOrder(orderID: String, orderDeliveredSwitch: ImageView) {
        val query = HashMap<String, Any>()
        query["orderID"] = orderID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.deliverorder("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "user not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFiftyFour -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Order not exist", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtySeven -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "user not the seller", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFiftyTwo -> {
                            orderDeliveredSwitch.setBackgroundResource(R.drawable.d_order_status_true_bg)
                            Toasty.success(context, "Order status updated successfully", Toasty.LENGTH_LONG).show()
                            if (context is OngoingOrderActivity) {
                                context.getItemList()
                            }
                        }
                    }
                } else {
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun shippedOrder(orderID: String, orderShippedSwitch: ImageView) {
        val query = HashMap<String, Any>()
        query["orderID"] = orderID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.shippedorder("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "user not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFiftyFour -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Order not exist", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtySeven -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "user not the seller", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFiftyOne -> {
                            orderShippedSwitch.setBackgroundResource(R.drawable.d_order_status_true_bg)
                            Toasty.success(context, "Order status updated successfully", Toasty.LENGTH_LONG).show()
                            if (context is OngoingOrderActivity) {
                                context.getItemList()
                            }
                        }
                    }
                } else {
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun recieveOrder(orderID: String, orderRecievedSwitch: ImageView) {
        val query = HashMap<String, Any>()
        query["orderID"] = orderID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.recieveorder("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "user not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFiftyFour -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "Order not exist", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtySeven -> {
                            if (context is OngoingOrderActivity) {
                                context.dismissLoader()
                            }
                            Toasty.error(context, "user not the seller", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneFifty -> {
                            orderRecievedSwitch.setBackgroundResource(R.drawable.d_order_status_true_bg)
                            Toasty.success(context, "Order recieved successfully", Toasty.LENGTH_LONG).show()
                            if (context is OngoingOrderActivity) {
                                context.getItemList()
                            }
                        }
                    }
                } else {
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })

    }


    private fun loadExpectedDeliveryDays(expectedDeliveryDays: TextView, purchasedDate: String, deliveryDays: String) {
        val expectedDeliveryDate: Long = purchasedDate.toLong() + (deliveryDays.toLong() * 86400000)
        val formatedDate = SimpleDateFormat("EE, dd MM yyyy", Locale.getDefault()).format(expectedDeliveryDate)
        expectedDeliveryDays.text = "On or before $formatedDate"
    }

    private fun loadSellerOrBuyerDetails(sellerOrBuyer: String, sellerProfileImage: CircularImageView, sellerEmail: TextView, sellerName: TextView, sellerPhoneNumber: TextView, buyerAddress: TextView, city: TextView) {
        Utils.database()
                .collection(Constants.users)
                .document(sellerOrBuyer)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val sellerOrBuyerData = it.toObject(UsersModel::class.java)
                        try {
                            if (sellerOrBuyerData!!.avatar != "") {
                                Glide.with(context).load(sellerOrBuyerData.avatar).into(sellerProfileImage)
                            }
                            sellerName.text = sellerOrBuyerData.username
                            sellerEmail.text = sellerOrBuyerData.email
                            sellerPhoneNumber.text = sellerOrBuyerData.phone_number

                            //get shipping address
                            Utils.database()
                                    .collection(Constants.users)
                                    .document(sellerOrBuyer)
                                    .collection("shipping_address")
                                    .document("shipping_address")
                                    .get()
                                    .addOnSuccessListener { shippingAddress ->
                                        val data = shippingAddress.toObject(ShippingAddressModel::class.java)
                                        buyerAddress.text = data!!.address
                                        city.text = data.city
                                    }
                        } catch (e: java.lang.Exception) {
                        }

                    }
                }
    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ongoing_sales_design_image)
        val title: TextView = itemView.findViewById(R.id.ongoing_sales_design_title)
        val price: TextView = itemView.findViewById(R.id.ongoing_sales_design_price)
        val cancelledPrice: TextView = itemView.findViewById(R.id.ongoing_sales_design_cancelPrice)
        val container: CardView = itemView.findViewById(R.id.ongoing_sales_design_container)
        val orderRecievedSwitch: ImageView = itemView.findViewById(R.id.ongoing_sales_design_orderRecievedSwitch)
        val orderShippedSwitch: ImageView = itemView.findViewById(R.id.ongoing_sales_design_orderShippedSwitch)
        val orderDeliveredSwitch: ImageView = itemView.findViewById(R.id.ongoing_sales_design_orderDeliveredSwitch)
        val buyerProfileImage: CircularImageView = itemView.findViewById(R.id.ongoing_sales_design_buyerProfileImage)
        val buyerName: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerName)
        val buyerEmail: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerEmail)
        val buyerPhoneNumber: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerPhoneNumber)
        val buyerAddress: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerAddress)
        val addressContainer: LinearLayout = itemView.findViewById(R.id.ongoing_sales_design_buyerAddressLinear)
        val city: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerCity)
        val cityContainer: LinearLayout = itemView.findViewById(R.id.ongoing_sales_design_buyerCityLinear)
        val confirmText: TextView = itemView.findViewById(R.id.ongoing_sales_design_confirmText)
        val confirmOrFlagContainer: LinearLayout = itemView.findViewById(R.id.ongoing_sales_design_confirmOrFlagContainer)
        val flagText: TextView = itemView.findViewById(R.id.ongoing_sales_design_flagText)
        val buyerDetailsText: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerDetailsText)
        val expectedDeliveryDays: TextView = itemView.findViewById(R.id.ongoing_sales_design_expectedDeliveryDays)
        val deleteIcon: ImageView = itemView.findViewById(R.id.ongoing_sales_design_deleteIcon)
        val quantity: TextView = itemView.findViewById(R.id.ongoing_sales_design_qty)
        val totalPrice: TextView = itemView.findViewById(R.id.ongoing_sales_design_totalPrice)
    }
}