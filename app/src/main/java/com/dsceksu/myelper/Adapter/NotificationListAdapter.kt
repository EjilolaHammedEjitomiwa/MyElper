package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.*
import com.dsceksu.myelper.Models.NotificationModel
import com.dsceksu.myelper.activities.CompletedOrderActivity
import com.dsceksu.myelper.activities.OngoingOrderActivity
import com.dsceksu.myelper.activities.ServiceDetailsActivity
import com.dsceksu.myelper.activities.ServiceReviewActivity
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils

class NotificationListAdapter(val context: Context, val userList: ArrayList<NotificationModel>) : RecyclerView.Adapter<NotificationListAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_notification_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = userList[position]

        holder.time.text = Utils.formatTime(data.date!!.toLong())
        holder.title.text = data.title
        holder.content.text = data.message

        if (data.read) {
            holder.dot.setColorFilter(ContextCompat.getColor(context, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else {
            holder.dot.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        holder.itemView.setOnClickListener {
            val map = HashMap<String, Any>()
            map["read"] = true
            Utils.database().collection(Constants.notification).document(data.id!!).update(map)
                    .addOnSuccessListener {
                        when (data.key) {
                            Constants.newProduct -> {
                                val intent = Intent(context, ServiceDetailsActivity::class.java)
                                intent.putExtra("category", data.productCategory)
                                intent.putExtra("id", data.productID)
                                intent.putExtra("seller_id", data.sellerID)
                                context.startActivity(intent)
                            }
                            Constants.newOrder ->{
                                val intent =  Intent(context, OngoingOrderActivity::class.java)
                                intent.putExtra("is_buyer",true)
                                context.startActivity(intent)
                            }
                            Constants.newSale ->{
                                val intent =  Intent(context, OngoingOrderActivity::class.java)
                                intent.putExtra("is_seller",true)
                                context.startActivity(intent)
                            }
                            Constants.orderStatus ->{
                                val intent =  Intent(context, OngoingOrderActivity::class.java)
                                intent.putExtra("is_buyer",true)
                                context.startActivity(intent)
                            }
                            Constants.orderConfirmed ->{
                                val intent =  Intent(context, CompletedOrderActivity::class.java)
                                intent.putExtra("is_seller",true)
                                context.startActivity(intent)
                            }
                            Constants.orderCancelled ->{
                                val intent =  Intent(context, CompletedOrderActivity::class.java)
                                intent.putExtra("is_buyer",true)
                                context.startActivity(intent)
                            }
                            Constants.newRating ->{
                                val intent = Intent(context, ServiceReviewActivity::class.java)
                                intent.putExtra("product_id", data.productID)
                                context.startActivity(intent)
                            }
                        }
                    }
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dot: ImageView = itemView.findViewById(R.id.notification_design_dot)
        val title: TextView = itemView.findViewById(R.id.notification_design_title)
        val content: TextView = itemView.findViewById(R.id.notification_design_content)
        val time: TextView = itemView.findViewById(R.id.notification_design_time)
    }
}