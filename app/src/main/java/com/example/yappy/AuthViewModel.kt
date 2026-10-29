package com.example.yappy

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import com.example.yappy.api.*

class AuthViewModel : ViewModel() {

    val registerResult = MutableLiveData<Result<ApiResponse>>()
    val verifyResult = MutableLiveData<Result<ApiResponse>>()
    val resendResult = MutableLiveData<Result<ApiResponse>>()
    val loginResult = MutableLiveData<Result<LoginResponse>>()
    val logoutResult = MutableLiveData<Result<ApiResponse>>()

    private val api = ApiClient.authApi

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        registerResult.value = safeCall { api.register(RegisterRequest(name, email, password)) }
    }

    fun verifyOtp(email: String, otp: String) = viewModelScope.launch {
        verifyResult.value = safeCall { api.verifyOtp(OtpRequest(email, otp)) }
    }

    fun resendOtp(email: String) = viewModelScope.launch {
        resendResult.value = safeCall { api.resendOtp(EmailRequest(email)) }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        loginResult.value = safeCall { api.login(LoginRequest(email, password)) }
    }

    fun logout(refreshToken: String) = viewModelScope.launch {
        logoutResult.value = safeCall { api.logout(LogoutRequest(refreshToken)) }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): Result<T> {
        return try {
            val res = block()
            val body = res.body()
            if (res.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception(parseError(res)))
        } catch (e: IOException) {
            Result.failure(Exception("Can't reach the server. Check your internet and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(res: Response<*>): String = try {
        Gson().fromJson(res.errorBody()?.string(), ApiResponse::class.java)?.message
            ?: "Error ${res.code()}"
    } catch (e: Exception) {
        "Error ${res.code()}"
    }
}