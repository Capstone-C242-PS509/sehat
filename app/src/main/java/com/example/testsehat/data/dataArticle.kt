package com.example.testsehat.data

data class dataArticle(
    val id: String,
    val title: String,
    val content: String,
    val creator: String,
    val tag: String,
    val createdAt: String,
    val url: String
)

data class ArticleResponse(
    val message: String,
    val status_code: Int,
    val data: List<dataArticle>
)