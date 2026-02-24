package com.nursena.fenlab_android.data.remote.api

import retrofit2.http.GET

interface UserApi {

    @GET("/me")
    suspend fun getCurrentUser(

    )

}