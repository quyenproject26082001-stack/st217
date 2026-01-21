package com.ocmaker.pixcel.maker.core.service
import com.ocmaker.pixcel.maker.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST193_PixaelMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}