package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.ProductAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ProductModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_my_service.*
import kotlinx.android.synthetic.main.activity_my_service.active_product_backIcon
import me.solidev.loadmore.AutoLoadMoreAdapter
import me.solidev.loadmore.AutoLoadMoreConfig

class MyServiceActivity : AppCompatActivity() {
    private var TAG = "AllProductFragment"
    private var serviceLists = ArrayList<ProductModel>()
    private var servieAdapter: ProductAdapter? = null
    private var autoLoader: AutoLoadMoreAdapter? = null
    private var lastVissiblePosition = 0
    private var lastDocumentSnapshot: DocumentSnapshot? = null

    private var sellerID: String? = ""
    private var isViewSellerService: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_service)

        AutoLoadMoreConfig.setGlobalLoadingView(R.layout.d_product_loading_layout_design)
        AutoLoadMoreConfig.setGlobalLoadFinishView(R.layout.d_all_data_loaded_design)

        sellerID = intent.getStringExtra("id")
        isViewSellerService = intent.getBooleanExtra(Constants.isViewSellerService, false)

        servieAdapter = if (sellerID == Utils.currentUserID()) {
            ProductAdapter(this, serviceLists, true, false, true)
        } else {
            ProductAdapter(this, serviceLists, false, false,true)
        }

        autoLoader = AutoLoadMoreAdapter(this, servieAdapter)
        autoLoader!!.setOnLoadListener(object : AutoLoadMoreAdapter.OnLoadListener {
            override fun onLoadMore() {
                getServiceLists()
            }

            override fun onRetry() {
            }
        })

        my_service_recyclerView.setHasFixedSize(true)
        val productLayoutManager = LinearLayoutManager(this)
        my_service_recyclerView.layoutManager = productLayoutManager
        my_service_recyclerView.adapter = autoLoader

        Utils.showLoader(this, "Loading..")
        getServiceLists()

        if (isViewSellerService!!) {
            loadSellerUsername()
        }

        active_product_backIcon.setOnClickListener {
            finish()
        }
    }

    private fun loadSellerUsername() {
        Utils.database().collection(Constants.users)
                .document(sellerID!!)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val seller = it.toObject(UsersModel::class.java)
                        my_service_toolbarTitle.text = "${seller!!.username} services"
                    }
                }
    }


    private fun getServiceLists() {
        val query: Query =
                if (lastDocumentSnapshot == null) {
                    Utils.database().collection(Constants.products)
                            .whereEqualTo(Constants.seller, sellerID)
                            .whereEqualTo(Constants.active, true)
                            .whereEqualTo(Constants.category, Constants.service)
                            .limit(10)
                } else {
                    Utils.database().collection(Constants.products)
                            .startAfter(lastDocumentSnapshot!!)
                            .whereEqualTo(Constants.seller, sellerID)
                            .whereEqualTo(Constants.active, true)
                            .whereEqualTo(Constants.category, Constants.service)
                            .limit(10)
                }

        query.get().addOnSuccessListener { it1 ->
            //set last snapshot
            if (it1.size() > 0) {
                lastDocumentSnapshot = it1.documents[it1.size() - 1]
                for (snapshot in it1) {
                    val products = snapshot.toObject(ProductModel::class.java)
                    serviceLists.add(products)
                }

                if (serviceLists.isEmpty()) {
                    Toasty.info(this, "No service found", Toasty.LENGTH_LONG).show()
                }
                // lastItemAddedPosition = productList.size
                autoLoader!!.notifyDataSetChanged()
                Utils.dismissLoader()
                //  autoLoader!!.notifyItemRangeInserted(lastItemAddedPosition,it1.size()-1)
                autoLoader!!.showLoadMore()
            } else {
                Utils.dismissLoader()
                Toasty.error(this, "don't get any document", Toasty.LENGTH_LONG).show()
            }

        }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this,"Error getting service list",Toasty.LENGTH_LONG).show()
                    Log.d(TAG, "getServiceLists: ${it.message}")
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