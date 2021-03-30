package com.dsceksu.myelper.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.developer.kalert.KAlertDialog
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ProductModel
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.activities.ServiceDetailsActivity
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.AddNewService
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ProductAdapter(val context: Context, val productList: ArrayList<ProductModel>,val isSellerProductView:Boolean,val toWrap:Boolean, val isService:Boolean = false) : RecyclerView.Adapter<ProductAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View?
        view = when {
            toWrap -> LayoutInflater.from(context).inflate(R.layout.d_product_layout_wrap, parent, false)
            isService -> LayoutInflater.from(context).inflate(R.layout.d_service_design, parent, false)
            else -> LayoutInflater.from(context).inflate(R.layout.d_product_layout_design, parent, false)
        }
        return ViewHolder(view!!)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        if(isSellerProductView){
            holder.editIcon.visibility = View.VISIBLE
            holder.likeBtn.visibility = View.GONE
            holder.copyProductID.visibility = View.VISIBLE
            holder.deleteIcon.visibility = View.VISIBLE
        }
        else{
            holder.editIcon.visibility = View.GONE
            holder.deleteIcon.visibility = View.GONE
        }

        if(product.sponsored){
            holder.sponsoredPostText.visibility = View.VISIBLE
        }
        else{
            holder.sponsoredPostText.visibility = View.GONE
        }
        Utils.loadImage(context,product.image,holder.image)
        holder.title.text = product.title
        holder.price.text = product.price
        holder.cancelPrice.text = product.cancelledPrice
        holder.cancelPrice.paintFlags = holder.cancelPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        Utils.setDeductPercentage(product.cancelledPrice, product.price,holder.deductPercent)
        isAddToWhishList(product.productID, holder.likeBtn)

        holder.editIcon.setOnClickListener {
            val intent = Intent(context, AddNewService::class.java)
            intent.putExtra(Constants.category,product.category)
            intent.putExtra(Constants.isEdit,true)
            intent.putExtra(Constants.productID,product.productID)
            context.startActivity(intent)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ServiceDetailsActivity::class.java)
            intent.putExtra("category", product.category)
            intent.putExtra("id", product.productID)
            intent.putExtra("seller_id",product.seller)
            context.startActivity(intent)
        }
        holder.likeBtn.setOnClickListener {
            if (holder.likeBtn.tag == "unlike") {
                addProductToWishList(product.productID)
            } else {
                deleteProductFromWishList(product.productID)
            }
        }
        holder.deleteIcon.setOnClickListener {
            KAlertDialog(context, KAlertDialog.WARNING_TYPE)
                .setTitleText("Warning")
                .setContentText("Are you sure")
                .setConfirmText("Confirm")
                .setCancelText("Cancel")
                .setConfirmClickListener {
                    it.dismiss()
                    deleteProduct(it,product.id)
                }.setCancelClickListener {
                    it.dismiss()
                }
                .show()
        }
        holder.copyProductID.setOnClickListener {
            Utils.copyValue(context as Activity,product.productID)
        }
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
                        Constants.oneThirtyFive ->{
                            Toasty.success(context, "It has been removed to your wishlists, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
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
                    Utils.dismissLoader()
                    Toasty.error(context, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(context, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteProduct(alert: KAlertDialog?, id: String) {
        Utils.databaseRef().child(Constants.products).child(id).removeValue().addOnSuccessListener {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    try {
                        alert!!.dismiss()
                    }catch (e:Exception){}
                }
            }, 5000)
        }
    }

    private fun isAddToWhishList(productID: String?, likeBtn: ImageView) {
        if (productID != null && productID != ""){
            Utils.database().collection(Constants.wishlists)
                    .document(Utils.currentUserID())
                    .collection(Utils.currentUserID())
                    .document(productID)
                    .addSnapshotListener { value, error ->
                        if (!value!!.exists()){
                            likeBtn.setImageResource(R.drawable.icon_unlike)
                            likeBtn.tag = "unlike"
                        }else{
                            likeBtn.setImageResource(R.drawable.icon_like)
                            likeBtn.tag = "liked"
                        }
                    }
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.product_layout_design_image)
        val title: TextView = itemView.findViewById(R.id.product_layout_design_title)
        val deductPercent: TextView = itemView.findViewById(R.id.product_layout_design_deductPercent)
        val likeBtn: ImageView = itemView.findViewById(R.id.product_layout_design_likeBtn)
        val price: TextView = itemView.findViewById(R.id.product_layout_design_price)
        val cancelPrice: TextView = itemView.findViewById(R.id.product_layout_design_cancelPrice)
        val deleteIcon:ImageView = itemView.findViewById(R.id.product_layout_design_deleteIcon)
        val editIcon:ImageView = itemView.findViewById(R.id.product_layout_design_editIcon)
        val copyProductID:TextView = itemView.findViewById(R.id.product_ID)
        val sponsoredPostText:TextView = itemView.findViewById(R.id.product_layout_design_sponsored)
    }
}