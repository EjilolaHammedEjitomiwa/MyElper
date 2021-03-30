package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.Models.TransactionHistoryModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.TransactionDetailsActivity
import com.dsceksu.myelper.constants.Constants

class TransactionHistoryAdapter(val context: Context, val itemList: ArrayList<TransactionHistoryModel>) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transaction_history_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        holder.date.text = DateFormat.format("EE, MMM dd, yyyy hh-mm-ss a", item.date!!.toString().toLong())
        holder.title.text = item.title.toString()
        holder.amount.text = item.amount.toString()
        holder.reference.text = "Ref: ${item.paymentTransactionRef}"
        holder.status.text = item.status.toString()
        holder.transactionMessage.text = item.transactionMessage.toString()

        if (item.title.toString() == Constants.orderDeliveredPayment || item.title.toString() == Constants.orderCancelRefund) {
            holder.price.visibility = View.VISIBLE
            holder.quantity.visibility = View.VISIBLE
            holder.deliveryCharge.visibility = View.VISIBLE

            holder.price.text = "Price: ${item.productPrice.toString()}"
            holder.quantity.text = "Qty: ${item.productQuantity.toString()}"
            holder.deliveryCharge.text = "D.charge: ${item.productDeliveryCharge.toString()}"
        } else {
            holder.price.visibility = View.GONE
            holder.quantity.visibility = View.GONE
            holder.deliveryCharge.visibility = View.GONE
        }

        if (item.title.toString() == Constants.sponsoredProductPayment || item.title.toString() == Constants.sponsoredPostPayment ) {
            holder.weeks.visibility = View.VISIBLE
            holder.weeks.text = "Weeks: ${item.sponsoredWeeks.toString()}"
        } else {
            holder.weeks.visibility = View.GONE
        }

        when (item.status) {
            Constants.successful -> {
                holder.status.setTextColor(context.resources.getColor(R.color.green))
                holder.transactionMessage.setTextColor(context.resources.getColor(R.color.green))
            }
            Constants.failed -> {
                holder.status.setTextColor(context.resources.getColor(R.color.alizarin_crimson))
                holder.transactionMessage.setTextColor(context.resources.getColor(R.color.alizarin_crimson))
            }
            else -> {
                holder.status.setTextColor(context.resources.getColor(R.color.gray))
                holder.transactionMessage.setTextColor(context.resources.getColor(R.color.gray))
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context,TransactionDetailsActivity::class.java)
            intent.putExtra(Constants.transactionsTitle, item.title.toString())
            intent.putExtra(Constants.transactionID, item.transactionID.toString())
            intent.putExtra(Constants.transactionDate, item.date.toString())
            intent.putExtra(Constants.transactionAmount, item.amount.toString())
            intent.putExtra(Constants.transactionStatus, item.status.toString())
            intent.putExtra(Constants.transactionMessage, item.transactionMessage.toString())
            intent.putExtra(Constants.transactionPaymentRef, item.paymentTransactionRef.toString())

            when(item.title.toString()){

                Constants.orderDeliveredPayment,Constants.orderCancelRefund ->{
                    intent.putExtra(Constants.productTitle, item.productTitle.toString())
                    intent.putExtra(Constants.productPrice, item.productPrice.toString())
                    intent.putExtra(Constants.productQuantity, item.productQuantity.toString())
                    intent.putExtra(Constants.productDeliveryCharge, item.productDeliveryCharge.toString())
                    intent.putExtra(Constants.productImage,item.productImage)
                    intent.putExtra(Constants.amountSettled,item.amountSettled.toString())
                }
                Constants.sponsoredProductPayment ->{
                    intent.putExtra(Constants.sponsoredWeeks,item.sponsoredWeeks.toString())
                }
                Constants.sponsoredPostPayment ->{
                    intent.putExtra(Constants.sponsoredWeeks,item.sponsoredWeeks.toString())
                    intent.putExtra(Constants.postTitle,item.postTitle.toString())
                    intent.putExtra(Constants.postContent,item.content.toString())
                    intent.putExtra(Constants.postImage,item.image.toString())
                    intent.putExtra(Constants.contactNumber,item.number.toString())
                    intent.putExtra(Constants.contactEmail,item.email.toString())
                    intent.putExtra(Constants.contactWebsite,item.website.toString())
                }

                Constants.productPurchase ->{
                    intent.putStringArrayListExtra(Constants.productPurchase, ArrayList(item.productsPurchased as List<String>))
                }
                Constants.AdSlotPurchase ->{
                    intent.putExtra(Constants.slot,item.slot.toString())
                }

            }

            context.startActivity(intent)
        }

    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.history_design_date)
        val title: TextView = itemView.findViewById(R.id.history_design_title)
        val amount: TextView = itemView.findViewById(R.id.history_design_amount)
        val reference: TextView = itemView.findViewById(R.id.history_design_reference)
        val status: TextView = itemView.findViewById(R.id.history_design_status)
        val price: TextView = itemView.findViewById(R.id.history_design_price)
        val quantity: TextView = itemView.findViewById(R.id.history_design_quantity)
        val viewDetails: TextView = itemView.findViewById(R.id.history_design_viewDetails)
        val weeks: TextView = itemView.findViewById(R.id.history_design_weeks)
        val transactionMessage: TextView = itemView.findViewById(R.id.history_design_transactionMessage)
        val viewReciept: TextView = itemView.findViewById(R.id.history_design_viewReciept)
        val deliveryCharge:TextView = itemView.findViewById(R.id.history_design_deliveryCharge)
    }
}