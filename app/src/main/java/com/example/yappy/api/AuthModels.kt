package com.example.yappy.api

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class OtpRequest(val email: String, val otp: String)
data class EmailRequest(val email: String)
data class LogoutRequest(val refreshToken: String)

data class ApiResponse(val success: Boolean, val message: String?)
data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String?,
    val expiresIn: Int
)