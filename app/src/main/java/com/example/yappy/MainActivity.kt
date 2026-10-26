package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRememberMe: CheckBox
    private lateinit var forgotPass: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogle: ImageView
    private lateinit var signUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        forgotPass = findViewById(R.id.forgotPass)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        signUp = findViewById(R.id.signUp)

        btnLogin.setOnClickListener {
            val userEmail = etEmail.text.toString().trim()
            val userPassword = etPassword.text.toString().trim()

            if (userEmail.isEmpty()) {
                etEmail.error = "Please enter your email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (userPassword.isEmpty()) {
                etPassword.error = "Please enter your password"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (userPassword.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            AuthApi.login(userEmail, userPassword, object : AuthApi.AuthCallback {
                override fun onSuccess(token: String, username: String) {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Log in  ➔"

                    Toast.makeText(this@MainActivity, "Authenticated via Demo API!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, homescreen::class.java).apply {
                        putExtra(EXTRA_USERNAME, username)
                        putExtra(EXTRA_USER_EMAIL, userEmail)
                        putExtra(EXTRA_AUTH_TOKEN, token)
                        putExtra(EXTRA_REMEMBER_ME, cbRememberMe.isChecked)
                    }
                    startActivity(intent)
                    finish()
                }

                override fun onError(errorMessage: String) {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Log in  ➔"
                    etPassword.error = errorMessage
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            })
        }

        signUp.setOnClickListener {
            Toast.makeText(this, "Navigate to Sign Up screen", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        forgotPass.setOnClickListener {
            val currentEmail = etEmail.text.toString().trim()
            Toast.makeText(this, "Navigate to Forgot Password", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, forgot::class.java).apply {
                if (currentEmail.isNotEmpty()) putExtra(EXTRA_USER_EMAIL, currentEmail)
            }
            startActivity(intent)
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-In Triggered", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        const val EXTRA_USERNAME = "username"
        const val EXTRA_USER_EMAIL = "email"
        const val EXTRA_AUTH_TOKEN = "auth_token"
        const val EXTRA_REMEMBER_ME = "remember_me"
    }
}
object Auth {
    interface AuthCallback {
        fun onSuccess(token: String, username: String)
        fun onError(errorMessage: String)
    }

    fun login(email: String, pass: String, callback: AuthCallback) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (pass == "password123" || !email.contains("fail")) {
                callback.onSuccess(
                    token = "demo_jwt_token_12345",
                    username = email.substringBefore("@")
                )
            } else {
                callback.onError("Invalid credentials. Please try again.")
            }
        }, 1200)
    }
}