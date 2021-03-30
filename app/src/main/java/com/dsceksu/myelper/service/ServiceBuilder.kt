package com.dsceksu.paybills.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
object ServiceBuilder {
    //url of the server
    private const val URL = "https://us-central1-myhelper-dsceksu.cloudfunctions.net/app/api/"

    //creating logger
    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    //http client
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(logger).connectTimeout(540, TimeUnit.SECONDS).readTimeout(540, TimeUnit.SECONDS)
    //create the builder
    private val builder = Retrofit.Builder().baseUrl(URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient.build())
    
    //create instance of retrofit
    private val retrofit = builder.build()

    fun <T> buildService(serviceType: Class<T>): T {
        return retrofit.create(serviceType)
    }
}