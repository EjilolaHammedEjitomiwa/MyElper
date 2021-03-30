package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.dsceksu.myelper.Adapter.FavouriteSellerAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_favourite_seller.*

class FavouriteSellerActivity : AppCompatActivity() {
    private var itemList = ArrayList<String>()
    private var adapter: FavouriteSellerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_seller)

        adapter = FavouriteSellerAdapter(this, itemList)
        favourite_seller_activity_recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(this, 2)
        favourite_seller_activity_recyclerView.layoutManager = layoutManager
        favourite_seller_activity_recyclerView.adapter = adapter

        getFavouriteList()
    }

     fun getFavouriteList() {
        showLoader("loading....")
        Utils.database().collection(Constants.favouriteSellers)
                .document(Utils.currentUserID())
                .collection(Utils.currentUserID())
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        itemList.clear()

                        for (data in it.documents){
                            itemList.add(data.id)
                        }
                        adapter!!.notifyDataSetChanged()
                        dismissLoader()
                    }else{
                        dismissLoader()
                        favourite_seller_activity_empty.visibility = View.VISIBLE
                    }

                }
                .addOnFailureListener {
                   dismissLoader()
                    Toasty.error(this,"error occur in getting your facourite sellers",Toasty.LENGTH_LONG).show()
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
