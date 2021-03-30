package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.WishListAdapter
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_wish_list.*

class WishListActivity : AppCompatActivity() {
    //for home display
    private var wishList = ArrayList<String>()
    private var adapter: WishListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)


        adapter = WishListAdapter(this, wishList)

        wishList_recyclerView.setHasFixedSize(true)
        val wishListLayoutManager = LinearLayoutManager(this)
        wishListLayoutManager.reverseLayout = true
        wishListLayoutManager.stackFromEnd = true
        wishList_recyclerView.layoutManager = wishListLayoutManager
        wishList_recyclerView.adapter = adapter

        Utils.showLoader(this, "loading")
        getWishLists()

        wishList_backIcon.setOnClickListener {
            finish()
        }
    }

    fun getWishLists() {

        Utils.database().collection(Constants.wishlists)
                .document(Utils.currentUserID())
                .collection(Utils.currentUserID())
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        wishList.clear()

                        for (data in it.documents){
                            wishList.add(data.id)
                        }

                        adapter!!.notifyDataSetChanged()

                        Utils.dismissLoader()
                    }else{
                        Utils.dismissLoader()
                        wishlist_emptyList_container.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this,"Error getting wishlists",Toasty.LENGTH_LONG).show()
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
