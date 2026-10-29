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

        backButton.setOnClickListener {
            finish()
        }

        friendsButton.setOnClickListener {
            openCreateServerScreen("Friends")
        }

        communityButton.setOnClickListener {
            openCreateServerScreen("Community")
        }

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

