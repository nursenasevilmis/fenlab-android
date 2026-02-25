package com.nursena.fenlab_android.core.network

import com.nursena.fenlab_android.core.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    // Bu path'lere token eklenmez
    private val publicPaths = listOf(
        "/api/auth/login",
        "/api/auth/register"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (publicPaths.any { path.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenManager.getToken() }

        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}

