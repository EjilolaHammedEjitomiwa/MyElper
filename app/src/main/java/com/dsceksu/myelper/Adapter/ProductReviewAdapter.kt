package com.dsceksu.myelper.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ProductReviewModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.mikhaellopez.circularimageview.CircularImageView
import com.willy.ratingbar.ScaleRatingBar
import java.lang.Exception

class ProductReviewAdapter(val context: Context, val reviewList: ArrayList<ProductReviewModel>) : RecyclerView.Adapter<ProductReviewAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_seller_review_design_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewList[position]
        try {
            loadRaterProfileImage(holder.rateProfileImage, review.rater, holder.raterName)
            holder.reviewMessage.text = review.reviewMessage
            holder.rating.text = review.rating.toString()
            holder.date.text = Utils.formatTime(review.date)
            holder.ratingBar.rating = review.rating.toString().toFloat()
        } catch (e: Exception) {
        }
    }

    private fun loadRaterProfileImage(rateProfileImage: CircularImageView, rater: String, raterName: TextView) {
        Utils.database()
                .collection(Constants.users)
                .document(rater)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(UsersModel::class.java)
                        raterName.text = user!!.username
                        if (user.avatar != "") {
                            Utils.loadImage(context, user.avatar, rateProfileImage)
                        }
                    }
                }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rateProfileImage: CircularImageView = itemView.findViewById(R.id.seller_review_design_profileImage)
        val reviewMessage: TextView = itemView.findViewById(R.id.seller_review_design_reviewMessage)
        val date: TextView = itemView.findViewById(R.id.seller_review_design_date)
        val rating:TextView = itemView.findViewById(R.id.seller_review_design_rating)
        val raterName: TextView = itemView.findViewById(R.id.seller_review_design_raterName)
        val ratingBar: ScaleRatingBar = itemView.findViewById(R.id.seller_review_design_account_rating)
    }
}