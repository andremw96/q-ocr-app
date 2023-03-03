package com.example.qocrapp.di

import com.example.qocrapp.BuildConfig
import com.example.qocrapp.api.ChatGptService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun provideBaseUrl() = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val tokenInterceptor = Interceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.CHATGPT_API_KEY}")
                .build()
            chain.proceed(newRequest)
        }

        val logginInterceptor = HttpLoggingInterceptor()
        logginInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logginInterceptor)
            .addInterceptor(tokenInterceptor)
            .connectTimeout(30L, TimeUnit.MINUTES)
            .readTimeout(30L, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitClient(client: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideChatGptService(retrofit: Retrofit): ChatGptService {
        return retrofit.create(ChatGptService::class.java)
    }
}
