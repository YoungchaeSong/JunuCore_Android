package kr.co.junu.core.network.models

import androidx.annotation.Keep
import com.google.gson.Gson
import kr.co.junu.core.extensions.camelToSnakeCase
import kr.co.junu.core.extensions.toMap
import kr.co.junu.core.extensions.toFieldMap
import kr.co.junu.core.network.ContentType
import kr.co.junu.core.network.HttpMethod
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

//val ep = EndPoint(
//    requestPath = RequestPath.UrlAgendaList,
//    body = AgendaListRequest(...),
//    clazz = AgendaListRequest::class.java
//)

@Keep
interface IRequestPath {
    val path: String
    val method: HttpMethod
    val baseUrl: String
}

@Keep
data class EndPoint<T : Any>(
    val requestPath: IRequestPath,
    val body: T? = null,
    val clazz: Class<T>,
    val useHttps: Boolean = true,
    val useJson: Boolean = true,
    var headers: MutableMap<String, String> = mutableMapOf()
) {
    companion object {
        val gson = Gson()
    }

    val url: String
        get() = "${requestPath.baseUrl}${requestPath.path}"

    val isGet: Boolean get() = requestPath.method == HttpMethod.GET

    fun getBuiltUrl(): HttpUrl {
        val builder = url.toHttpUrlOrNull()?.newBuilder()
            ?: throw IllegalArgumentException("❌ 잘못된 URL: $url")

        if (isGet && body != null) {
            queryMap().forEach { (key, value) ->
                builder.addQueryParameter(key.camelToSnakeCase(), value.toString())
            }
        }
        return builder.build()
    }

    // GET
    private fun queryMap(): Map<String, Any?> {
        return body?.toMap(clazz) ?: emptyMap()
    }

    // POST
    private fun getJsonBody(): String {
        return body?.let { gson.toJson(it) } ?: "{}"
    }

    fun getRequestBody(): RequestBody? {
        if (isGet || body == null) return null

        val fields = body.toFieldMap(clazz)

        // ✅ File이 하나라도 있으면 multipart/form-data
        val containsFile = fields.values.any { it is File }

        return if (containsFile) {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            fields.forEach { (k, v) ->
                val key = k.camelToSnakeCase()
                when (v) {
                    is File -> {
                        val requestBody = v.asRequestBody("application/octet-stream".toMediaType())
                        builder.addFormDataPart(key, v.name, requestBody)
                    }
                    else -> builder.addFormDataPart(key, v.toString())
                }
            }
            builder.build()
        } else {
            getJsonBody().toRequestBody("application/json".toMediaType())
        }
    }

}