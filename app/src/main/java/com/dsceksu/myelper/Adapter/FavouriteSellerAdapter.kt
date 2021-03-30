package com.dsceksu.myelper.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.FavouriteSellerActivity
import com.dsceksu.myelper.activities.MyServiceActivity
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.mikhaellopez.circularimageview.CircularImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.view_seller_product_option.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FavouriteSellerAdapter(val context: Context, val sellerID: ArrayList<String>) : RecyclerView.Adapter<FavouriteSellerAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_favourite_seller_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sellerID.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seller = sellerID[position]

        try {
            loadSellerInfo(seller, holder.profileImage, holder.sellerUsername)
            holder.loveIcon.setImageResource(R.drawable.icon_like)
        } catch (e: Exception) { }

        holder.loveIcon.setOnClickListener {
            if (context is FavouriteSellerActivity){
                context.showLoader("Removing seller from favourites..")
            }
           removeSellerFromfavourite(seller,position)
        }
        holder.itemView.setOnClickListener {
            val mDialogueView = LayoutInflater.from(context).inflate(R.layout.view_seller_product_option, null)
            val mBuilder = AlertDialog.Builder(context).setView(mDialogueView)
            val mAlertDualogue = mBuilder.show()

            mAlertDualogue.view_seller_option_viewServices.setOnClickListener {
                mAlertDualogue.dismiss()
                val intent = Intent(context, MyServiceActivity::class.java)
                intent.putExtra("id",seller)
                intent.putExtra(Constants.isViewSellerService,true)
                context.startActivity(intent)
            }
        }
    }

    private fun removeSellerFromfavourite(seller: String, position: Int) {
        val query = HashMap<String,Any>()
        query["sellerID"] = seller
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.removefavseller("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Toasty.error(context,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive ->{
                            Toasty.error(context,"User not found",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneThirtySeven ->{
                            if (context is FavouriteSellerActivity){
                                context.dismissLoader()
                            }
                            Toasty.success(context, "Seller removed from your favourite, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                            sellerID.removeAt(position)
                            notifyDataSetChanged()
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

    private fun loadSellerInfo(seller: String, profileImage: CircularImageView, sellerUsername: TextView) {
        Utils.database().collection(Constants.users)
                .document(seller)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val user = it.toObject(UsersModel::class.java)
                        if (user!!.avatar != ""){
                            Utils.loadImage(context,user.avatar,profileImage)
                        }
                        sellerUsername.text = user.username
                    }
                }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircularImageView = itemView.findViewById(R.id.favourite_seller_design_profileImage)
        val sellerUsername: TextView = itemView.findViewById(R.id.favourite_seller_design_sellerName)
        val loveIcon: ImageView = itemView.findViewById(R.id.favourite_seller_design_likeIcon)
        val container: ConstraintLayout = itemView.findViewById(R.id.favourite_seller_design_container)

    }
}