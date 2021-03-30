package com.dsceksu.myelper.activities

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.dsceksu.myelper.Models.myelperresponse
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.paybills.service.ApiService
import com.dsceksu.paybills.service.ServiceBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_submit_k_y_c.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class SubmitKYCActivity : AppCompatActivity() {

    private var idImageRef: StorageReference? = null
    private var idImageBitmap: Bitmap? = null
    private var idImageUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_k_y_c)

        idImageRef = FirebaseStorage.getInstance().reference.child("kyc id images")

        submit_kyc_chooseImage.setOnClickListener {
            CropImage.activity().setAspectRatio(2, 1).start(this)
        }

        submit_kyc_submitBtn.setOnClickListener {
            when{
                idImageBitmap == null -> Toasty.info(this,"Please select your id image",Toasty.LENGTH_LONG).show()
                !submit_kyc_chkBox.isChecked -> Toasty.info(this,"Please declare that you are submitting the correct details",Toasty.LENGTH_LONG).show()
                else ->{
                    Utils.showLoader(this,"Submitting...")
                    uploadIDImage()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            val cropedImage = CropImage.getActivityResult(data)
            val categoryImageUri = cropedImage.uri
            idImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, categoryImageUri)
            submit_kyc_image.setImageBitmap(idImageBitmap)
        }
    }

    private fun uploadIDImage() {
        try {
            val baos = ByteArrayOutputStream()
            idImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            val data = baos.toByteArray()
            val fileRef = idImageRef!!.child("${System.currentTimeMillis()}.jpg")
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
                    idImageUrl = task.result.toString()
                    submitKYC()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun submitKYC() {
        val query = HashMap<String,Any>()
        query["idImage"] = idImageUrl
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.submitkyc("Bearer ${Utils.currentUserID()}",query)
        requestCall.enqueue(object : Callback<myelperresponse> {
            override fun onResponse(call: Call<myelperresponse>, response: Response<myelperresponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    when(res!!.code){
                        Constants.oneO3 ->{
                            Utils.dismissLoader()
                            Toasty.error(this@SubmitKYCActivity,"Invalid arguments", Toast.LENGTH_LONG).show()
                        }

                        Constants.oneSixtyOne ->{
                            Utils.dismissLoader()
                            finishAndRemoveTask()
                            Toasty.success(this@SubmitKYCActivity, "KYC info submitted successfully, your account is pending activation", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Utils.dismissLoader()
                    Toasty.error(this@SubmitKYCActivity, "error occur", Toasty.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<myelperresponse>, t: Throwable) {
                Utils.dismissLoader()
                Toasty.error(this@SubmitKYCActivity, "error occur : ${t.message}", Toasty.LENGTH_LONG).show()
            }
        })
    }
}