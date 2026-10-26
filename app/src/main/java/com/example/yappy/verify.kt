package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class verify : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify)

        val intent1 = findViewById<Button>(R.id.verifyBtn)
        intent1.setOnClickListener {
            val intent = Intent(this, homescreen::class.java)
            startActivity(intent)
        }
    }
}