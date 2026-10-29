package com.example.yappy.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")   suspend fun register(@Body r: RegisterRequest): Response<ApiResponse>
    @POST("auth/verify-otp") suspend fun verifyOtp(@Body r: OtpRequest): Response<ApiResponse>
    @POST("auth/resend-otp") suspend fun resendOtp(@Body r: EmailRequest): Response<ApiResponse>
    @POST("auth/login")      suspend fun login(@Body r: LoginRequest): Response<LoginResponse>
    @POST("auth/logout")     suspend fun logout(@Body r: LogoutRequest): Response<ApiResponse>
}