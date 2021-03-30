package com.dsceksu.myelper.Adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.OngoingOrderModel
import com.dsceksu.myelper.Models.ShippingAddressModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.mikhaellopez.circularimageview.CircularImageView

class CompletedOrderAdapter(val context: Context, val item: ArrayList<OngoingOrderModel>, val isCompletedOrders: Boolean) : RecyclerView.Adapter<CompletedOrderAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_ongoing_order_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = item[position]

        try {
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
            } else {
                holder.orderDeliveredSwitch.setBackgroundResource(R.drawable.d_order_status_false_bg)
            }
            Glide.with(context).load(product.productImage!!).into(holder.image)
            holder.title.text = product.title
            holder.price.text = product.productPrice
            holder.totalPrice.text = product.orderTotalAmount
            holder.quantity.text = product.quantity
            holder.ongoingText.text = "order ${product.status}"
            holder.cancelledPrice.text = product.cancelledPrice
            holder.cancelledPrice.paintFlags = holder.cancelledPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            if (product.status == Constants.delivered) {
                holder.ongoingText.setTextColor(context.resources.getColor(R.color.green))
            } else {
                holder.ongoingText.setTextColor(context.resources.getColor(R.color.colorPrimary))
            }
            if (isCompletedOrders) {
                holder.buyerDetailsText.text = "Seller Details"
                loadSellerOrBuyerDetails(product.seller!!, holder.buyerProfileImage, holder.buyerEmail, holder.buyerName, holder.buyerPhoneNumber, holder.buyerAddress, holder.city)
            } else {
                holder.buyerDetailsText.text = "Buyer Details"
                loadSellerOrBuyerDetails(product.buyer!!, holder.buyerProfileImage, holder.buyerEmail, holder.buyerName, holder.buyerPhoneNumber, holder.buyerAddress, holder.city)
            }
        } catch (e: Exception) {
        }
        holder.deliveryDaysContainer.visibility = View.GONE
        holder.confirmText.visibility = View.GONE
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
                                    .addOnSuccessListener {shippingAddress ->
                                        val data =  shippingAddress.toObject(ShippingAddressModel::class.java)
                                        buyerAddress.text =data!!.address
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
        val orderRecievedSwitch: ImageView = itemView.findViewById(R.id.ongoing_sales_design_orderRecievedSwitch)
        val orderShippedSwitch: ImageView = itemView.findViewById(R.id.ongoing_sales_design_orderShippedSwitch)
        val orderDeliveredSwitch: ImageView = itemView.findViewById(R.id.ongoing_sales_design_orderDeliveredSwitch)
        val buyerProfileImage: CircularImageView = itemView.findViewById(R.id.ongoing_sales_design_buyerProfileImage)
        val buyerName: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerName)
        val buyerEmail: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerEmail)
        val buyerPhoneNumber: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerPhoneNumber)
        val totalPrice: TextView = itemView.findViewById(R.id.ongoing_sales_design_totalPrice)
        val quantity: TextView = itemView.findViewById(R.id.ongoing_sales_design_qty)
        val buyerAddress: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerAddress)
        val buyerAddressLinear: LinearLayout = itemView.findViewById(R.id.ongoing_sales_design_buyerAddressLinear)
        val buyerDetailsText: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerDetailsText)
        val expectedDeliveryDays: TextView = itemView.findViewById(R.id.ongoing_sales_design_expectedDeliveryDays)
        val ongoingText: TextView = itemView.findViewById(R.id.ongoing_sales_design_ongoingText)
        val deliveryDaysContainer: LinearLayout = itemView.findViewById(R.id.ongoing_sales_design_deliveryDaysContainer)
        val city: TextView = itemView.findViewById(R.id.ongoing_sales_design_buyerCity)
        val confirmText: TextView = itemView.findViewById(R.id.ongoing_sales_design_confirmText)


    }
}