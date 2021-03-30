package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.OngoingOrderAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.OngoingOrderModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_active_order.*

class OngoingOrderActivity : AppCompatActivity() {
    private val TAG = "OngoingOrderActivity"
    private var itemList = ArrayList<OngoingOrderModel>()
    private var adapter: OngoingOrderAdapter? = null

    private var isBuyer: Boolean? = null
    private var isSeller: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_order)

        isBuyer = intent.getBooleanExtra("is_buyer", false)
        isSeller = intent.getBooleanExtra("is_seller", false)

        if (isBuyer!!) {
            adapter = OngoingOrderAdapter(this, itemList, false)
        }
        if (isSeller!!) {
            adapter = OngoingOrderAdapter(this, itemList, true)
        }
        active_orders_activity_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        active_orders_activity_recyclerView.layoutManager = layoutManager
        active_orders_activity_recyclerView.adapter = adapter

        showLoader("Loading orders...")
        getItemList()

        active_orders_activity_backIcon.setOnClickListener {
            finish()
        }
    }

    fun getItemList() {
        val ongoingOrder=  if (isSeller!!) {
            Utils.database()
                    .collection(Constants.orders)
                    .whereEqualTo("seller",Utils.currentUserID())
                    .whereEqualTo("status","ongoing")
                    .orderBy("date")
        }else{
            Utils.database()
                    .collection(Constants.orders)
                    .whereEqualTo("buyer",Utils.currentUserID())
                    .whereEqualTo("status","ongoing")
                    .orderBy("date")
        }

        ongoingOrder.get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        itemList.clear()

                        for (data in it.documents){
                            val order = data.toObject(OngoingOrderModel::class.java)
                            itemList.add(order!!)
                            order.orderID = data.id
                        }

                        adapter!!.notifyDataSetChanged()
                        dismissLoader()

                    }else{
                        dismissLoader()
                        active_orders_activity_emptyLinear.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    dismissLoader()
                    Toasty.error(this,"Error occur",Toasty.LENGTH_LONG).show()
                    Log.d(TAG, "getItemList: ${it.message}")
                }
    }

    fun showLoader(message:String){
        Utils.showLoader(this,message)
    }
    fun dismissLoader(){
        Utils.dismissLoader()
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
