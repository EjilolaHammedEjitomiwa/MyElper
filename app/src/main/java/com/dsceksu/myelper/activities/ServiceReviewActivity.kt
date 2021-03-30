package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.ProductReviewAdapter
import com.dsceksu.myelper.Models.ProductReviewModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_seller_review.*

class ServiceReviewActivity : AppCompatActivity() {
    private var itemList = ArrayList<ProductReviewModel>()
    private var adapter: ProductReviewAdapter? = null

    private var productID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_review)

        productID = intent.getStringExtra("product_id")!!

        adapter = ProductReviewAdapter(this, itemList)
        seller_review_activity_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        seller_review_activity_recyclerView.layoutManager = layoutManager
        seller_review_activity_recyclerView.adapter = adapter

        Utils.showLoader(this, "loading")
        loadReviews()
    }

    private fun loadReviews() {
        Utils.database().collection(Constants.products)
                .document(productID!!)
                .collection(Constants.reviews)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        itemList.clear()
                        for (data in it.documents){
                            val review = data.toObject(ProductReviewModel::class.java)
                            itemList.add(review!!)
                        }

                        seller_review_activity_sellerReviewCount.text = "${it.documents.size} reviews"
                        adapter!!.notifyDataSetChanged()
                        Utils.dismissLoader()

                    }else{
                        Utils.dismissLoader()
                        Toasty.info(this,"This product has no review",Toasty.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this,"Error occur loading review",Toasty.LENGTH_LONG).show()
                }

    }

    override fun onResume() {
        super.onResume()
        Utils.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        Utils.setUserOffline()
    }
}
