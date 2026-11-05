package com.example.yappy.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CommunityApiService {

    @GET("communities")
    suspend fun getCommunities(): Response<GetCommunitiesResponse>

    @POST("communities")
    suspend fun createCommunity(@Body body: CreateCommunityRequest): Response<CreateCommunityResponse>

    @POST("communities/{id}/join")
    suspend fun joinCommunity(@Path("id") communityId: String): Response<JoinCommunityResponse>

    @GET("channels/communities/{id}/channels")
    suspend fun getChannels(@Path("id") communityId: String): Response<GetChannelsResponse>

    @POST("channels/communities/{id}/channels")
    suspend fun createChannel(
        @Path("id") communityId: String,
        @Body body: CreateChannelRequest
    ): Response<ChannelResponse>

    @GET("channels/{id}")
    suspend fun getChannelDetails(@Path("id") channelId: String): Response<ChannelResponse>

    @PATCH("channels/{id}")
    suspend fun updateChannel(
        @Path("id") channelId: String,
        @Body body: UpdateChannelRequest
    ): Response<ChannelResponse>

    @DELETE("channels/{id}")
    suspend fun deleteChannel(@Path("id") channelId: String): Response<ApiResponse>
}
