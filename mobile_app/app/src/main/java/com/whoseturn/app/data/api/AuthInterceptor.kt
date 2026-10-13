package com.whoseturn.app.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.getAccessToken()

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrEmpty() && !original.url.encodedPath.contains("auth/login") && !original.url.encodedPath.contains("auth/register")) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
