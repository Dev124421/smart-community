package com.example.yappy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private var accountMemberSince: TextView? = null
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: View
    private lateinit var backButton: View
    private var btnShareProfile: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        session = SessionManager(this)

        setupWindowInsets()

        profileName = findViewById(R.id.profileUsername)
        profileEmail = findViewById(R.id.profileEmail)
        accountName = findViewById(R.id.accountUsername)
        accountEmail = findViewById(R.id.accountEmail)
        accountUserId = findViewById(R.id.accountUserId)
        accountMemberSince = findViewById(R.id.accountMemberSince)

        logoutButton = findViewById(R.id.logoutButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        backButton = findViewById(R.id.backButton)
        btnShareProfile = findViewById(R.id.btnShareProfile)

        loadAccountDetails()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }

    private fun updateNotificationBadge() {
        val hasUnread = com.example.yappy.storage.NotificationStore.getUnreadCount(this) > 0
        findViewById<View>(R.id.badgeNotificationDotAccount)?.visibility =
            if (hasUnread) View.VISIBLE else View.GONE
    }

    private fun setupWindowInsets() {
        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.account_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                bars.left,
                maxOf(bars.top, minTop),
                bars.right,
                maxOf(bars.bottom, minBottom)
            )
            insets
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            showEditNameDialog()
        }

        findViewById<View>(R.id.rowAccountName)?.setOnClickListener {
            showEditNameDialog()
        }

        findViewById<View>(R.id.rowAccountUserId)?.setOnClickListener {
            copyUserIdToClipboard()
        }

        btnShareProfile?.setOnClickListener {
            shareProfile()
        }

        val openNotifications = {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<View>(R.id.btnNotification)?.setOnClickListener { openNotifications() }
        findViewById<View>(R.id.layoutBtnNotification)?.setOnClickListener { openNotifications() }
        findViewById<View>(R.id.rowNotifications)?.setOnClickListener { openNotifications() }

        findViewById<View>(R.id.rowHelpSupport)?.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        findViewById<View>(R.id.rowPrivacyPolicy)?.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        findViewById<View>(R.id.rowTermsConditions)?.setOnClickListener {
            startActivity(Intent(this, TermsConditionsActivity::class.java))
        }

        findViewById<View>(R.id.rowAppearance)?.setOnClickListener {
            Toast.makeText(this, "Theme: Dark Midnight (Default)", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.rowVoice)?.setOnClickListener {
            Toast.makeText(this, "Voice & Audio: Default system audio", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun loadAccountDetails() {
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val profiles = getSharedPreferences("user_profiles", MODE_PRIVATE)

        val email = preferences.getString("email", null)
        var name = preferences.getString("name", null)
        if (name.isNullOrBlank() && !email.isNullOrBlank()) {
            name = profiles.getString(email, null)
        }

        val displayName = when {
            !name.isNullOrBlank() -> name
            !email.isNullOrBlank() -> email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            else -> "Community Member"
        }
        val displayEmail = if (!email.isNullOrBlank()) email else "user@smartcommunity.io"

        profileName.text = displayName
        profileEmail.text = displayEmail

        accountName.text = displayName
        accountEmail.text = displayEmail

        val userHash = if (!name.isNullOrBlank()) Math.abs(name.hashCode() % 9000 + 1000) else 1024
        accountUserId?.text = "#$userHash"

        accountMemberSince?.text = "October 2026"
    }

    private fun copyUserIdToClipboard() {
        val tag = accountUserId?.text?.toString() ?: "#1024"
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Yappy Tag", tag)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied $tag to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareProfile() {
        val name = profileName.text.toString()
        val tag = accountUserId?.text?.toString() ?: ""
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Connect with me on Yappy")
            putExtra(
                Intent.EXTRA_TEXT,
                "Hey! Connect with me on Yappy: $name $tag\nJoin smart communities: https://smart-community-2.onrender.com"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share Profile"))
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
                    val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
                    val email = preferences.getString("email", null)
                    preferences.edit()
                        .putString("name", newName)
                        .apply()
                    if (!email.isNullOrBlank()) {
                        getSharedPreferences("user_profiles", MODE_PRIVATE).edit()
                            .putString(email, newName)
                            .apply()
                    }
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
