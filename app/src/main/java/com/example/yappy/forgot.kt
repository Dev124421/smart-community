package com.example.yappy

import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class forgot : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnReset: MaterialButton
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot)

        etEmail = findViewById(R.id.email)
        btnReset = findViewById(R.id.btnResetPassword)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)

        // Email carried over from the login screen (if the user had typed one)
        intent.getStringExtra(MainActivity.EXTRA_USER_EMAIL)?.let { carriedEmail ->
            if (carriedEmail.isNotBlank()) {
                etEmail.setText(carriedEmail)
                etEmail.setSelection(carriedEmail.length)   // cursor at the end
            }
        }

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // TODO: call the reset-password endpoint here once your backend provides one
            Toast.makeText(
                this,
                "Password reset isn't available yet. Please contact support.",
                Toast.LENGTH_LONG
            ).show()
        }

        tvBackToLogin.setOnClickListener { finish() }
    }
}