package com.example.qocrapp.api

import com.google.gson.annotations.SerializedName

data class CompletionRequest(
    @SerializedName("frequency_penalty")
    val frequencyPenalty: Int = 2,

    @SerializedName("max_tokens")
    val maxTokens: Int = 512,

    @SerializedName("model")
    val model: String = "text-davinci-003",

    @SerializedName("presence_penalty")
    val presencePenalty: Int = 0,

    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("temperature")
    val temperature: Double = 0.2,

    @SerializedName("top_p")
    val topP: Int = 1
)
