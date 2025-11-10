package com.example.appconsultas.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header // Importação necessária
import retrofit2.http.POST

interface ApiService {

    // Este é o ÚNICO endpoint que a sua API usa
    @POST("api/data")
    suspend fun consultarDados(
        @Header("Authorization") authHeader: String, // Recebe o token "Basic ..."
        @Body requestBody: ConsultaRequestBody
    ): List<ConsultaRecord>

}

object RetrofitClient {

    private const val BASE_URL = "http://85.209.93.16/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}