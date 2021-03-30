package com.dsceksu.myelper.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.ProductAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ProductModel

import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_service.*
import kotlinx.android.synthetic.main.fragment_service.view.*
import me.solidev.loadmore.AutoLoadMoreAdapter
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ServiceFragment : Fragment() {
    private var TAG = "ServiceFragment"
    private var serviceLists = ArrayList<ProductModel>()
    private val sponsoredServiceLists = ArrayList<ProductModel>()
    private var serviceAdapter: ProductAdapter? = null
    private var autoLoader: AutoLoadMoreAdapter? = null
    private var lastVissiblePosition = 0
    private var sponsoredProductCount = 0
    private var lastItemAddedPosition = 0
    private var lastDocumentSnapshot: DocumentSnapshot? = null
    var count = 0
    var searchText: String = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_service, container, false)

        serviceAdapter = ProductAdapter(context!!, serviceLists, false, false, true)
        autoLoader = AutoLoadMoreAdapter(context, serviceAdapter)
        autoLoader!!.setOnLoadListener(object : AutoLoadMoreAdapter.OnLoadListener {
            override fun onLoadMore() {
                if (searchText == "") {
                    getServiceLists()
                }
            }

            override fun onRetry() {
            }
        })

        view.service_fragment_recyclerView.setHasFixedSize(true)
        val productLayoutManager = LinearLayoutManager(context)
        view.service_fragment_recyclerView.layoutManager = productLayoutManager
        view.service_fragment_recyclerView.adapter = autoLoader

        getServiceLists()

        view.service_fragment_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query !== "" && query !== null) {
                    lastDocumentSnapshot = null
                    serviceLists.clear()
                    count = 0
                    autoLoader!!.notifyDataSetChanged()
                    searchText = query.toLowerCase(Locale.ROOT)
                    getServiceLists()
                }
                return true
            }
        })
        view.service_fragment_search.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                lastDocumentSnapshot = null
                serviceLists.clear()
                count = 0
                autoLoader!!.notifyDataSetChanged()
                searchText = ""
                getServiceLists()

                return true
            }
        })

        return view
    }

    private fun getServiceLists() {

        if (lastDocumentSnapshot == null) {
            try {
                service_fragment_progress.visibility = View.VISIBLE
            } catch (e: Exception) {
            }
        }

        //get all sponsored products
        Utils.database().collection(Constants.products)
                .whereEqualTo(Constants.sponsored, true)
                .whereEqualTo(Constants.category, Constants.service)
                .get()
                .addOnSuccessListener {
                    for (snapshot in it) {
                        val sponsoredService = snapshot.toObject(ProductModel::class.java)
                        sponsoredServiceLists.add(sponsoredService)
                    }

                    val query: Query = if (searchText == "") {
                        if (lastDocumentSnapshot == null) {
                            Utils.database().collection(Constants.products)
                                    .whereEqualTo(Constants.category, Constants.service)
                                    .whereEqualTo(Constants.active, true)
                                    .limit(10)
                        } else {
                            Utils.database().collection(Constants.products)
                                    .startAfter(lastDocumentSnapshot!!)
                                    .whereEqualTo(Constants.category, Constants.service)
                                    .whereEqualTo(Constants.active, true)
                                    .limit(10)
                        }
                    } else {
                        Utils.database().collection(Constants.products)
                    }

                    query.get().addOnSuccessListener { it1 ->
                        //set last snapshot
                        if (it1.size() > 0) {
                            lastDocumentSnapshot = it1.documents[it1.size() - 1]
                            for (snapshot in it1) {
                                val products = snapshot.toObject(ProductModel::class.java)
                                if (searchText == "") {
                                    serviceLists.add(products)
                                    count++
                                    //get sponsored products
                                    if (count % 3 == 0) {
                                        var sponsoredTempCount = 0
                                        loo@ for (item in sponsoredServiceLists) {
                                            if (sponsoredTempCount >= sponsoredProductCount) {
                                                serviceLists.add(item)
                                                sponsoredTempCount++
                                                if (sponsoredTempCount % 2 == 0) {
                                                    sponsoredProductCount = sponsoredTempCount
                                                    break@loo
                                                }
                                            } else {
                                                sponsoredTempCount++
                                            }
                                        }

                                        if (sponsoredTempCount >= sponsoredServiceLists.size) {
                                            sponsoredProductCount = 0
                                        }
                                    }
                                } else {
                                    if (products.title.contains(searchText) && products.category == Constants.service) {
                                        serviceLists.add(products)
                                        count++
                                        //get sponsored post
                                        if (count % 3 == 0) {
                                            var sponsoredTempCount = 0
                                            loo@ for (item in sponsoredServiceLists) {
                                                if (sponsoredTempCount >= sponsoredProductCount) {
                                                    serviceLists.add(item)
                                                    sponsoredTempCount++
                                                    if (sponsoredTempCount % 2 == 0) {
                                                        sponsoredProductCount = sponsoredTempCount
                                                        break@loo
                                                    }
                                                } else {
                                                    sponsoredTempCount++
                                                }
                                            }
                                            if (sponsoredTempCount >= sponsoredServiceLists.size) {
                                                sponsoredProductCount = 0
                                            }
                                        }
                                    }
                                }
                            }

                            if (serviceLists.isEmpty()) {
                                Toasty.info(requireContext(), "No service found", Toasty.LENGTH_LONG).show()
                            }
                            // lastItemAddedPosition = productList.size
                            autoLoader!!.notifyDataSetChanged()
                            //  autoLoader!!.notifyItemRangeInserted(lastItemAddedPosition,it1.size()-1)
                            autoLoader!!.showLoadMore()
                            try {
                                service_fragment_progress.visibility = View.GONE
                            } catch (e: Exception) {
                            }
                        } else {
                            Utils.dismissLoader()
                            try {
                                Toasty.error(requireContext(), "don't get any document", Toasty.LENGTH_LONG).show()
                            }catch (e:Exception){}

                        }

                    }

                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(requireContext(), "Error getting products", Toasty.LENGTH_LONG).show()
                    Log.d(TAG, "getProductList: ${it.message}")
                }
    }

    override fun onResume() {
        super.onResume()
        lastVissiblePosition = (service_fragment_recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        (service_fragment_recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(lastVissiblePosition)
    }

    override fun onPause() {
        super.onPause()
        lastVissiblePosition = (service_fragment_recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        (service_fragment_recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(lastVissiblePosition)
    }
}