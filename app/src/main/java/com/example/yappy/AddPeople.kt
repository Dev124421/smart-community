package com.example.yappy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddPeople : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_people)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val communityId = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_ID).orEmpty()
        val serverName = intent.getStringExtra("SERVER_NAME") ?: "Community"
        val inviteLink = "https://yappy.app/invite/${serverName.lowercase().replace(" ", "")}"

        val tvInviteUrl = findViewById<TextView>(R.id.tv_invite_url)
        tvInviteUrl?.text = inviteLink

        findViewById<TextView>(R.id.btn_skip)?.setOnClickListener {
            openNextScreen(communityId, serverName)
        }

        findViewById<ImageView>(R.id.btn_copy_link)?.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Yappy Invite", inviteLink)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Invite link copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_share_link)?.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Join my server on Yappy: $inviteLink")
            }
            startActivity(Intent.createChooser(sendIntent, "Share server invite"))
            openNextScreen(communityId, serverName)
        }
    }

    private fun openNextScreen(communityId: String, serverName: String) {
        val intent = Intent(this, AskFriends::class.java).apply {
            putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
            putExtra("SERVER_NAME", serverName)
        }
        startActivity(intent)
        finish()
    }
}
