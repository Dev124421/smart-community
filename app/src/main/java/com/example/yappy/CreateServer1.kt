package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateServer1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_server1)

        val joinServer = findViewById<Button>(R.id.btn_join_server)

        val createOwn = findViewById<LinearLayout>(
            R.id.btn_create_my_own
        )

        val gaming = findViewById<LinearLayout>(
            R.id.btn_gaming
        )

        val schoolClub = findViewById<LinearLayout>(
            R.id.btn_school_club
        )

        val studyGroup = findViewById<LinearLayout>(
            R.id.btn_study_group
        )

        val friends = findViewById<LinearLayout>(
            R.id.btn_friends
        )

        val artists = findViewById<LinearLayout>(
            R.id.btn_artists_creators
        )

        val community = findViewById<LinearLayout>(
            R.id.btn_local_community
        )

        createOwn.setOnClickListener {
            openServerTypeScreen("Custom")
        }

        gaming.setOnClickListener {
            openServerTypeScreen("Gaming")
        }

        schoolClub.setOnClickListener {
            openServerTypeScreen("School Club")
        }

        studyGroup.setOnClickListener {
            openServerTypeScreen("Study Group")
        }

        friends.setOnClickListener {
            openServerTypeScreen("Friends")
        }

        artists.setOnClickListener {
            openServerTypeScreen("Artists & Creators")
        }

        community.setOnClickListener {
            openServerTypeScreen("Local Community")
        }

        joinServer.setOnClickListener {
            startActivity(Intent(this, AddPeople::class.java))
        }

        findViewById<ImageView>(R.id.btn_close)
            .setOnClickListener {
                finish()
            }
    }

    private fun openServerTypeScreen(template: String) {
        val intent = Intent(this, CreateServer2::class.java)

        intent.putExtra("SERVER_TEMPLATE", template)

        startActivity(intent)
    }
}

