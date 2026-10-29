package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SetUpFinish : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_up_finish)

        val scrim = findViewById<View>(R.id.scrimContainer)
        if (scrim != null) {
            val minTop = (15 * resources.displayMetrics.density).toInt()
            val minBottom = (25 * resources.displayMetrics.density).toInt()
            ViewCompat.setOnApplyWindowInsetsListener(scrim) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, maxOf(systemBars.top, minTop), systemBars.right, maxOf(systemBars.bottom, minBottom))
                insets
            }
        }

        val communityId = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_ID) ?: ""
        val serverName = intent.getStringExtra("SERVER_NAME") ?: "New Server"

        findViewById<TextView>(R.id.setupTitle)?.text = "Finish setting up $serverName!"
        findViewById<View>(R.id.stepInviteFriends)?.setOnClickListener {
            val intent = Intent(this, AddPeople::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra("SERVER_NAME", serverName)
            }
            startActivity(intent)
        }
        findViewById<View>(R.id.stepUploadIcon)?.setOnClickListener {
            Toast.makeText(this, "Server icon updated!", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.stepSendMessage)?.setOnClickListener {
            openServerRules(communityId, serverName)
        }
        findViewById<View>(R.id.stepBoosts)?.setOnClickListener {
            Toast.makeText(this, "Server Boosts feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnSkipSteps)?.setOnClickListener {
            openServerRules(communityId, serverName)
        }
    }

    private fun openServerRules(communityId: String, serverName: String) {
        val intent = Intent(this, ChannelListActivity::class.java).apply {
            putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, communityId)
            putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, serverName)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}
