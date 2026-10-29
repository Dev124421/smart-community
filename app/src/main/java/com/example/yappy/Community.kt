package com.example.yappy.model

import androidx.annotation.DrawableRes

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val onlineCount: String,
    val membersCount: String,
    @DrawableRes val bannerRes: Int? = null
)