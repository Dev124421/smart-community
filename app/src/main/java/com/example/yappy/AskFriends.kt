package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AskFriends : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ask_friends)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val communityId = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_ID).orEmpty()
        val serverName = intent.getStringExtra("SERVER_NAME")

        findViewById<TextView>(R.id.btn_skip)?.setOnClickListener {
            finishSetup(communityId, serverName)
        }

        findViewById<TextView>(R.id.tv_learn_more_prompt)?.setOnClickListener {
            Toast.makeText(this, "Privacy settings can be configured anytime in Account", Toast.LENGTH_SHORT).show()
        }

        val btnAllowFind = findViewById<Button>(R.id.btn_allow_find)
        btnAllowFind?.isClickable = true
        btnAllowFind?.isFocusable = true
        btnAllowFind?.setTextColor(getColor(R.color.white))
        btnAllowFind?.setOnClickListener {
            Toast.makeText(this, "Discovery enabled! Friends can find you by email.", Toast.LENGTH_SHORT).show()
            finishSetup(communityId, serverName)
        }
    }

    private fun finishSetup(communityId: String, serverName: String?) {
        if (!serverName.isNullOrBlank()) {
            val intent = Intent(this, ChannelListActivity::class.java).apply {
                putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, communityId)
                putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, serverName)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, homescreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        finish()
    }
}
