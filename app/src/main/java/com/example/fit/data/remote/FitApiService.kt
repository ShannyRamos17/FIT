package com.example.fit.data.remote

import com.example.fit.ui.viewmodel.RunItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FitApiService {

    @GET("runs")
    suspend fun getRuns(@Query("usuario") usuario: String): List<RunItem>

    @POST("runs")
    suspend fun createRun(@Body run: RunItem): RunItem

    @DELETE("runs/{id}")
    suspend fun deleteRun(@Path("id") id: Int)

    @PUT("runs/{id}")
    suspend fun updateRun(@Path("id") id: Int, @Body run: RunItem): RunItem
}