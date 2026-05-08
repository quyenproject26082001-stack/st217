package com.pony.avatar.ocmaker.core.service

import com.pony.avatar.ocmaker.core.utils.key.DomainKey
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitPreventive : BaseRetrofitHelper() {
    val api = Retrofit.Builder()
        .baseUrl(DomainKey.BASE_URL_PREVENTIVE)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(okHttpClient!!)
        .build()
        .create(ApiService::class.java)
}

open class BaseRetrofitHelper() {
    var okHttpClient: OkHttpClient? = null

    init {
        okHttpClient = OkHttpClient.Builder()
            .writeTimeout(4_500L, TimeUnit.MILLISECONDS)
            .readTimeout(4_500L, TimeUnit.MILLISECONDS)
            .build()
    }
}