package top.iwesley.lyn.music.online

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.resolve.DeviceFingerprint
import top.iwesley.lyn.music.online.resolve.TxUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.Quality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TxUrlResolverTest {

    private fun mockHttp(json: String): HttpClient = HttpClient(MockEngine { _ ->
        respond(
            content = ByteReadChannel(json),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    })

    private val fakeFingerprint = DeviceFingerprint(
        guid = "a".repeat(32),
        wid = "b".repeat(32),
        deviceId = "c".repeat(32),
    )

    @Test
    fun resolves_tx_url_for_k320() = runTest {
        val mockResponse = """{"code":0,"req_0":{"data":{"sip":["https://ws.stream.qqmusic.qq.com/"],"midurlinfo":[{"purl":"C400xxxxxx.m4a?guid=a","filename":"C400xxxxxx.m4a"}]}}}"""
        val resolver = TxUrlResolver(
            http = mockHttp(mockResponse),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        val url = resolver.resolve("xxxxxx", Quality.K320)
        assertTrue(url.url.startsWith("https://ws.stream.qqmusic.qq.com/C400xxxxxx.m4a"))
        assertEquals(Quality.K320, url.quality)
    }

    @Test
    fun throws_on_empty_purl() = runTest {
        val mockResponse = """{"code":0,"req_0":{"data":{"sip":["https://ws.stream.qqmusic.qq.com/"],"midurlinfo":[{"purl":"","filename":""}]}}}"""
        val resolver = TxUrlResolver(
            http = mockHttp(mockResponse),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        assertFailsWith<top.iwesley.lyn.music.scripting.MusicSourceException> {
            resolver.resolve("xxxxxx", Quality.K320)
        }
    }
}
