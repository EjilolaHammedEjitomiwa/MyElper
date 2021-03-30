package com.dsceksu.myelper.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dsceksu.myelper.Adapter.ChatAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ChatModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


@Suppress("UNREACHABLE_CODE")
class ChatActivity : AppCompatActivity() {
    private var chatImageRef: StorageReference? = null
    private var chatImageBitman: Bitmap? = null
    private var mediaDownloadUrl: String = ""

    private var chatLits = ArrayList<ChatModel>()
    private var senderChatList = ArrayList<ChatModel>()
    private var recieverChatList = ArrayList<ChatModel>()
    private var adapter: ChatAdapter? = null

    private var recieverId: String? = null
    private var userSuspended = false
    private var userBanned = false

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chat_activity_toolbar.inflateMenu(R.menu.chat_activity_menu)
        chatImageRef = FirebaseStorage.getInstance().reference.child("chat images")
        recieverId = intent.getStringExtra(Constants.recieverID)

        adapter = ChatAdapter(this, chatLits)
        val layoutManager = LinearLayoutManager(this)
        chat_listView.layoutManager = layoutManager
        chat_listView.adapter = adapter

        loadRecieverIdDetails()

        chat_sendIcon.setOnClickListener { click ->
            val message = chat_sendEditText.text.toString()
            if (message.isEmpty()) {
                Toasty.info(this@ChatActivity, "message is empty", Toast.LENGTH_LONG, true).show()
            } else {
                chat_sendEditText.text.clear()
                when {
                    userBanned -> {
                        finishAndRemoveTask()
                        Toasty.error(this, "Account banned", Toasty.LENGTH_LONG).show()
                    }
                    userSuspended -> {
                        finishAndRemoveTask()
                        Toasty.error(this, "Account suspended", Toasty.LENGTH_LONG).show()
                    }
                    else -> {
                        sendChatMessage(message, "text")
                    }
                }

            }
        }
        chat_backIcon.setOnClickListener {
            finishAndRemoveTask()
        }
        chat_selectImage.setOnClickListener {
            when {
                userBanned -> {
                    finishAndRemoveTask()
                    Toasty.error(this, "Account banned", Toasty.LENGTH_LONG).show()
                }
                userSuspended -> {
                    finishAndRemoveTask()
                    Toasty.error(this, "Account suspended", Toasty.LENGTH_LONG).show()
                }
                else -> {
                    CropImage.activity().setAspectRatio(1, 1).start(this)
                }
            }

        }
        chat_activity_toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item!!.itemId) {
                    R.id.viewProduct -> {
                        val intent = Intent(this@ChatActivity, ActiveGig::class.java)
                        intent.putExtra("id", recieverId)
                        startActivity(intent)
                    }

                    R.id.viewService -> {
                        val intent = Intent(this@ChatActivity, MyServiceActivity::class.java)
                        intent.putExtra("id", recieverId)
                        startActivity(intent)
                    }

                    R.id.addToFav -> {
                        addSellerToFavourite()
                    }
                    R.id.removeFromFav -> {
                        removeSellerFromFavourite()
                    }
                }
                return true
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            val cropedImage = CropImage.getActivityResult(data)
            val categoryImageUri = cropedImage.uri
            chatImageBitman = MediaStore.Images.Media.getBitmap(this.contentResolver, categoryImageUri)
            uploadImages().execute()
        }
    }

    override fun onStart() {
        super.onStart()
        loadChats()
        isUserSuspendedOrBanned()
    }

    private fun isUserSuspendedOrBanned() {
        Utils.database()
                .collection(Constants.users)
                .document(Utils.currentUserID())
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(UsersModel::class.java)
                        userBanned = user!!.banned!!
                        userSuspended = user.suspended!!
                    }
                }
    }

    private fun addSellerToFavourite() {
        val query = HashMap<String, Any>()
        query["sellerID"] = recieverId!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.addfavseller("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ChatActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@ChatActivity, "User not found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtySix -> {
                            Toasty.success(this@ChatActivity, "Seller added to your favourites, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ChatActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ChatActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun removeSellerFromFavourite() {
        val query = HashMap<String, Any>()
        query["sellerID"] = recieverId!!
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.removefavseller("Bearer ${Utils.currentUserID()}", query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when (res!!.code) {
                        Constants.oneO3 -> {
                            Toasty.error(this@ChatActivity, "Invalid arguments", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneTwentyFive -> {
                            Toasty.error(this@ChatActivity, "No user found", Toast.LENGTH_LONG).show()
                        }
                        Constants.oneThirtySeven -> {
                            Toasty.success(this@ChatActivity, "Seller removed from your favourite, HAPPY SHOPPING", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toasty.error(this@ChatActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Toasty.error(this@ChatActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }

    private fun loadChats() {
        Utils.database().collection("chats")
                .whereEqualTo("from", Utils.currentUserID())
                .whereEqualTo("to", recieverId)
                .addSnapshotListener(this) { value, error ->
                    if (!value!!.isEmpty) {
                        senderChatList.clear()
                        for (data in value) {
                            val chats = data.toObject(ChatModel::class.java)
                            senderChatList.add(chats)
                            displayChats()
                        }
                    }
                }

        Utils.database().collection("chats")
                .whereEqualTo("from", recieverId)
                .whereEqualTo("to", Utils.currentUserID())
                .addSnapshotListener(this) { value, error ->
                    if (!value!!.isEmpty) {
                        recieverChatList.clear()
                        for (data in value) {
                            val chats = data.toObject(ChatModel::class.java)
                            recieverChatList.add(chats)
                            displayChats()
                        }
                    }
                }

    }

    private fun displayChats() {
        chatLits.clear()
        chatLits.addAll(recieverChatList)
        chatLits.addAll(senderChatList)
        chatLits.sortBy { it.time }
        adapter!!.notifyDataSetChanged()
        chat_listView.scrollToPosition(chatLits.size - 1)
    }

    private fun loadRecieverIdDetails() {
        Utils.database()
                .collection(Constants.users)
                .document(recieverId!!)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val reciever = it.toObject(UsersModel::class.java)
                        chat_username.text = reciever!!.username
                        Glide.with(this@ChatActivity).load(reciever.avatar).into(chat_profileImage)
                    }
                }
    }

    private fun sendChatMessage(message: String, type: String) {
        val batch = Utils.database().batch()
        val time = System.currentTimeMillis()

        val messagesMap = HashMap<String, Any>()
        messagesMap["time"] = time
        messagesMap["to"] = recieverId!!
        messagesMap["from"] = Utils.currentUserID()
        messagesMap["message"] = message
        messagesMap["type"] = type

        val chatRef = Utils.database().collection("chats").document(Utils.databaseRef().push().key.toString())
        batch.set(chatRef, messagesMap)

        val senderChatListMap = HashMap<String, Any>()
        senderChatListMap["time"] = time
        senderChatListMap["userID"] = recieverId!!
        senderChatListMap["to"] = recieverId!!
        if (type == "image") {
            senderChatListMap["message"] = "sent image"
        } else {
            senderChatListMap["message"] = message
        }

        val senderChatListRef = Utils.database().collection("chatlists").document(Utils.currentUserID()).collection(Utils.currentUserID()).document(recieverId!!)
        batch.set(senderChatListRef, senderChatListMap)

        val recieverChatListMap = HashMap<String, Any>()
        recieverChatListMap["time"] = time
        recieverChatListMap["userID"] = Utils.currentUserID()
        recieverChatListMap["to"] = recieverId!!
        if (type == "image") {
            recieverChatListMap["message"] = "sent image"
        } else {
            recieverChatListMap["message"] = message
        }

        val recieverChatListRef = Utils.database().collection("chatlists").document(recieverId!!).collection(recieverId!!).document(Utils.currentUserID())
        batch.set(recieverChatListRef, recieverChatListMap)

        batch.commit()
                .addOnSuccessListener {
                    //send notification here
                    loadChats()
                }

    }

    inner class uploadImages : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                val baos = ByteArrayOutputStream()
                chatImageBitman!!.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                val data = baos.toByteArray()
                val fileRef = chatImageRef!!.child("${System.currentTimeMillis()}.jpg")
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
                        sendChatMessage(mediaDownloadUrl, "image")
                    }
                }
            } catch (e: Exception) {
            }
            return null
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.userInChatActivity(this)
        Utils.setUserOnline()

    }

    override fun onPause() {
        super.onPause()
        Utils.userLeftChatActivity(this)
        Utils.setUserOffline()
    }

}