package com.example.testsehat.data


data class ApiRequest(
val text: String
)

data class ApiResponse(
    val predictions: String,
    val description: String,
    val first_aid: List<String>,
    val improvement_tips: List<String>
)

