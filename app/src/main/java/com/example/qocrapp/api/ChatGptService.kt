package com.example.qocrapp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatGptService {
    @POST("completions")
    suspend fun completions(
        @Body request: CompletionRequest
    ): Response<CompletionResponse>
}
