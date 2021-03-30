package com.dsceksu.myelper.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.developer.kalert.KAlertDialog
import com.dsceksu.myelper.Adapter.CartAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.CartModel
import com.dsceksu.myelper.Models.ProductModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import kotlinx.android.synthetic.main.activity_cart.*
import kotlin.collections.ArrayList

class CartActivity : AppCompatActivity(){
    private var TAG = "CartActivity"
    private var cartList = ArrayList<CartModel>()
    private var cartAdapter: CartAdapter? = null
    private var totalProductPrice =  0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartAdapter = CartAdapter(this@CartActivity, cartList)
        cart_activity_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this@CartActivity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        cart_activity_recyclerView.layoutManager = layoutManager
        cart_activity_recyclerView.adapter = cartAdapter

        showLoader("Loading cart items...")
        getCartList()
        cart_activity_backIcon.setOnClickListener {
            finish()
        }
        cart_activity_nextBtn.setOnClickListener {
            Utils.showLoader(this,"loading...")
            isSetDeliveryAddress()
        }
    }

    private fun isSetDeliveryAddress(){
        Utils.database()
                .collection(Constants.users)
                .document(Utils.currentUserID())
                .collection(Constants.shippingAddress)
                .get()
                .addOnSuccessListener {
                    Utils.dismissLoader()
                    if (!it.isEmpty){
                        KAlertDialog(this@CartActivity, KAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure?")
                                .setContentText("Please confirm your order details")
                                .setConfirmText("Confirm")
                                .setCancelText("Cancel")
                                .setConfirmClickListener {alertDialogueConfirm ->
                                    alertDialogueConfirm.dismiss()
                                    val intent = Intent(this@CartActivity, PaymentActivity::class.java)
                                    intent.putExtra("total_payment",cart_activity_totalPrice.valueString.toString().toInt())
                                    startActivity(intent)
                                }.setCancelClickListener {alertDialogueCancel ->
                                    alertDialogueCancel.dismiss()
                                }
                                .show()
                    }else{
                        startActivity(Intent(this@CartActivity, EditShippingAddress::class.java))
                    }
                }
    }

     fun getCartList() {
         Utils.database()
             .collection(Constants.carts)
             .document(Utils.currentUserID())
             .collection(Utils.currentUserID())
             .get()
             .addOnSuccessListener {
                 if (!it.isEmpty){
                     cartList.clear()

                     for(data in it.documents){
                         val cart =  data.toObject(CartModel::class.java)
                         cartList.add(cart!!)
                     }

                     cartAdapter!!.notifyDataSetChanged()
                     showTotalPrice()
                 }else{
                   dismissLoader()
                     cart_activity_emptyLinear.visibility = View.VISIBLE
                 }
             }
    }

    private fun showTotalPrice() {
        var price = 0
        for(i in cartList){
            Utils.database()
                .collection(Constants.products)
                .document(i.productID)
                .get()
                .addOnSuccessListener {
                    val product = it.toObject(ProductModel::class.java)
                    price += (product!!.price.toInt() * i.quantity.toString().toInt()) + product.deliveryCharge.toInt()
                    totalProductPrice = price
                    cart_activity_totalPrice.setText(price.toString())
                }
        }
        dismissLoader()
    }

    fun showLoader(message:String){
        Utils.showLoader(this,message)
    }
    fun dismissLoader(){
        Utils.dismissLoader()
    }

    override fun onPause() {
        super.onPause()
        Utils.setUserOffline()
    }
}