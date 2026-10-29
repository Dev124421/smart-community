package com.example.yappy

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class SignUpActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageView
    private var isPasswordVisible: Boolean = false
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignUp: MaterialButton
    private lateinit var btnGoogle: ImageView
    private lateinit var btnApple: ImageView
    private lateinit var login: TextView

    private var submittedName: String = ""
    private var submittedEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        cbTerms = findViewById(R.id.cbTerms)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnApple = findViewById(R.id.btnApple)
        login = findViewById(R.id.login)

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

        observeRegister()

        btnSignUp.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (fullName.isEmpty()) {
                etFullName.error = "Please enter your full name"
                etFullName.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please accept the Terms of Service & Privacy Policy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submittedName = fullName
            submittedEmail = email
            getSharedPreferences("user_session", MODE_PRIVATE).edit()
                .putString("name", fullName)
                .putString("email", email)
                .apply()

            setLoading(true)
            viewModel.register(fullName, email, password)
        }

        login.setOnClickListener { finish() }

        btnGoogle.setOnClickListener {
            val typedName = etFullName.text.toString().trim()
            val name = if (typedName.isNotEmpty()) typedName else "Demo User"
            val typedEmail = etEmail.text.toString().trim()
            val email = if (typedEmail.isNotEmpty()) typedEmail else "demo@yappy.app"
            getSharedPreferences("user_session", MODE_PRIVATE).edit()
                .putString("name", name)
                .putString("email", email)
                .apply()
            startActivity(Intent(this, homescreen::class.java))
            finish()
        }

        btnApple.setOnClickListener {
            val typedName = etFullName.text.toString().trim()
            val name = if (typedName.isNotEmpty()) typedName else "Demo User"
            val typedEmail = etEmail.text.toString().trim()
            val email = if (typedEmail.isNotEmpty()) typedEmail else "demo@yappy.app"
            getSharedPreferences("user_session", MODE_PRIVATE).edit()
                .putString("name", name)
                .putString("email", email)
                .apply()
            startActivity(Intent(this, homescreen::class.java))
            finish()
        }
    }

    private fun observeRegister() {
        viewModel.registerResult.observe(this) { result ->
            setLoading(false)
            result.onSuccess { response ->
                Toast.makeText(
                    this,
                    response.message ?: "Registered. Please verify your email.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, verify::class.java).apply {
                    putExtra("name", submittedName)
                    putExtra("email", submittedEmail)
                }
                startActivity(intent)
                finish()
            }.onFailure { error ->
                Toast.makeText(this, error.message ?: "Something went wrong", Toast.LENGTH_LONG).show()
                val message = error.message.orEmpty()
                if (message.contains("server", ignoreCase = true) || message.contains("internet", ignoreCase = true)) {
                    Toast.makeText(this, "Opening verification preview...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, verify::class.java).apply {
                        putExtra("name", submittedName)
                        putExtra("email", submittedEmail)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnSignUp.isEnabled = !isLoading
        btnSignUp.text = if (isLoading) "Creating Account..." else "Sign Up  âž”"
    }
}
