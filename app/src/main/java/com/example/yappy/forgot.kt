package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class forgot : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot)

        val inetnt1 = findViewById<Button>(R.id.btnResetPassword)
        inetnt1.setOnClickListener {
            val intent = Intent(this, verify::class.java)
            startActivity(intent)
        }
    }
}