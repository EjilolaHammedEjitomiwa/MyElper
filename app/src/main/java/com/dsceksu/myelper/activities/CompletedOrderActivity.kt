package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.CompletedOrderAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.OngoingOrderModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_completed_order.*

class CompletedOrderActivity : AppCompatActivity() {
    private var TAG = "CompletedOrderActivity"
    private var itemList = ArrayList<OngoingOrderModel>()
    private var adapter: CompletedOrderAdapter? = null

    private var isBuyer: Boolean? = null
    private var isSeller: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_order)

        isBuyer = intent.getBooleanExtra("is_buyer", false)
        isSeller = intent.getBooleanExtra("is_seller", false)

        if (isBuyer!!) {
            adapter = CompletedOrderAdapter(this, itemList, true)
            completed_order_toolbarTitle.text = getString(R.string.My_Completed_Orders)
        }

        if (isSeller!!) {
            adapter = CompletedOrderAdapter(this, itemList, false)
            completed_order_toolbarTitle.text = getString(R.string.My_Completed_Sales)
        }

        completed_order_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        completed_order_recyclerView.layoutManager = layoutManager
        completed_order_recyclerView.adapter = adapter

        completed_order_backIcon.setOnClickListener {
            finish()
        }

        getItemList()
    }

    private fun getItemList() {
        Utils.showLoader(this, "Loading orders...")

        val ongoingOrder = if (isSeller!!) {
            Utils.database()
                    .collection(Constants.orders)
                    .whereEqualTo("seller", Utils.currentUserID())
                    .whereNotEqualTo("status", "ongoing")
        } else {
            Utils.database()
                    .collection(Constants.orders)
                    .whereEqualTo("buyer", Utils.currentUserID())
                    .whereNotEqualTo("status", "ongoing")

        }

        ongoingOrder.get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        itemList.clear()
                        for (data in it.documents) {
                            val order = data.toObject(OngoingOrderModel::class.java)
                            itemList.add(order!!)
                        }

                        itemList.sortBy {itemList ->
                            itemList.date.toString().toLong()
                        }
                        adapter!!.notifyDataSetChanged()
                        Utils.dismissLoader()

                    } else {
                        Utils.dismissLoader()
                        completed_orders_activity_emptyLinear.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this, "Error occur", Toasty.LENGTH_LONG).show()
                    Log.d(TAG, "getItemList: ${it.message}")
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
