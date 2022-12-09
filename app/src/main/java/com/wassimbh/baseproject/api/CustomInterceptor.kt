package com.wassimbh.baseproject.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Observes and modifies requests going out and the corresponding
 * responses in order to add the Access-key as a header
 */
class CustomInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url
        val url = originalHttpUrl.newBuilder().build()
        val builder = originalRequest.newBuilder()
            .url(url)

        val request = builder.build()
        return chain.proceed(request)
    }
}