package com.dsceksu.myelper.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.dsceksu.myelper.Fragments.AccountFragment
import com.dsceksu.myelper.Fragments.ServiceFragment
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var selectedFragment: Fragment? = null
    private val TAG = "MainActivity"
    private var drawerLayout: DrawerLayout? = null
    private var navigatioView: NavigationView? = null

    private var profilePicRef: StorageReference? = null
    private var profilePicBitmap: Bitmap? = null
    private var mediaDownloadUrl: String = ""

    private var key: String? = null
    private var title: String? = null
    private var body: String? = null
    private var sender: String? = null
    private var productID: String? = null
    private var productCategory: String? = null
    private var seller: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.userLeftChatActivity(this)
        profilePicRef = FirebaseStorage.getInstance().reference.child("profile pictures")
        main_bottomBar.setActiveItem(0)
        supportFragmentManager.beginTransaction().replace(R.id.main_frame, ServiceFragment()).commit()
        bottomNavSetUp()
        setUpNavigation()

        //getting intent from notification
        key = intent.getStringExtra("key")
        title = intent.getStringExtra("title")
        body = intent.getStringExtra("body")
        sender = intent.getStringExtra("user")
        productID = intent.getStringExtra("productID")
        productCategory = intent.getStringExtra("productCategory")
        seller = intent.getStringExtra("sellerID")

        when (key) {
            Constants.newProduct -> {
                val intent = Intent(this, ServiceDetailsActivity::class.java)
                intent.putExtra("category", productCategory)
                intent.putExtra("id", productID)
                intent.putExtra("seller_id", seller)
                startActivity(intent)
            }
            Constants.newOrder -> {
                val intent = Intent(this, OngoingOrderActivity::class.java)
                intent.putExtra("is_buyer", true)
                startActivity(intent)
            }
            Constants.newSale -> {
                val intent = Intent(this, OngoingOrderActivity::class.java)
                intent.putExtra("is_seller", true)
                startActivity(intent)
            }

            Constants.orderStatus -> {
                val intent = Intent(this, OngoingOrderActivity::class.java)
                intent.putExtra("is_buyer", true)
                startActivity(intent)
            }

            Constants.orderConfirmed -> {
                val intent = Intent(this, CompletedOrderActivity::class.java)
                intent.putExtra("is_seller", true)
                startActivity(intent)
            }
            Constants.newRating -> {
                val intent = Intent(this, ServiceReviewActivity::class.java)
                intent.putExtra("product_id", productID)
                startActivity(intent)
            }

            Constants.orderCancelled -> {
                val intent = Intent(this, CompletedOrderActivity::class.java)
                intent.putExtra("is_buyer", true)
                startActivity(intent)
            }

        }

        main_notification.setOnClickListener {
            startActivity(Intent(this, NotificationList::class.java))
        }
        main_cart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        getUnreadNotificationCount()
        getCartList()
    }

    private fun setUpNavigation() {
        drawerLayout = findViewById(R.id.main_drawer_layout)
        navigatioView = findViewById(R.id.main_navView)
        navigatioView!!.setNavigationItemSelectedListener(this)
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, main_toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout!!.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.home -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_frame, ServiceFragment()).commit()
                main_bottomBar.setActiveItem(0)
            }
        }
        closeDrawer()
        return false
    }

    private fun closeDrawer() {
        drawerLayout!!.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START))
            closeDrawer()
        else {
            super.onBackPressed()
        }
    }

    private fun getUnreadNotificationCount() {
        Utils.database().collection(Constants.notification)
                .whereEqualTo("userID", Utils.currentUserID())
                .whereEqualTo("read", false)
                .addSnapshotListener(this) { value, error ->
                    if (!value!!.isEmpty) {
                        main_notification_counter.text = value.documents.size.toString()
                    }
                }
    }

    private fun getCartList() {
        Utils.database()
                .collection(Constants.carts)
                .document(Utils.currentUserID())
                .collection(Utils.currentUserID())
                .addSnapshotListener(this) { value, error ->
                    if (!value!!.isEmpty) {
                        main_cart_counter.text = value.documents.size.toString()
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            val cropedImage = CropImage.getActivityResult(data)
            val categoryImageUri = cropedImage.uri
            profilePicBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, categoryImageUri)
            uploadProfilePic().execute()
        }
    }

    private fun bottomNavSetUp() {
        main_bottomBar.onItemSelected = {
            when (it) {
                0 -> selectedFragment = ServiceFragment()
                1 -> startActivity(Intent(this, MessagesActivity::class.java))
                2 -> selectedFragment = AccountFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.main_frame, selectedFragment!!).commit()
            }
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

    override fun onStart() {
        super.onStart()
        updateDeviceToken()
    }

    @SuppressLint("HardwareIds")
    private fun updateDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val query = HashMap<String, Any>()
                        query["token"] = task.result!!.token
                        query["deviceID"] = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                        val apiService = ServiceBuilder.buildService(ApiService::class.java)
                        val requestCall = apiService.updatedevicetoken("Bearer ${Utils.currentUserID()}", query)
                        requestCall.enqueue(object : Callback<myelperresponse> {
                            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {

                            }

                            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                            }
                        })
                    }
                }
    }

    inner class uploadProfilePic : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            Toasty.info(this@MainActivity, "uploading", Toasty.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                val baos = ByteArrayOutputStream()
                profilePicBitmap!!.compress(Bitmap.CompressFormat.JPEG, 10, baos)
                val data = baos.toByteArray()
                val fileRef = profilePicRef!!.child("${Utils.currentUserID()}.jpg")
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
                        uploadAvatar()
                    }
                }
            } catch (e: Exception) {
            }
            return null
        }
    }

    private fun uploadAvatar() {
        val query = HashMap<String, Any>()
        query["avatar"] = mediaDownloadUrl
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updateavatar("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@MainActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@MainActivity, "User not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyTwo -> {
                            Utils.dismissLoader()
                            Toasty.error(this@MainActivity, "Account banned", Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSeventyOne -> {
                            Utils.dismissLoader()
                            Toasty.success(this@MainActivity, "Uploaded successfully", Toasty.LENGTH_LONG).show()
                        }
                        Constants.oneSixtyThree -> {
                            Utils.dismissLoader()
                            Toasty.error(this@MainActivity, "Your account has been suspended", Toasty.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@MainActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@MainActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
}
