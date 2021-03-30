package com.dsceksu.myelper.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.dsceksu.myelper.Adapter.AddNewProductImageAdapter
import com.dsceksu.myelper.Models.*
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.jaredrummler.materialspinner.MaterialSpinner
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_add_new_product.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddNewService : AppCompatActivity(){
    //for loading list of categories
    private var TAG = "AddNewService"
    private var categoryList = ArrayList<String>()

    private var productMap = HashMap<String,Any>()

    //product image
    private var productImageRef: StorageReference? = null
    private var productImageBitmap: Bitmap? = null
    private var mediaDownloadUrl: String = ""

    //displaying the product image
    private var imageList = ArrayList<String>()
    private var allProductImages = ""
    private var imageAdapter: AddNewProductImageAdapter? = null

    private var category: String? = null
    private var productId: String? = null
    private var productImageUrl =""

    private var productCondition: String = ""
    private var screenSize: String = ""
    private var bookType: String = ""
    private var foodOrDrink: String = ""
    private var containerType: String = ""
    private var isEdit: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        productImageRef = FirebaseStorage.getInstance().reference.child("product pictures")

        //for product image
        imageAdapter = AddNewProductImageAdapter(this, imageList)
        add_product_imageRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        add_product_imageRecyclerView.layoutManager = layoutManager
        add_product_imageRecyclerView.adapter = imageAdapter

        isEdit = intent.getBooleanExtra(Constants.isEdit, false)
        productId = intent.getStringExtra(Constants.productID)
        category =  intent.getStringExtra(Constants.category)

        //verify details for editing products
        if (isEdit!!) {
            add_product_selectCatgoryContainer.visibility = View.GONE
            loadProductDetails()
        } else {
            loadCategoryItems()
        }

        if (category == null){
            com_content_container.visibility = View.GONE
            add_product_publishBtn.visibility = View.GONE
        }

        if (productId == null){
            productId = Utils.databaseRef().push().key.toString()
        }

        add_product_selectCatgory.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(view: MaterialSpinner?, position: Int, id: Long, item: String?) {
               val intent =  Intent(this@AddNewService, AddNewService::class.java)
                intent.putExtra("category",item)
                startActivity(intent)
                finishAndRemoveTask()
                Animatoo.animateSlideLeft(this@AddNewService)
            }
        })

        add_product_selectImage.setOnClickListener {
            cropImage()
        }

        add_product_publishBtn.setOnClickListener {
            if (category != null) {
                if(add_product_title.text.toString().toLowerCase(Locale.ROOT).contains("|") || add_product_title.text.toString().toLowerCase(Locale.ROOT).contains(";")){
                    Toasty.info(this,"Title cannot contain | or ;) ",Toasty.LENGTH_LONG).show()
                }else{
                    startUploadingProduct()
                }
            } else {
                Toasty.info(this, "please select category", Toasty.LENGTH_LONG).show()
            }

        }
    }



    //LOADING DETAILS FOR EDIT
    private fun loadProductDetails() {
        Utils.showLoader(this,"loading....")
        Utils.database().collection(Constants.products)
                .document(productId!!)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){

                        val product = it.toObject(ProductModel::class.java)
                        try {
                            category = product!!.category
                            //uploadedProductImageCount()
                            //displayUploadedProductImage()
                            add_product_title.setText(product.title)
                            add_product_fullDesc.setText(product.description)
                            add_product_price.setText(product.price)
                            add_product_cancelledPrice.setText(product.cancelledPrice)
                            add_product_deliveryDays.setText(product.deliveryDays)
                            add_product_deliveryCharge.setText(product.deliveryCharge)
                            loadProductImages(product.imageLists)
                            Utils.dismissLoader()
                        } catch (e: Exception) {
                        }

                    }
                }
    }

    private fun loadProductImages(productImages: List<String>?) {
        productImages!!.forEach {
            if (it != ""){
                productImageUrl = it
                imageList.add(it)
                allProductImages+= "$it,"
                imageAdapter!!.notifyDataSetChanged()
            }
       }
    }

    //UPLOADING PRODUCTS
    private fun startUploadingProduct() {

        val title = add_product_title.text.toString().toLowerCase(Locale.ROOT)
        val description = add_product_fullDesc.text.toString()
        val price = add_product_price.text.toString()
        val cancelledPrice = add_product_cancelledPrice.text.toString()
        val deliveryDays = add_product_deliveryDays.text.toString()
        val seller = Utils.currentUserID()
        val deliveryCharge = add_product_deliveryCharge.text.toString()

        when {
           // !isSet -> Toasty.info(this, "Please select product image", Toast.LENGTH_LONG).show()
            imageList.isEmpty() -> Toasty.info(this, "Please select product image", Toast.LENGTH_LONG).show()
           // imageList.size < 3 -> Toasty.info(this, "You have to select minimum of 3 product image", Toast.LENGTH_LONG).show()
            category == null -> Toasty.info(this, "Please select your product category", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(title) -> Toasty.info(this, "title cannot be empty", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(description) -> Toasty.info(this, "description cannot be empty", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(price) -> Toasty.info(this, "price cannot be empty", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(cancelledPrice) -> Toasty.info(this, "cancelled price cannot be empty", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(deliveryDays) -> Toasty.info(this, "delivery days  cannot be empty", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(deliveryCharge) -> Toasty.info(this, "Delivery charge cannot be empty", Toast.LENGTH_LONG).show()

            else ->{

                productMap["productID"] = productId!!
                productMap["category"] = category!!
                productMap["title"] = title
                productMap["description"] = description
                productMap["price"] = price
                productMap["cancelledPrice"] = cancelledPrice
                productMap["deliveryDays"] = deliveryDays
                productMap["seller"] = seller
                productMap["deliveryCharge"] =  deliveryCharge
                productMap["image"] = productImageUrl
                productMap["imageLists"] = allProductImages

                uploadProduct()
            }

        }
    }

    private fun uploadProduct() {
        Utils.showLoader(this,"Uploading....")
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.uploadproduct("Bearer ${Utils.currentUserID()}",productMap)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtyTwo ->{
                            Utils.dismissLoader()
                            finishAndRemoveTask()
                            Animatoo.animateFade(this@AddNewService)
                            Toasty.success(this@AddNewService,"Product Uploaded successfully",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneThirty ->{
                            Utils.dismissLoader()
                            Toasty.info(this@AddNewService,"${res.category} does not exist",Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneTwentyFive ->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"User not found",Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneSixtyTwo ->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"Account banned",Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneSixtyThree ->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"Account suspended",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyFour ->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"No ads slot left please buy more",Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSeventyTwo->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"Product title cannot include | or ; ",Toasty.LENGTH_LONG).show()
                        }

                        Constants.oneSixtyFive ->{
                            Utils.dismissLoader()
                            Toasty.error(this@AddNewService,"Please activated your account first",Toasty.LENGTH_LONG).show()
                            startActivity(Intent(this@AddNewService,SubmitKYCActivity::class.java))
                            finishAndRemoveTask()
                        }

                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@AddNewService, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@AddNewService, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })

    }

    private fun cropImage() {
        CropImage.activity().setAspectRatio(1, 1).start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            val croppedImage = CropImage.getActivityResult(data)
            val categoryImageUri = croppedImage.uri
            productImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, categoryImageUri)
            uploadProductImage()
        }
    }

    private fun uploadProductImage() {
       Utils.showLoader(this,"uploading")
        val randomKey = Utils.databaseRef().push().key
        try {
            val baos = ByteArrayOutputStream()
            productImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 30, baos)
            val data = baos.toByteArray()
            val fileRef = productImageRef!!.child("$randomKey.jpg")
            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putBytes(data)
            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<com.google.firebase.storage.UploadTask.TaskSnapshot, com.google.android.gms.tasks.Task<android.net.Uri>> {
                if (!it.isSuccessful) {
                    it.exception?.let { error ->
                        throw error
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mediaDownloadUrl = task.result.toString()
                    productImageUrl = mediaDownloadUrl
                    imageList.add(mediaDownloadUrl)
                    allProductImages+= "$mediaDownloadUrl,"
                    imageAdapter!!.notifyDataSetChanged()
                    Utils.dismissLoader()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun loadCategoryItems() {
        if (category == null || category == ""){
            Utils.showLoader(this,"Loading categories")
        }

        Utils.database().collection(Constants.productCategories)
            .get()
            .addOnSuccessListener { result ->
                categoryList.clear()
                for (document in result) {
                    val category = document.toObject(CategoryModel::class.java)
                    categoryList.add(category.title)
                }
                categoryList.add(Constants.service)
                add_product_selectCatgory.setItems(categoryList)
                Utils.dismissLoader()
            }
            .addOnFailureListener { exception ->
                Utils.dismissLoader()
                Log.d(TAG, "loadCategoryItems: ${exception.message}")
               Toasty.error(this,"Error occur fetching categories",Toast.LENGTH_LONG).show()
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
