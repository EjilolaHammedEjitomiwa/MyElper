package com.dsceksu.myelper.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.Models.ProductPurchasedModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils

class ProductPurchasedAdapter (val context: Context, val userList: ArrayList<ProductPurchasedModel>) : RecyclerView.Adapter<ProductPurchasedAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.product_purchased_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = userList[position]
        holder.price.text = "Price : ${item.price}"
        holder.quantity.text = "Qty : ${item.quantity}"
        holder.deliveryCharge.text = "D. charge: ${item.deliveryCharge}"
        Utils.loadImage(context,item.image!!,holder.image)
    }
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.product_purchased_design_image)
        val price: TextView = itemView.findViewById(R.id.product_purchased_design_price)
        val quantity: TextView = itemView.findViewById(R.id.product_purchased_design_quantity)
        val deliveryCharge : TextView = itemView.findViewById(R.id.product_purchased_design_deliveryCharge)
    }
}