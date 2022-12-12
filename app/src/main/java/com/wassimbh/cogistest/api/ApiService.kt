package com.wassimbh.cogistest.api

import retrofit2.http.GET

interface ApiService {

    @GET("cards")
    suspend fun getTestApi(): Any
}