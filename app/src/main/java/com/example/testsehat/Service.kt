package com.example.testsehat
import com.example.testsehat.data.ApiRequest
import com.example.testsehat.data.ApiResponse
import com.example.testsehat.data.ArticleResponse
import com.example.testsehat.data.AuthResponse
import com.example.testsehat.data.LoginRequest
import com.example.testsehat.data.MentalDiseaseData
import com.example.testsehat.data.MentalDiseaseResponse
import com.example.testsehat.data.RegisterRequest
import com.example.testsehat.data.dataMusic
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface Service {
    @Headers("Content-Type: application/json")
    @POST("predict/")
    fun getPrediction(@Body request: ApiRequest): Call<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("user/login")
    fun login(@Body loginRequest: LoginRequest): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("user/register")
    fun register(@Body registerRequest: RegisterRequest): Call<AuthResponse>

    @GET("articles/")
    fun getArticles(): Call<ArticleResponse>

    @GET("predictions/summary")
    fun getMentalDiseases(): Call<MentalDiseaseResponse>

    @Headers("Content-Type: application/json")
    @POST("predictions")
    fun postSummary(@Body mentalSummary : MentalDiseaseData):Call<ApiResponse>

    @GET("music")
    fun getMusic(@Query("mood") mood: String): Call<List<dataMusic>>

}

object RetrofitClient {
    val baseUrl: String by lazy { BuildConfig.BASE_URL}

    val instance: Service by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(Service::class.java)
    }
}

object RetrofitClientModel {
    val modelUrl: String by lazy { BuildConfig.MDL_URL}

    val instance: Service by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(modelUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(Service::class.java)
    }
}