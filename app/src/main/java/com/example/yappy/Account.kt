package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private var accountUserId: TextView? = null
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
        accountUserId = findViewById(R.id.accountUserId)

        logoutButton = findViewById(R.id.logoutButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        backButton = findViewById(R.id.backButton)

        loadAccountDetails()

        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            showEditNameDialog()
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun loadAccountDetails() {
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)

        val name = preferences.getString("name", null)
        val email = preferences.getString("email", null)

        val displayName = if (!name.isNullOrBlank()) name else "User"
        val displayEmail = if (!email.isNullOrBlank()) email else "Not available"

        profileName.text = displayName
        profileEmail.text = displayEmail

        accountName.text = displayName
        accountEmail.text = displayEmail

        accountUserId?.let {
            val userHash = if (!name.isNullOrBlank()) Math.abs(name.hashCode() % 9000 + 1000) else 1024
            it.text = "#$userHash"
        }
    }

    private fun showEditNameDialog() {
        val input = EditText(this).apply {
            setText(profileName.text)
            setSelection(text.length)
            setSingleLine()
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Profile Name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    getSharedPreferences("user_session", MODE_PRIVATE).edit()
                        .putString("name", newName)
                        .apply()
                    loadAccountDetails()
                    Toast.makeText(this, "Profile updated to $newName", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        val refreshToken = session.getRefreshToken()

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
                    connectTimeout = 30000
                    readTimeout = 30000
                }

                val body = JSONObject().put("refreshToken", refreshToken).toString()
                conn.outputStream.use { it.write(body.toByteArray()) }

                code = conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {
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
        JoinedCommunityManager.clearCurrent(this)
        session.clear()
        getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply()

        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
