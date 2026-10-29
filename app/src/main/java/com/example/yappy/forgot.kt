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

        intent.getStringExtra(MainActivity.EXTRA_USER_EMAIL)?.let { carriedEmail ->
            if (carriedEmail.isNotBlank()) {
                etEmail.setText(carriedEmail)
                etEmail.setSelection(carriedEmail.length)
            }
        }

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            Toast.makeText(
                this,
                "Verification code sent to $email",
                Toast.LENGTH_SHORT
            ).show()
            val verifyIntent = android.content.Intent(this, verify::class.java).apply {
                putExtra("email", email)
            }
            startActivity(verifyIntent)
        }

        tvBackToLogin.setOnClickListener { finish() }
    }
}