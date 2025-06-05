package kr.co.junu.core.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.junu.core.network.models.ApiResponse
import kr.co.junu.core.network.models.EndPoint
import kr.co.junu.core.network.models.Status
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

object APIClient {
    val gson = Gson()
    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    suspend inline fun <reified Req : Any, reified Res : Any> sendRequest(
        endPoint: EndPoint<Req>
    ): ApiResponse<Res> = withContext(Dispatchers.IO) {
        val url = endPoint.getBuiltUrl()
        val requestBuilder = Request.Builder().url(url)

        endPoint.headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }

        val body = endPoint.getRequestBody()
        requestBuilder.method(endPoint.requestPath.method.value, body)

        val request = requestBuilder.build()

        try {
            val response = client.newCall(request).execute()
            val raw = response.body?.string()
            if (!response.isSuccessful || raw == null) {
                throw IOException("❌ 서버 응답 에러: $response")
            }

            val type = object : TypeToken<ApiResponse<Res>>() {}.type
            try {
                gson.fromJson<ApiResponse<Res>>(raw, type)
            } catch (e: JsonSyntaxException) {
                throw IOException("Invalid JSON", e)
            }
        } catch (e: Exception) {
            Log.e("APIClient", "❌ 요청 실패: ${e.localizedMessage}")
            throw e
        }
    }

    suspend inline fun <reified Req : Any> sendWithoutDecoding(
        endPoint: EndPoint<Req>
    ): Boolean {
        return try {
            sendRequest<Req, Any>(endPoint).status == Status.SUCCESS
        } catch (e: Exception) {
            Log.e("APIClient", "❌ sendWithoutDecoding error: ${e.localizedMessage}")
            false
        }
    }
}