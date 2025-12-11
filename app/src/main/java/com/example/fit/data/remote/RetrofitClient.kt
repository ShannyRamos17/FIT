    package com.example.fit.data.remote

    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory

    object RetrofitClient {

        private const val BASE_URL = "https://693a7e659b80ba7262ca23bb.mockapi.io/"

        val apiService: FitApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FitApiService::class.java)
        }
    }