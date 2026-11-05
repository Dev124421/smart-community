package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateServer2 : AppCompatActivity() {

    private var serverTemplate: String = "Custom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_server2)

        serverTemplate = intent.getStringExtra("SERVER_TEMPLATE") ?: "Custom"

        val baseHorizontal = (25 * resources.displayMetrics.density).toInt()
        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left + baseHorizontal,
                maxOf(systemBars.top, minTop),
                systemBars.right + baseHorizontal,
                maxOf(systemBars.bottom, minBottom)
            )
            insets
        }

        val backButton = findViewById<ImageView>(R.id.btn_back)
        val friendsButton = findViewById<LinearLayout>(R.id.btn_for_friends)
        val communityButton = findViewById<LinearLayout>(R.id.btn_for_community)
        val skipButton = findViewById<TextView>(R.id.tv_skip_prompt)

        backButton.setOnClickListener {
            finish()
        }

        friendsButton.setOnClickListener {
            openFinalServerScreen("For Friends")
        }

        communityButton.setOnClickListener {
            openFinalServerScreen("For Community")
        }

        skipButton.setOnClickListener {
            openFinalServerScreen("Not specified")
        }
    }

    private fun openFinalServerScreen(serverType: String) {
        val intent = Intent(this, CreateServer3::class.java).apply {
            putExtra("SERVER_TEMPLATE", serverTemplate)
            putExtra("SERVER_TYPE", serverType)
        }
        startActivity(intent)
    }
}
