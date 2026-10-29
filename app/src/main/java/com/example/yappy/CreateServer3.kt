package com.example.yappy

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateServer3 : AppCompatActivity() {

    private var selectedIcon = R.drawable.add_icon

    private var serverTemplate: String = "Custom"
    private var serverType: String = "Not specified"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_server3)

        serverTemplate = intent.getStringExtra("SERVER_TEMPLATE")
            ?: "Custom"

        serverType = intent.getStringExtra("SERVER_TYPE")
            ?: "Not specified"

        val backButton = findViewById<ImageView>(
            R.id.back_arrow
        )

        val addIcon = findViewById<ImageView>(
            R.id.add_icon
        )

        val gamingIcon = findViewById<ImageView>(
            R.id.gaming_icon
        )

        val serverName = findViewById<EditText>(
            R.id.server_name
        )

        val createButton = findViewById<Button>(
            R.id.createBtn
        )

        val guidelines = findViewById<TextView>(
            R.id.guidelines
        )

        // Handle system bar insets.
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

        // Go back to CreateServer2
        backButton.setOnClickListener {
            finish()
        }

        // Select the default icon
        addIcon.setOnClickListener {
            selectedIcon = R.drawable.add_icon

            Toast.makeText(
                this,
                "Default server icon selected",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Select the gaming icon
        gamingIcon.setOnClickListener {
            selectedIcon = R.drawable.gaming2

            Toast.makeText(
                this,
                "Gaming icon selected",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Open community guidelines
        guidelines.setOnClickListener {
            Toast.makeText(
                this,
                "Community Guidelines",
                Toast.LENGTH_SHORT
            ).show()

            // Open a guidelines screen or webpage here.
        }

        // Create server
        createButton.setOnClickListener {

            val name = serverName.text.toString().trim()

            if (name.isEmpty()) {
                serverName.error = "Please enter a server name"
                serverName.requestFocus()
                return@setOnClickListener
            }

            // Server details are ready for saving.
            val result = """
                Server Name: $name
                Template: $serverTemplate
                Server Type: $serverType
                Icon Resource: $selectedIcon
            """.trimIndent()

            Toast.makeText(
                this,
                "Server '$name' created successfully!",
                Toast.LENGTH_LONG
            ).show()

            // Return the created server information to the caller.
            intent.putExtra("SERVER_NAME", name)
            intent.putExtra("SERVER_TEMPLATE", serverTemplate)
            intent.putExtra("SERVER_TYPE", serverType)
            intent.putExtra("SERVER_ICON", selectedIcon)

            setResult(RESULT_OK, intent)

            finish()
        }
    }
}

