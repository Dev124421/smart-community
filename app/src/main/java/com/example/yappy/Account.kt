package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yappy.api.SessionManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class Account : AppCompatActivity() {

    private lateinit var session: SessionManager

    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var accountName: TextView
    private lateinit var accountEmail: TextView
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var backButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        session = SessionManager(this)

        profileName = findViewById(R.id.profileUsername)
        profileEmail = findViewById(R.id.profileEmail)
        accountName = findViewById(R.id.accountUsername)
        accountEmail = findViewById(R.id.accountEmail)

        logoutButton = findViewById(R.id.logoutButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        backButton = findViewById(R.id.backButton)

        loadAccountDetails()

        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun loadAccountDetails() {
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)

        val name = preferences.getString("name", null)
        val email = preferences.getString("email", null)

        val displayName = if (name.isNullOrBlank()) "User" else name
        val displayEmail = if (email.isNullOrBlank()) "Not available" else email

        profileName.text = displayName
        profileEmail.text = if (email.isNullOrBlank()) "Account information unavailable" else email

        accountName.text = if (name.isNullOrBlank()) "Not available" else name
        accountEmail.text = displayEmail
    }

    private fun performLogout() {
        val refreshToken = session.getRefreshToken()

        // No token stored: nothing to tell the server, just clear and leave
        if (refreshToken.isNullOrBlank()) {
            finishLogout()
            return
        }

        logoutButton.isEnabled = false
        logoutButton.text = "Logging out..."

        thread {
            var code = -1
            try {
                val conn = (URL("https://smart-community-2.onrender.com/api/auth/logout")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 30000   // Render can be slow to wake up
                    readTimeout = 30000
                }

                val body = JSONObject().put("refreshToken", refreshToken).toString()
                conn.outputStream.use { it.write(body.toByteArray()) }

                code = conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {
                // Handled below: we still sign out locally
            }

            runOnUiThread {
                if (code !in 200..299) {
                    Toast.makeText(
                        this,
                        "Couldn't reach server, signed out locally",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                finishLogout()
            }
        }
    }

    private fun finishLogout() {
        // Clear tokens + remember-me flag, and the saved name/email
        session.clear()
        getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply()

        // Back to the login screen, wiping the back stack
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}