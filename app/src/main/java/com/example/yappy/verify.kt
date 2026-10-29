package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class verify : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var etCode: EditText
    private lateinit var btnVerify: MaterialButton
    private lateinit var tvResend: TextView
    private lateinit var tvBack: TextView
    private lateinit var tvSubtitle: TextView

    private var email: String = ""
    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify)

        etCode = findViewById(R.id.verification)
        btnVerify = findViewById(R.id.verifyBtn)
        tvResend = findViewById(R.id.tvResendCode)
        tvBack = findViewById(R.id.tvBackToLogin)
        tvSubtitle = findViewById(R.id.tvSubtitle)

        email = intent.getStringExtra("email").orEmpty()
        if (email.isEmpty()) {
            Toast.makeText(this, "Email missing. Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            return
        }

        tvSubtitle.text = "Enter the 6-digit code sent to $email"

        observeResults()

        // Coming from a blocked login: the old code may be expired, so send a fresh one
        if (intent.getBooleanExtra("autoResend", false)) {
            viewModel.resendOtp(email)
            startResendCooldown()
        }

        btnVerify.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.length != 6) {
                etCode.error = "Enter the 6-digit code"
                etCode.requestFocus()
                return@setOnClickListener
            }
            setLoading(true)
            viewModel.verifyOtp(email, code)
        }

        tvResend.setOnClickListener {
            if (!tvResend.isEnabled) return@setOnClickListener
            viewModel.resendOtp(email)
            startResendCooldown()
        }

        tvBack.setOnClickListener {goToLogin()}
    }

    private fun observeResults() {
        viewModel.verifyResult.observe(this) { result ->
            setLoading(false)
            result.onSuccess { response ->
                Toast.makeText(
                    this,
                    response.message ?: "Email verified.\nWelcome back",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, homescreen::class.java))
            }.onFailure { error ->
                val message = error.message ?: "Verification failed"
                etCode.error = message
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.resendResult.observe(this) { result ->
            result.onSuccess { response ->
                Toast.makeText(
                    this,
                    response.message ?: "A new code has been sent",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(this, error.message ?: "Could not resend code", Toast.LENGTH_LONG).show()
                resendTimer?.cancel()
                tvResend.isEnabled = true
                tvResend.text = "Resend"
            }
        }
    }

    private fun startResendCooldown() {
        resendTimer?.cancel()
        tvResend.isEnabled = false
        resendTimer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(msLeft: Long) {
                tvResend.text = "Resend (${msLeft / 1000}s)"
            }
            override fun onFinish() {
                tvResend.isEnabled = true
                tvResend.text = "Resend"
            }
        }.start()
    }

    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_USER_EMAIL, email)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        btnVerify.isEnabled = !isLoading
        btnVerify.text = if (isLoading) "Verifying..." else "Verify  ➔"
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        super.onDestroy()
    }
}