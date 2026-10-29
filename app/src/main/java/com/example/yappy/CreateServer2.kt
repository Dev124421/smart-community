package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateServer2 : AppCompatActivity() {

    private var serverTemplate: String = "Custom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_server2)

        serverTemplate = intent.getStringExtra("SERVER_TEMPLATE")
            ?: "Custom"

        val backButton = findViewById<ImageView>(
            R.id.btn_back
        )

        val friendsButton = findViewById<LinearLayout>(
            R.id.btn_for_friends
        )

        val communityButton = findViewById<LinearLayout>(
            R.id.btn_for_community
        )

        val skipButton = findViewById<TextView>(
            R.id.tv_skip_prompt
        )

        // Handle system bar insets without replacing XML padding.
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Back to CreateServer1
        backButton.setOnClickListener {
            finish()
        }

        // Server for friends
        friendsButton.setOnClickListener {
            openCreateServerScreen("Friends")
        }

        // Server for a community
        communityButton.setOnClickListener {
            openCreateServerScreen("Community")
        }

        // Skip this question
        skipButton.setOnClickListener {
            openCreateServerScreen("Not specified")
        }
    }

    private fun openCreateServerScreen(serverType: String) {

        val intent = Intent(
            this,
            CreateServer3::class.java
        )

        intent.putExtra("SERVER_TEMPLATE", serverTemplate)
        intent.putExtra("SERVER_TYPE", serverType)

        startActivity(intent)
    }
}

