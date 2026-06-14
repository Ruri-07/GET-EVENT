package com.get.events.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ── CHANGER selon l'environnement ─────────────────────────────────────────
    // Émulateur Android Studio :
    private const val BASE_URL_EMULATOR = "http://192.168.0.150:8080/"
    // Vrai téléphone sur même WiFi — remplacer par l'IP de la machine :
    private const val BASE_URL_WIFI     = "http://192.168.0.150:8080/"
    // Production Railway :
    private const val BASE_URL_PROD     = "https://get-backend.up.railway.app/"

    // ← Changer cette ligne selon la situation
    private const val BASE_URL = BASE_URL_EMULATOR

    /** URL publique du backend (sans slash final) — pour charger images/QR */
    const val BASE_HOST = BASE_URL

    fun mediaUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http")) path else "$BASE_HOST$path"
    }

    // Token JWT — mis à jour après login/register
    var authToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = authToken
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else original
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
