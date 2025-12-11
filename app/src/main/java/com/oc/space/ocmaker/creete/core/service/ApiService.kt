package com.oc.space.ocmaker.creete.core.service
import com.oc.space.ocmaker.creete.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST189_SpaacceMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}