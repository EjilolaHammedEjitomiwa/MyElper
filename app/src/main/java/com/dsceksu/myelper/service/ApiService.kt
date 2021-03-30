package com.dsceksu.paybills.service

import com.dsceksu.myelper.Models.bankLists.Banks
import com.dsceksu.myelper.Models.myelperresponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("savesettlementaccount")
    fun savesettlementaccount(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("savenewuser")
    fun saveNewUser(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("verifyotp")
    fun verifyOtp(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("resendotp")
    fun resendOTP(@Header("Authorization") authkey:String): Call<myelperresponse>
    @POST("logginguser")
    fun logginguser(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("addwishlist")
    fun addwishlist(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("updateavatar")
    fun updateavatar(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("addtocart")
    fun addtocart(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @DELETE("deletefromcart")
    fun deletefromcart(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @PUT("updatecartqty")
    fun updatecartqty(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("followseller")
    fun followseller(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @DELETE("unfollowseller")
    fun unfollowseller(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("addfavouriteseller")
    fun addfavseller(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("productviews")
    fun updateproductviews(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @DELETE("removewishlist")
    fun removewishlist(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @DELETE("removefavseller")
    fun removefavseller(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("uploadproduct")
    fun uploadproduct(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("completeorder")
    fun completeorder(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("setshippingaddress")
    fun setshippingaddress(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("recieveorder")
    fun recieveorder(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("shippedorder")
    fun shippedorder(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("deliverorder")
    fun deliverorder(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("rateproduct")
    fun rateproduct(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("confirmorder")
    fun confirmorder(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("cancelorder")
    fun cancelorder(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("startproductpurchase")
    fun startproductpurchase(@Header("Authorization") authkey:String): Call<myelperresponse>
    @POST("paysponsorproduct")
    fun paysponsorproduct(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("buyadslot")
    fun buyadslot(@Header("Authorization") authkey:String): Call<myelperresponse>
    @POST("paysponsorpost")
    fun paysponsorpost(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("completesponsoredproduct")
    fun completesponsoredproduct(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("completesponsoredpost")
    fun completesponsoredpost(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("completeadslotpurchase")
    fun completeadslotpurchase(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("submitkyc")
    fun submitkyc(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("updatedevicetoken")
    fun updatedevicetoken(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @POST("updateuserstatus")
    fun updateuserstatus(@Header("Authorization") authkey:String, @QueryMap queryDetails:HashMap<String,Any>): Call<myelperresponse>
    @GET("banklists")
    fun getBankList(@Header("Authorization") authkey:String): Call<Banks>

}