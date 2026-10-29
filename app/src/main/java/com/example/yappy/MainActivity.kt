package com.example.yappy

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.yappy.api.SessionManager
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var session: SessionManager
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageView
    private var isPasswordVisible: Boolean = false
    private lateinit var cbRememberMe: CheckBox
    private lateinit var forgotPass: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogle: ImageView
    private lateinit var signUp: TextView
    private var submittedEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        session = SessionManager(this)
        if (session.shouldAutoLogin()) {
            startActivity(Intent(this, homescreen::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        forgotPass = findViewById(R.id.forgotPass)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        signUp = findViewById(R.id.signUp)

        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnTogglePassword.setImageResource(R.drawable.ic_eye)
                btnTogglePassword.imageTintList = ColorStateList.valueOf(getColor(R.color.white))
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
                btnTogglePassword.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_border))
            }
            etPassword.setSelection(etPassword.text.length)
        }

        intent.getStringExtra(EXTRA_USER_EMAIL)?.let { etEmail.setText(it) }

        observeLogin()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                etEmail.error = "Please enter your email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Please enter your password"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            submittedEmail = email
            setLoading(true)
            viewModel.login(email, password)
        }

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        forgotPass.setOnClickListener {
            val currentEmail = etEmail.text.toString().trim()
            val intent = Intent(this, forgot::class.java).apply {
                if (currentEmail.isNotEmpty()) putExtra(EXTRA_USER_EMAIL, currentEmail)
            }
            startActivity(intent)
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Continuing as Demo User to preview layouts", Toast.LENGTH_SHORT).show()
            getSharedPreferences("user_session", MODE_PRIVATE).edit()
                .putString("name", "Demo User")
                .putString("email", "demo@yappy.app")
                .apply()
            startActivity(Intent(this, homescreen::class.java))
            finish()
        }
    }

    private fun observeLogin() {
        viewModel.loginResult.observe(this) { result ->
            setLoading(false)

            result.onSuccess { response ->
                session.saveTokens(response.accessToken, response.refreshToken)
                session.setRememberMe(cbRememberMe.isChecked)

                getSharedPreferences("user_session", MODE_PRIVATE).edit()
                    .putString("email", submittedEmail)
                    .apply()

                startActivity(Intent(this, homescreen::class.java))
                finish()
            }.onFailure { error ->
                val message = error.message ?: "Login failed. Please try again."

                if (message.contains("verify", ignoreCase = true)) {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, verify::class.java).apply {
                        putExtra("email", submittedEmail)
                    })
                } else {
                    etPassword.error = message
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Logging in..." else "Log in  ➔"
    }

    companion object {
        const val EXTRA_USER_EMAIL = "email"
    }
}