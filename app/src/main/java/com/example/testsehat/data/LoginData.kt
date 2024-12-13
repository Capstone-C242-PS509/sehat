package com.example.testsehat.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

data class AuthResponse(
    val message: String,
    val status_code: Int,
    val data: TokenData
)

data class TokenData(
    val token: String,
    val role: String
)
