package com.example.yappy.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface representing the 8 endpoints in the Community Postman Collection.
 */
interface CommunityApiService {

    /** 1. Get all communities */
    @GET("communities")
    suspend fun getCommunities(): Response<GetCommunitiesResponse>

    /** 2. Create community: { name, description } */
    @POST("communities")
    suspend fun createCommunity(@Body body: CreateCommunityRequest): Response<CreateCommunityResponse>

    /** 3. Join community */
    @POST("communities/{id}/join")
    suspend fun joinCommunity(@Path("id") communityId: String): Response<JoinCommunityResponse>

    /** 4. Get all channels for a community */
    @GET("channels/communities/{id}/channels")
    suspend fun getChannels(@Path("id") communityId: String): Response<GetChannelsResponse>

    /** 5. Create channel: { name, description } */
    @POST("channels/communities/{id}/channels")
    suspend fun createChannel(
        @Path("id") communityId: String,
        @Body body: CreateChannelRequest
    ): Response<ChannelResponse>

    /** 6. Get channel details */
    @GET("channels/{id}")
    suspend fun getChannelDetails(@Path("id") channelId: String): Response<ChannelResponse>

    /** 7. Update channel: { name?, description? } */
    @PATCH("channels/{id}")
    suspend fun updateChannel(
        @Path("id") channelId: String,
        @Body body: UpdateChannelRequest
    ): Response<ChannelResponse>

    /** 8. Delete channel */
    @DELETE("channels/{id}")
    suspend fun deleteChannel(@Path("id") channelId: String): Response<ApiResponse>
}
