package com.example.yappy

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_policy)

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.privacy_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                bars.left,
                maxOf(bars.top, minTop),
                bars.right,
                maxOf(bars.bottom, minBottom)
            )
            insets
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
