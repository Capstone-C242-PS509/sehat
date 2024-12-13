package com.example.testsehat.data

data class MentalDiseaseResponse(
    val message: String? = null,
    val status_code: Int? = null,
    val data: List<MentalDiseaseData>? = null
)

data class MentalDiseaseData(
    val mental_disease: String? = null,
    val count: Int? = null
)
