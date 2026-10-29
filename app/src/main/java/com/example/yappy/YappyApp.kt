package com.example.yappy

import android.app.Application
import com.example.yappy.api.ApiClient

class YappyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}