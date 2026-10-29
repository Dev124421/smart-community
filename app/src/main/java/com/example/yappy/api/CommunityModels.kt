package com.example.yappy.api

import com.google.gson.annotations.SerializedName

/**
 * Models matching the Smart Community Postman API collection.
 */

data class GetCommunitiesResponse(
    val success: Boolean,
    val count: Int = 0,
    val communities: List<ApiCommunityDto> = emptyList()
)

data class ApiCommunityDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val owner: String? = null,
    val memberCount: Int = 0,
    val isMember: Boolean = false,
    val createdAt: String? = null
)

data class CreateCommunityRequest(
    val name: String,
    val description: String
)

data class CreateCommunityResponse(
    val success: Boolean,
    val message: String? = null,
    val community: ApiCommunityDto? = null
)

data class JoinCommunityResponse(
    val success: Boolean,
    val message: String? = null
)

data class GetChannelsResponse(
    val success: Boolean,
    val count: Int = 0,
    val channels: List<ChannelDto> = emptyList()
)

data class ChannelDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String? = null,
    val community: String? = null,
    val createdBy: String? = null,
    val type: String = "text",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreateChannelRequest(
    val name: String,
    val description: String
)

data class ChannelResponse(
    val success: Boolean,
    val message: String? = null,
    val channel: ChannelDto? = null
)

data class UpdateChannelRequest(
    val name: String? = null,
    val description: String? = null
)
