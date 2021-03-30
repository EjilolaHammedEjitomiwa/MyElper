package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
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
import com.dsceksu.myelper.*
import com.dsceksu.myelper.Models.ProductModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.activities.ServiceDetailsActivity
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.mikhaellopez.circularimageview.CircularImageView

import java.lang.Exception
import java.lang.NumberFormatException

class WishListAdapter(val context: Context, val productID: ArrayList<String>) : RecyclerView.Adapter<WishListAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_wishlists_design_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productID.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productID = productID[position]

        Utils.database().collection(Constants.products)
                .document(productID)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val product = it.toObject(ProductModel::class.java)
                        holder.title.text = product!!.title
                        holder.cancelPrice.text = product.cancelledPrice
                        holder.cancelPrice.paintFlags = holder.cancelPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        holder.price.text = product.price
                        Utils.loadImage(context, product.image, holder.image)
                        setDeductPercent(product.cancelledPrice, product.price, holder.deductPercent)
                        loadSellerProfileImage(product.seller, holder.sellerImage)
                    }
                }

        holder.itemView.setOnClickListener {
            Utils.database().collection(Constants.products)
                    .document(productID)
                    .get()
                    .addOnSuccessListener {docSnap ->
                        if (docSnap.exists()) {
                            val product = docSnap.toObject(ProductModel::class.java)
                            val intent = Intent(context, ServiceDetailsActivity::class.java)
                            intent.putExtra("category", product!!.category)
                            intent.putExtra("id", productID)
                            intent.putExtra("seller_id", product.seller)
                            context.startActivity(intent)
                        }
                    }
        }
    }

    private fun loadSellerProfileImage(seller: String, sellerImage: CircularImageView) {
        Utils.database().collection(Constants.users)
                .document(seller)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val seller = it.toObject(UsersModel::class.java)
                        if (seller!!.avatar != "") {
                            Glide.with(context).load(seller.avatar).into(sellerImage)
                        }
                    }
                }
    }

    private fun setDeductPercent(cancelledPrice: String, price: String, deductPercent: TextView) {
        var percent = 0.0
        try {
            percent = (100 * (cancelledPrice.toDouble() - price.toDouble())) / price.toDouble()
        } catch (e: NumberFormatException) {
        }
        try {
            deductPercent.text = "- ${String.format("%.1f", percent)}%"
        } catch (e: Exception) {
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.wishlist_design_image)
        val title: TextView = itemView.findViewById(R.id.wishlist_design_title)
        val deductPercent: TextView = itemView.findViewById(R.id.wishlist_design_percent)
        val price: TextView = itemView.findViewById(R.id.wishlist_design_price)
        val cancelPrice: TextView = itemView.findViewById(R.id.wishlist_design_cancelledPrice)
        val sellerImage: CircularImageView = itemView.findViewById(R.id.wishlist_design_profileImage)
        val container: LinearLayout = itemView.findViewById(R.id.wishList_container)
    }
}