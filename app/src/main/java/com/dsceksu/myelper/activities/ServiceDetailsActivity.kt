package com.dsceksu.myelper.activities

import android.content.Intent
import android.graphics.Paint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.devs.readmoreoption.ReadMoreOption
import com.dsceksu.myelper.Adapter.ProductAdapter
import com.dsceksu.myelper.Adapter.ProductImageSliderAdapter
import com.dsceksu.myelper.Models.*
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_product_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class ServiceDetailsActivity : AppCompatActivity() {
    private var TAG = "ServiceDetailsActivity"
    private var readMore: ReadMoreOption? = null

    private var productImageList = ArrayList<AddNewProductImageModel>()
    private var productImageAdapter: ProductImageSliderAdapter? = null

    private var moreSellerProductList = ArrayList<ProductModel>()
    private var moreSellerProductAdapter: ProductAdapter? = null

    //sponsored products
    private var sponsoredProductList = ArrayList<ProductModel>()
    private var sponsoredProductAdapter: ProductAdapter? = null

    //for products in same category
    private var productInSameCategoryList = ArrayList<ProductModel>()
    private var productInSameCategoryAdapter: ProductAdapter? = null

    private var productID: String? = ""
    private var category: String? = ""
    private var seller: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        //adapter for more seller product
        moreSellerProductAdapter = ProductAdapter(this, moreSellerProductList, isSellerProductView = false, toWrap = true)
        product_details_moreSellerProductRecyclerView.setHasFixedSize(true)
        val moreSellerProductLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        moreSellerProductLayoutManager.reverseLayout = true
        moreSellerProductLayoutManager.stackFromEnd = true
        product_details_moreSellerProductRecyclerView.layoutManager = moreSellerProductLayoutManager
        product_details_moreSellerProductRecyclerView.adapter = moreSellerProductAdapter

        //product image adapter
        productImageAdapter = ProductImageSliderAdapter(this, productImageList)
        product_details_productImageSlider.setSliderAdapter(productImageAdapter!!)

        //for products in same category
        productInSameCategoryAdapter = ProductAdapter(this, productInSameCategoryList, false, true)
        product_details_moreProductRecyclerView.setHasFixedSize(true)
        val newProductLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        product_details_moreProductRecyclerView.layoutManager = newProductLayoutManager
        product_details_moreProductRecyclerView.adapter = productInSameCategoryAdapter

        //for sponsored products
        sponsoredProductAdapter = ProductAdapter(this, sponsoredProductList, false, true)
        product_details_sponsoredProductRecyclerView.setHasFixedSize(true)
        val sponsoredProductLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        product_details_sponsoredProductRecyclerView.layoutManager = sponsoredProductLayoutManager
        product_details_sponsoredProductRecyclerView.adapter = sponsoredProductAdapter

        readMore = ReadMoreOption.Builder(this)
                .textLength(5, ReadMoreOption.TYPE_LINE)
                .moreLabel("read more")
                .lessLabel("less")
                .moreLabelColor(resources.getColor(R.color.green))
                .lessLabelColor(resources.getColor(R.color.colorPrimary))
                .expandAnimation(true)
                .build()

        productID = intent.getStringExtra("id")
        category = intent.getStringExtra("category")
        seller = intent.getStringExtra("seller_id")

        product_details_moreProductInCategoryTitle.text = "More product in ${category!!.toLowerCase()} category"

        //set recently viewed category

        Utils.setRecentlyViewedCategory(this, category!!)

        product_details_loveIcon.setOnClickListener {
            if (product_details_loveIcon.tag == "unlike") {
                addProductToWishList()
            } else {
                deleteProductFromWishList()
            }
        }
        product_details_sellerIconLike.setOnClickListener {
            if (product_details_sellerIconLike.tag == "unlike") {
                addSellerToFavourite()
            } else {
                removeSellerFromFavourite()
            }
        }
        product_details_add_to_cart_btn.setOnClickListener {
            when (product_details_add_to_cart_text.text) {
                Constants.alreadyAddedToCart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                }
                else -> {
                    addProductToCart()
                }
            }
        }

        product_details_chatWithSeller.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(Constants.recieverID, seller)
            startActivity(intent)
        }

        product_details_followSeller.setOnClickListener {
            if (product_details_followSeller.tag == "followed") {
                unfollowSeller()
            } else {
                followSeller()
            }
        }

        product_details_ratingsContainer.setOnClickListener {
            val intent = Intent(this, ServiceReviewActivity::class.java)
            intent.putExtra("product_id", productID)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        doStuffInBackground().execute()
    }

    private fun followSeller() {
        val query = HashMap<String, Any>()
        query["sellerID"] = seller!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.followseller("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtyNine -> {
                            Toasty.success(this@ServiceDetailsActivity, "Added to following list", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Utils.dismissLoader()
                            Toasty.error(this@ServiceDetailsActivity, "User not found", Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {

                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun unfollowSeller() {
        val query = HashMap<String, Any>()
        query["sellerID"] = seller!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.unfollowseller("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFourty -> {
                            Toasty.success(this@ServiceDetailsActivity, "removed from following list", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@ServiceDetailsActivity, "Users not found", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteProductFromWishList() {
        val query = HashMap<String, Any>()
        query["productID"] = productID!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.removewishlist("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtyFive -> {
                            Toasty.success(this@ServiceDetailsActivity, "It has been removed to your wishlists, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun addProductToWishList() {
        val query = HashMap<String, Any>()
        query["productID"] = productID!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.addwishlist("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(this@ServiceDetailsActivity,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(this@ServiceDetailsActivity,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@ServiceDetailsActivity, "User not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtyFour -> {
                            Toasty.success(this@ServiceDetailsActivity, "It has been added to your wishlists, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                            product_details_loveIcon.alpha = 1f
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun removeSellerFromFavourite() {
        val query = HashMap<String, Any>()
        query["sellerID"] = seller!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.removefavseller("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@ServiceDetailsActivity, "No user found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtySeven -> {
                            Toasty.success(this@ServiceDetailsActivity, "Seller removed from your favourite, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun addSellerToFavourite() {
        val query = HashMap<String, Any>()
        query["sellerID"] = seller!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.addfavseller("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@ServiceDetailsActivity, "User not found", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneThirtySix -> {
                            Toasty.success(this@ServiceDetailsActivity, "Seller added to your favourites, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun addProductToCart() {
        val query = HashMap<String, Any>()
        query["productID"] = productID!!
        query["quantity"] = "1"

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.addtocart("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ServiceDetailsActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(this@ServiceDetailsActivity,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(this@ServiceDetailsActivity,"Your account has been suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyTwo ->{
                            Toasty.error(this@ServiceDetailsActivity, "Account banned", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree ->{
                            Toasty.error(this@ServiceDetailsActivity, "Account suspended", Toast.LENGTH_LONG).show()
                        }
                         Constants.oneSeventy ->{
                            Toasty.error(this@ServiceDetailsActivity, "You can't add your product / service to cart", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneFourtyOne -> {
                            Toasty.success(this@ServiceDetailsActivity, "Add to cart successfully", Toast.LENGTH_LONG).show()
                            product_details_add_to_cart_text.text = Constants.alreadyAddedToCart
                            product_details_add_to_cart_btn.setBackgroundColor(resources.getColor(R.color.green))
                        }
                    }
                } else {
                    Toasty.error(this@ServiceDetailsActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ServiceDetailsActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun isProductAddedToCart() {
        Utils.database()
                .collection(Constants.carts)
                .document(Utils.currentUserID())
                .collection(Utils.currentUserID())
                .document(productID!!)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        product_details_add_to_cart_text.text = Constants.alreadyAddedToCart
                        product_details_add_to_cart_btn.setBackgroundColor(resources.getColor(
                                R.color.green
                        ))
                    }
                }
    }

    private fun setProductImage(imageLists: List<String>) {
        productImageList.clear()
        imageLists.forEach { imageUrl ->
            if (imageUrl != "") {
                productImageList.add(AddNewProductImageModel(imageUrl))
            }
        }
        productImageAdapter!!.notifyDataSetChanged()

        try {
            product_details_productImageSlider.setIndicatorAnimation(IndicatorAnimations.WORM)
        } catch (e: NullPointerException) {
        }
        try {
            product_details_productImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        } catch (e: NullPointerException) {
        }
        try {
            product_details_productImageSlider.startAutoCycle()
        } catch (e: NullPointerException) {
        }
    }

    private fun getMoreSellerProductList() {
        Utils.database().collection(Constants.products)
                .whereEqualTo(Constants.seller, seller)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        it.documents.forEach { sellerProducts ->
                            val product = sellerProducts.toObject(ProductModel::class.java)
                            if (seller == product!!.seller) {
                                moreSellerProductList.add(product)
                            }
                        }
                        moreSellerProductAdapter!!.notifyDataSetChanged()
                    }
                }
    }

    private fun setProductDetails() {
        if (productID != "" && productID != null) {
            Utils.database().collection(Constants.products)
                    .document(productID!!)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val product = it.toObject(ProductModel::class.java)
                            try {
                                product_details_category.text = product!!.category
                                product_details_title.text = product.title
                                product_details_price.text = product.price
                                product_details_cancelledPric.text = product.cancelledPrice
                                product_details_rating.text = product.rating.toString()
                                if (category != Constants.service) {
                                    product_details_noAvailableInStock.text = "${product.noAvailableInStock} available"
                                }
                                product_details_cancelledPric.paintFlags = product_details_cancelledPric.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                setDeductPercent(product.price, product.cancelledPrice)
                                isAddToWishList()
                                setExpectedDeliveryDays(product.deliveryDays)
                                setProductImage(product.imageLists!!)
                                countReviews(productID!!)
                                readMore!!.addReadMoreTo(product_details_sellerDesc, product.description)
                                // product_details_ratingsCount.text = "${p0.child(Constants.ratings).childrenCount} ratings"
                                //product_details_viewCount.text = "${p0.child(Constants.views).childrenCount} views"
                                //check which category
                                when (category) {
                                    Constants.phoneAndTablets -> {
                                        product_details_brand.text = product.brand
                                        product_details_model.text = product.model
                                        product_details_condition.text = product.condition
                                        product_details_noOfUsedDays.text = product.noOfUsedDays
                                        product_details_anyFault.text = product.anyFault
                                        product_details_whatIsInTheBox.text = product.whatIsInTheBox
                                        product_details_storage.text = product.storage
                                        product_details_ram.text = product.ram
                                        product_details_screenSize.text = product.screenSize
                                        product_details_model.text = product.model
                                        product_details_color.text = product.color
                                        product_details_withCase.text = product.withCase
                                        product_details_os.text = product.os
                                        product_details_resolution.text = product.resolution
                                        product_details_simSlot.text = product.simSlot
                                        product_details_estimatedBatteryHour.text = product.estimatedBatteryHour
                                        product_details_mainCamera.text = product.backCamera
                                        product_details_frontCamera.text = product.frontCamera
                                        product_details_battery.text = product.batteryType
                                    }
                                    Constants.laptop -> {
                                        product_details_brand.text = product.brand
                                        product_details_model.text = product.model
                                        product_details_condition.text = product.condition
                                        product_details_noOfUsedDays.text = product.noOfUsedDays
                                        product_details_anyFault.text = product.anyFault
                                        product_details_whatIsInTheBox.text = product.whatIsInTheBox
                                        product_details_storage.text = product.storage
                                        product_details_ram.text = product.ram
                                        product_details_screenSize.text = product.screenSize
                                        product_details_color.text = product.color
                                        product_details_withCase.text = product.withCase
                                        product_details_os.text = product.os
                                        product_details_resolution.text = product.resolution
                                        product_details_estimatedBatteryHour.text = product.estimatedBatteryHour
                                        product_details_processorType.text = product.processorType
                                        product_details_storage.text = product.storage
                                        product_details_battery.text = product.batteryType

                                    }
                                    Constants.grocery -> {
                                        product_details_brand.text = product.brand
                                        product_details_nafdacNo.text = product.nafdacNo
                                    }
                                    Constants.bookAndPQ -> {
                                        product_details_brand.text = product.brand
                                        product_details_anyFault.text = product.anyFault
                                        product_details_isbn.text = product.isbn
                                        product_details_author.text = product.author
                                        product_details_publisher.text = product.publisher
                                        product_details_condition.text = product.condition
                                    }
                                    Constants.foodAndDrink -> {
                                        product_details_brand.text = product.brand
                                        product_details_nafdacNo.text = product.nafdacNo
                                        product_details_color.text = product.color
                                        product_details_alcoholPercent.text = product.alcoholPercent
                                        product_details_sugarPercent.text = product.sugarPercent
                                        product_details_ingredients.text = product.ingredient
                                        product_details_mfg.text = product.mfgDate
                                        product_details_expiryDate.text = product.expiryDate
                                        product_details_containerType.text = product.containerType
                                    }
                                    Constants.gaming -> {
                                        product_details_brand.text = product.brand
                                        product_details_model.text = product.model
                                        product_details_condition.text = product.condition
                                        product_details_noOfUsedDays.text = product.noOfUsedDays
                                        product_details_anyFault.text = product.anyFault
                                        product_details_color.text = product.color
                                    }
                                    Constants.snacksAndCakes -> {
                                        product_details_ingredients.text = product.ingredient
                                        product_details_containerType.text = product.containerType
                                    }
                                    Constants.electronicsAndGadget -> {
                                        product_details_brand.text = product.brand
                                        product_details_condition.text = product.condition
                                        product_details_noOfUsedDays.text = product.noOfUsedDays
                                        product_details_anyFault.text = product.anyFault
                                        product_details_color.text = product.color
                                    }
                                    Constants.fashionAndClothing -> {
                                        product_details_color.text = product.color
                                        product_details_material.text = product.material
                                        product_details_size.text = product.sizes
                                        product_details_condition.text = product.condition
                                    }
                                    Constants.jwelleries -> {
                                        product_details_color.text = product.color
                                        product_details_material.text = product.material
                                    }
                                    Constants.shoesAndFootwears -> {
                                        product_details_color.text = product.color
                                        product_details_material.text = product.material
                                        product_details_size.text = product.sizes
                                        product_details_condition.text = product.condition

                                    }
                                }
                            } catch (e: Exception) {
                            }
                        } else {
                            Toasty.error(this, "Product / Service not exist", Toasty.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Toasty.error(this, "Error getting products", Toasty.LENGTH_LONG).show()
                        Log.d(TAG, "setProductDetails: ${it.message}")

                    }
        }

    }

    private fun countReviews(productID: String) {
        Utils.database()
                .collection(Constants.products)
                .document(productID)
                .collection(Constants.reviews)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        product_details_ratingsCount.text = "See all ${it.documents.size} buyer reviews"
                    } else {
                        product_details_ratingsCount.text = "No buyer review yet"
                    }
                }
    }

    private fun updateProductView() {
        val query = HashMap<String, Any>()
        query["productID"] = productID!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updateproductviews("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {

            }
        })
    }

    private fun setSellerInfo() {
        Utils.database().collection(Constants.users)
                .document(seller!!)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val seller = it.toObject(UsersModel::class.java)
                        if (seller!!.avatar !== "") {
                            Utils.loadImage(this@ServiceDetailsActivity, seller!!.avatar, product_details_sellerProfile)
                        }
                        product_details_sellerName.text = seller!!.username
                        product_details_sellerCampus.text = seller.campus.toUpperCase(Locale.ROOT)
                        if (seller.presence != Constants.online) {
                            product_details_sellerPresence.text = Utils.formatTime(seller.presence.toLong())
                        } else {
                            product_details_sellerPresence.text = seller.presence
                        }
                        product_details_sellerJoinedDate.text = Utils.formatTime(seller.reg_date.toString().toLong())
                    }

                }
    }


    private fun setExpectedDeliveryDays(deliveryDays: String) {
        val currentDate = System.currentTimeMillis()
        val expectedDeliveryDate: Long = currentDate + (deliveryDays.toLong() * 86400000)
        val formatedDate = SimpleDateFormat("EE, dd MM yyyy", Locale.getDefault()).format(expectedDeliveryDate)
        product_details_expectedDeliveryDate.text = "between today and $formatedDate"
    }

    private fun isAddToWishList() {
        if (productID != null && productID != "") {
            Utils.database().collection(Constants.wishlists)
                    .document(Utils.currentUserID())
                    .collection(Utils.currentUserID())
                    .document(productID!!)
                    .addSnapshotListener { value, error ->
                        if (!value!!.exists()) {
                            product_details_loveIcon.setImageResource(R.drawable.icon_unlike)
                            product_details_loveIcon.tag = "unlike"
                        } else {
                            product_details_loveIcon.setImageResource(R.drawable.icon_like)
                            product_details_loveIcon.tag = "liked"
                            product_details_loveIcon.alpha = 1f
                        }
                    }
        }
    }

    private fun isAddSellerToFavourite() {
        if (seller != null && seller != "") {
            Utils.database().collection(Constants.favouriteSellers)
                    .document(Utils.currentUserID())
                    .collection(Utils.currentUserID())
                    .document(seller!!)
                    .addSnapshotListener { value, error ->
                        if (!value!!.exists()) {
                            product_details_sellerIconLike.setImageResource(R.drawable.icon_unlike)
                            product_details_sellerIconLike.tag = "unlike"
                        } else {
                            product_details_sellerIconLike.setImageResource(R.drawable.icon_like)
                            product_details_sellerIconLike.tag = "liked"
                        }
                    }
        }
    }

    private fun setDeductPercent(price: String, cancelledPrice: String) {
        var percent = 0.0
        try {
            percent = (100 * (cancelledPrice.toDouble() - price.toDouble())) / price.toDouble()
        } catch (e: NumberFormatException) {
        }
        try {
            product_details_deductPercent.text = "- ${String.format("%.1f", percent)}%"
        } catch (e: Exception) {
        }
    }

    private fun checkWhichCategory() {
        when (category) {
            Constants.phoneAndTablets -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_modelContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE
                product_details_noOfUsedDaysContainer.visibility = View.VISIBLE
                product_details_anyFaultContainer.visibility = View.VISIBLE
                product_details_whatIsInTheBoxContainer.visibility = View.VISIBLE
                product_details_storageContainer.visibility = View.VISIBLE
                product_details_ramContainer.visibility = View.VISIBLE
                product_details_screenSizeContainer.visibility = View.VISIBLE
                product_details_colorContainer.visibility = View.VISIBLE
                product_details_withCaseContainer.visibility = View.VISIBLE
                product_details_osContainer.visibility = View.VISIBLE
                product_details_resolutionContainer.visibility = View.VISIBLE
                product_details_simSlotContainer.visibility = View.VISIBLE
                product_details_estimatedBatteryHourContainer.visibility = View.VISIBLE
                product_details_mainCameraContainer.visibility = View.VISIBLE
                product_details_frontCameraContainer.visibility = View.VISIBLE
                product_details_batteryContainer.visibility = View.VISIBLE
            }
            Constants.laptop -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_modelContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE
                product_details_noOfUsedDaysContainer.visibility = View.VISIBLE
                product_details_anyFaultContainer.visibility = View.VISIBLE
                product_details_whatIsInTheBoxContainer.visibility = View.VISIBLE
                product_details_storageContainer.visibility = View.VISIBLE
                product_details_ramContainer.visibility = View.VISIBLE
                product_details_screenSizeContainer.visibility = View.VISIBLE
                product_details_colorContainer.visibility = View.VISIBLE
                product_details_withCaseContainer.visibility = View.VISIBLE
                product_details_osContainer.visibility = View.VISIBLE
                product_details_resolutionContainer.visibility = View.VISIBLE
                product_details_estimatedBatteryHourContainer.visibility = View.VISIBLE
                product_details_processorTypeContainer.visibility = View.VISIBLE
                product_details_storageTypeContainer.visibility = View.VISIBLE
                product_details_batteryContainer.visibility = View.VISIBLE
            }
            Constants.grocery -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_nafdacNoContainer.visibility = View.VISIBLE
            }
            Constants.bookAndPQ -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_anyFaultContainer.visibility = View.VISIBLE
                product_details_isbnContainer.visibility = View.VISIBLE
                product_details_authorContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE
                product_details_publisherContainer.visibility = View.VISIBLE
            }
            Constants.foodAndDrink -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_nafdacNoContainer.visibility = View.VISIBLE
                product_details_colorContainer.visibility = View.VISIBLE
                product_details_alcoholPercentContainer.visibility = View.VISIBLE
                product_details_sugarPercentContainer.visibility = View.VISIBLE
                product_details_ingredientsContainer.visibility = View.VISIBLE
                product_details_mfgContainer.visibility = View.VISIBLE
                product_details_expiryDateContainer.visibility = View.VISIBLE
                product_details_containerTypeContainer.visibility = View.VISIBLE
            }
            Constants.gaming -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_modelContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE
                product_details_noOfUsedDaysContainer.visibility = View.VISIBLE
                product_details_anyFaultContainer.visibility = View.VISIBLE
                product_details_colorContainer.visibility = View.VISIBLE
            }
            Constants.snacksAndCakes -> {
                product_details_ingredientsContainer.visibility = View.VISIBLE
                product_details_containerTypeContainer.visibility = View.VISIBLE
            }
            Constants.electronicsAndGadget -> {
                product_details_brandContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE
                product_details_noOfUsedDaysContainer.visibility = View.VISIBLE
                product_details_anyFaultContainer.visibility = View.VISIBLE
                product_details_colorContainer.visibility = View.VISIBLE
            }
            Constants.fashionAndClothing -> {
                product_details_colorContainer.visibility = View.VISIBLE
                product_details_materialContainer.visibility = View.VISIBLE
                product_details_sizeContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE

            }
            Constants.jwelleries -> {
                product_details_colorContainer.visibility = View.VISIBLE
                product_details_materialContainer.visibility = View.VISIBLE
            }
            Constants.shoesAndFootwears -> {
                product_details_colorContainer.visibility = View.VISIBLE
                product_details_materialContainer.visibility = View.VISIBLE
                product_details_sizeContainer.visibility = View.VISIBLE
                product_details_conditionContainer.visibility = View.VISIBLE
                product_details_newOrOkirikaContainer.visibility = View.VISIBLE
            }
            Constants.others -> {
                product_details_featuresTextView.visibility = View.GONE
                product_details_featuresCardView.visibility = View.GONE
            }
        }
    }

    private fun getProductInSameCategory() {
        Utils.database().collection(Constants.products)
                .whereEqualTo(Constants.category, category)
                .limit(50)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        it.documents.forEach { sponsoredProducts ->
                            val product = sponsoredProducts.toObject(ProductModel::class.java)
                            productInSameCategoryList.add(product!!)
                        }
                        productInSameCategoryList.shuffle()
                        productInSameCategoryAdapter!!.notifyDataSetChanged()
                    }
                }
    }

    private fun isFollowing() {
        Utils.database()
                .collection(Constants.users)
                .document(seller!!)
                .collection(Constants.followers)
                .document(Utils.currentUserID())
                .addSnapshotListener { value, error ->
                    if (value!!.exists()) {
                        product_details_followSeller.text = "Unfollow seller"
                        product_details_followSeller.setBackgroundColor(resources.getColor(
                                R.color.blue
                        ))
                        product_details_followSeller.tag = "followed"
                    } else {
                        product_details_followSeller.text = "Follow seller"
                        product_details_followSeller.setBackgroundColor(resources.getColor(
                                R.color.colorPrimary
                        ))
                        product_details_followSeller.tag = "notFollow"
                    }
                }
    }

    private fun followersCount() {
        Utils.database()
                .collection(Constants.users)
                .document(seller!!)
                .collection(Constants.followers)
                .addSnapshotListener { value, error ->
                    if (!value!!.isEmpty) {
                        product_details_followersCount.text = "${value.documents.size} followers"
                    } else {
                        product_details_followersCount.text = "0 followers"
                    }
                }
    }

    private fun getSellerFollowersCount() {
        Utils.databaseRef().child(Constants.users).child(seller!!).child(
                Constants.followers
        ).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    product_details_followersCount.text = "${p0.childrenCount} followers"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Animatoo.animateFade(this)
    }

    inner class doStuffInBackground : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            try {
                Utils.showLoader(this@ServiceDetailsActivity, "loading")
            } catch (e: Exception) {
            }

        }

        override fun doInBackground(vararg p0: Void?): Void? {

            setProductDetails()
            isProductAddedToCart()
            setSellerInfo()
            getSellerFollowersCount()
            checkWhichCategory()
            getMoreSellerProductList()
            updateProductView()
            getProductInSameCategory()
            getSponsoredProducts()
            isAddSellerToFavourite()
            isFollowing()
            followersCount()
            displayProductViewCount()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Utils.dismissLoader()
        }
    }

    private fun displayProductViewCount() {
        Utils.database()
                .collection(Constants.products)
                .document(productID!!)
                .collection(Constants.views)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        product_details_viewCount.text = "${it.documents.size} views"
                    }
                }
    }

    private fun getSponsoredProducts() {
        Utils.database().collection(Constants.products)
                .whereEqualTo(Constants.sponsored, true)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        it.documents.forEach { sponsoredProducts ->
                            val product = sponsoredProducts.toObject(ProductModel::class.java)
                            sponsoredProductList.add(product!!)
                        }
                        sponsoredProductAdapter!!.notifyDataSetChanged()
                    }
                }
    }

    override fun onPause() {
        super.onPause()
        Utils.setUserOffline()
    }

    override fun onResume() {
        super.onResume()
        Utils.setUserOnline()
    }
}
