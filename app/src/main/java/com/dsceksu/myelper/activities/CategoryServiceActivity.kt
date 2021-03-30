package com.dsceksu.myelper.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.ProductAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ProductModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_products.*
import me.solidev.loadmore.AutoLoadMoreAdapter
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class CategoryServiceActivity : AppCompatActivity() {
    private var TAG = "AllProductFragment"
    private var productList = ArrayList<ProductModel>()
    private val sponsoredProductList = ArrayList<ProductModel>()
    private var productAdapter: ProductAdapter? = null
    private var autoLoader: AutoLoadMoreAdapter? = null
    private var lastVissiblePosition = 0
    private var sponsoredProductCount = 0
    private var lastItemAddedPosition = 0
    private var lastDocumentSnapshot: DocumentSnapshot? = null
    var count = 0
    var searchText:String = ""
    var category:String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        category = intent.getStringExtra(Constants.category)

        product_activity_categoryTitle.text = category

        productAdapter = ProductAdapter(this, productList, false, false)
        autoLoader = AutoLoadMoreAdapter(this, productAdapter)
        autoLoader!!.setOnLoadListener(object : AutoLoadMoreAdapter.OnLoadListener {
            override fun onLoadMore() {
                if (searchText == ""){
                    getProductList()
                }
            }
            override fun onRetry() {
            }
        })

        product_recyclerView.setHasFixedSize(true)
        val productLayoutManager = GridLayoutManager(this, 2)
        product_recyclerView.layoutManager = productLayoutManager
        product_recyclerView.adapter = autoLoader

        getProductList()

        category_product_searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query !== "" && query !== null){
                    lastDocumentSnapshot = null
                    productList.clear()
                    count = 0
                    autoLoader!!.notifyDataSetChanged()
                    searchText = query.toLowerCase(Locale.ROOT)
                    getProductList()
                }
                return true
            }
        })
        category_product_searchView.setOnCloseListener(object : SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                lastDocumentSnapshot = null
                productList.clear()
                count = 0
                autoLoader!!.notifyDataSetChanged()
                searchText = ""
                getProductList()

                return true
            }
        })

        product_activity_backIcon.setOnClickListener {
            finishAndRemoveTask()

        }
    }
    private fun getProductList() {

        if (lastDocumentSnapshot == null){
            try {
                product_progress.visibility = View.VISIBLE
            }catch (e: Exception){}
        }

        //get all sponsored products
        Utils.database().collection(Constants.products)
                .whereEqualTo(Constants.sponsored,true)
                .whereNotEqualTo(Constants.category,Constants.service)
                .get()
                .addOnSuccessListener {
                    for(snapshot in it){
                        val sponsoredProducts =  snapshot.toObject(ProductModel::class.java)
                        sponsoredProductList.add(sponsoredProducts)
                    }

                    val query: Query = if (searchText == ""){
                        if (lastDocumentSnapshot == null){
                            Utils.database().collection(Constants.products)
                                    .whereEqualTo(Constants.active, true)
                                    .whereEqualTo(Constants.category,category)
                                    .limit(10)
                        }else{
                            Utils.database().collection(Constants.products)
                                    .startAfter(lastDocumentSnapshot!!)
                                    .whereEqualTo(Constants.active, true)
                                    .whereEqualTo(Constants.category,category)
                                    .limit(10)
                        }

                    }else{
                        Utils.database().collection(Constants.products)
                    }

                    query.get().addOnSuccessListener {it1 ->
                        //set last snapshot
                        if(it1.size() > 0){
                            lastDocumentSnapshot = it1.documents[it1.size()-1]
                            for (snapshot in it1){
                                val products =  snapshot.toObject(ProductModel::class.java)
                                if (searchText =="") {
                                    if (!products.sponsored){
                                        productList.add(products)
                                        count++
                                        //get sponsored products
                                        if(count % 3 == 0){
                                            var sponsoredTempCount = 0
                                            loo@ for (item in sponsoredProductList){
                                                if(sponsoredTempCount >= sponsoredProductCount){
                                                    productList.add(item)
                                                    sponsoredTempCount++
                                                    if (sponsoredTempCount % 2 == 0){
                                                        sponsoredProductCount = sponsoredTempCount
                                                        break@loo
                                                    }
                                                }else{
                                                    sponsoredTempCount++
                                                }
                                            }

                                            if(sponsoredTempCount >= sponsoredProductList.size){
                                                sponsoredProductCount = 0
                                            }
                                        }
                                    }
                                }
                                else {
                                    if (products.title.contains(searchText) && products.category == category) {
                                        productList.add(products)
                                        count++
                                        //get sponsored post
                                        if(count % 3 == 0){
                                            var sponsoredTempCount = 0
                                            loo@ for (item in sponsoredProductList){
                                                if(sponsoredTempCount >= sponsoredProductCount){
                                                    productList.add(item)
                                                    sponsoredTempCount++
                                                    if (sponsoredTempCount % 2 == 0){
                                                        sponsoredProductCount = sponsoredTempCount
                                                        break@loo
                                                    }
                                                }else{
                                                    sponsoredTempCount++
                                                }
                                            }
                                            if(sponsoredTempCount >= sponsoredProductList.size){
                                                sponsoredProductCount = 0
                                            }
                                        }
                                    }
                                }
                            }

                            if (productList.isEmpty()){
                                Toasty.info(this,"No product found in thid category", Toasty.LENGTH_LONG).show()
                            }
                            // lastItemAddedPosition = productList.size
                            autoLoader!!.notifyDataSetChanged()
                            //  autoLoader!!.notifyItemRangeInserted(lastItemAddedPosition,it1.size()-1)
                            autoLoader!!.showLoadMore()
                            try {
                                product_progress.visibility = View.GONE
                            }catch (e: Exception){}
                        }
                        else{
                            Utils.dismissLoader()
                            Toasty.error(this,"don't get any document", Toasty.LENGTH_LONG).show()
                        }

                    }

                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this,"Error getting products", Toasty.LENGTH_LONG).show()
                    Log.d(TAG, "getProductList: ${it.message}")
                }
    }

    override fun onResume() {
        super.onResume()
        lastVissiblePosition = (product_recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        (product_recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(lastVissiblePosition)
    }

    override fun onPause() {
        super.onPause()
        lastVissiblePosition = (product_recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        (product_recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(lastVissiblePosition)
    }

}
