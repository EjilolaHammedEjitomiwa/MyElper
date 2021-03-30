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
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.developer.kalert.KAlertDialog
import com.dsceksu.myelper.activities.CartActivity
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.CartModel
import com.dsceksu.myelper.Models.ProductModel
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.activities.ServiceDetailsActivity
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.jaredrummler.materialspinner.MaterialSpinner
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartAdapter(val context: Context, val cartList: ArrayList<CartModel>) : RecyclerView.Adapter<CartAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_cart_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val products = cartList[position]

        Utils.database()
            .collection(Constants.products)
            .document(products.productID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val product = it.toObject(ProductModel::class.java)
                    if(product!!.category == Constants.service){
                        holder.selectQtyContainer.visibility = View.GONE
                    }else{
                        holder.selectQtyContainer.visibility = View.VISIBLE
                    }
                    try {
                        Utils.loadImage(context,product.image,holder.productImage)
                        holder.productTitle.text = product.title
                        holder.price.text = product.price
                        holder.cancelledPrice.text = product.cancelledPrice
                        holder.cancelledPrice.paintFlags = holder.cancelledPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        holder.deliveryCharge.text = product.deliveryCharge
                        isAddToWhishList(product.productID, holder.likeIcon)
                        setQuantityItem(holder.quantitySpinner,product.noAvailableInStock.toInt())
                        holder.quantity.text =  products.quantity.toString()
                    } catch (e: Exception) {
                    }
                }
            }

        holder.likeIcon.setOnClickListener {
            if (holder.likeIcon.tag == "unlike") {
                addProductToWishList(products.productID)
            } else {
                deleteProductFromWishList(products.productID)
            }
        }
        holder.itemView.setOnClickListener {
            Utils.database()
                .collection(Constants.products)
                .document(products.productID)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val product = it.toObject(ProductModel::class.java)
                        val intent = Intent(context, ServiceDetailsActivity::class.java)
                        intent.putExtra("category", product!!.category)
                        intent.putExtra("id", product.productID)
                        intent.putExtra("seller_id",product.seller)
                        context.startActivity(intent)
                    }
                }
        }
        holder.deleteIcon.setOnClickListener {
            KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setConfirmText("Yes")
                    .setCancelText("Cancel")
                    .setConfirmClickListener {
                        if(context is CartActivity){
                            context.showLoader("Deleting item from cart...")
                        }
                        deleteProductFromCart(products.productID)
                        it.dismissWithAnimation()
                    }.setCancelClickListener {
                        it.dismissWithAnimation()
                    }
                    .show()
        }
        holder.quantitySpinner.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(view: MaterialSpinner?, position: Int, id: Long, item: String?) {
                if (item != null){
                    if(context is CartActivity){
                        context.showLoader("Updating cart items...")
                        updateCartQty(products.productID,item)
                    }
                }
            }
        })
    }

    private fun updateCartQty(productID: String, quantity: String?) {
        val query = HashMap<String,Any>()
        query["productID"] = productID
        query["quantity"] = quantity!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updatecartqty("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Toasty.error(context,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFourtyThree ->{
                            if(context is CartActivity){
                               context.getCartList()
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

    private fun deleteProductFromWishList(productID: String) {
        val query = HashMap<String,Any>()
        query["productID"] = productID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.removewishlist("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Toasty.error(context,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Toasty.error(context,"user not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtyFive ->{
                            Toasty.success(context, "It has been removed to your wishlists, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
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

    private fun addProductToWishList(productID: String) {
        val query = HashMap<String,Any>()
        query["productID"] = productID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.addwishlist("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Toasty.error(context,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Toasty.error(context,"User not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(context,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(context,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneThirtyFour ->{
                            Toasty.success(context, "It has been added to your wishlists, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
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
    private fun setQuantityItem(spinner: MaterialSpinner, noInStock: Int) {
        val noAvailableArrayList = ArrayList<String>()
        for (i in 1..noInStock) {
            noAvailableArrayList.add(i.toString())
        }
        spinner.setItems(noAvailableArrayList)
    }
    private fun deleteProductFromCart(productID: String) {
        val query = HashMap<String,Any>()
        query["productID"] = productID
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.deletefromcart("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Toasty.error(context,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneFourtyTwo ->{
                            if(context is CartActivity){
                                Toasty.success(context,"Item removed successfully",Toasty.LENGTH_LONG).show()
                                context.getCartList()
                            }
                        }
                    }
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
    private fun isAddToWhishList(productID: String, likeIcon: ImageView) {
        Utils.database().collection(Constants.wishlists)
            .document(Utils.currentUserID())
            .collection(Utils.currentUserID())
            .document(productID)
            .addSnapshotListener { value, error ->
                if (!value!!.exists()){
                    likeIcon.setImageResource(R.drawable.icon_unlike)
                    likeIcon.tag = "unlike"
                }else{
                    likeIcon.setImageResource(R.drawable.icon_like)
                    likeIcon.tag = "liked"
                }
            }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cart_productImage)
        val productTitle: TextView = itemView.findViewById(R.id.cart_title)
        val price: TextView = itemView.findViewById(R.id.cart_price)
        val cancelledPrice: TextView = itemView.findViewById(R.id.cart_cancelledPrice)
        val likeIcon: ImageView = itemView.findViewById(R.id.cart_likeicon)
        val deleteIcon: ImageView = itemView.findViewById(R.id.cart_deleteIcon)
        val quantitySpinner: MaterialSpinner = itemView.findViewById(R.id.cart_quantity)
        val quantity: TextView = itemView.findViewById(R.id.cart_qty)
        val deliveryCharge: TextView = itemView.findViewById(R.id.cart_deliveryCharge)
        val selectQtyContainer:LinearLayout = itemView.findViewById(R.id.cart_selectQty)

    }
}