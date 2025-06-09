package kr.co.junu.core

import kotlinx.coroutines.runBlocking
import kr.co.junu.core.network.ApiClient
import kr.co.junu.core.network.HttpMethod
import kr.co.junu.core.network.models.EndPoint
import kr.co.junu.core.network.models.IRequestPath
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

data class RequestAppVersion(
    val os_type: String = "ANDROID",
    val app_type: String = "OurVote"
)

data class AppVersion (
    val appType: String,
    val osType: String,
    val version: String,
    val forceUpdate: Boolean,
    val description: String? = null
)

enum class RequestPath(
    override val path: String,
    override val method: HttpMethod,
    override val baseUrl: String = "https://www.junu.co.kr"  // default value
) : IRequestPath {
    UrlAppVersion("/compliance/appVersion/", HttpMethod.POST),
    UrlAgendaList("/ov/agenda/list", HttpMethod.POST)
}

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAppVersionApi() = runBlocking {
        val endPoint = EndPoint(
            requestPath = RequestPath.UrlAppVersion,
            body = RequestAppVersion(),
            clazz = RequestAppVersion::class.java,
        )

        try {
            val response = ApiClient.sendRequest<RequestAppVersion, AppVersion>(endPoint)
            println("✅ 테스트 성공: $response")
        } catch (e: Exception) {
            println("❌ 테스트 실패: ${e.localizedMessage}")
            assert(false)
        }
    }
}