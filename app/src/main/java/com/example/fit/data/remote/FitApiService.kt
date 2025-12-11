package com.example.fit.data.remote

import com.example.fit.ui.viewmodel.RunItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query // <--- IMPORTANTE: Importar Query

interface FitApiService {

    // 1. OBTENER (READ): CORREGIDO
    // Usamos "runs" (el nombre real en MockAPI)
    // Usamos @Query para filtrar: URL final será ".../runs?usuario=Shanny"
    @GET("runs")
    suspend fun getRuns(@Query("usuario") usuario: String): List<RunItem>

    // 2. CREAR (CREATE): CORREGIDO
    // Cambiamos "recorridos" por "runs" para que coincida con tu MockAPI
    @POST("runs")
    suspend fun createRun(@Body run: RunItem): RunItem

    // 3. BORRAR: ESTABA BIEN (pero confirma que sea "runs")
    @DELETE("runs/{id}")
    suspend fun deleteRun(@Path("id") id: Int)

    @PUT("runs/{id}")
    suspend fun updateRun(@Path("id") id: Int, @Body run: RunItem): RunItem
}