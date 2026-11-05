package com.example.yappy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HelpSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help_support)

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.support_root)) { v, insets ->
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

        findViewById<TextView>(R.id.tvSupportEmail)?.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:appdevtrial2007@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Yappy Support Request")
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Send Email"))
            } catch (_: Exception) {
            }
        }
    }
}
