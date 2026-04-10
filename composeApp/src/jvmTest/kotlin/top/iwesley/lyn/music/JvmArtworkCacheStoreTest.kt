package top.iwesley.lyn.music

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import top.iwesley.lyn.music.platform.createJvmArtworkCacheStore

class JvmArtworkCacheStoreTest {

    @Test
    fun `cache reuses existing file for same locator and redownloads after deletion`() {
        synchronized(USER_HOME_LOCK) {
            val originalUserHome = System.getProperty("user.home")
            val temporaryUserHome = kotlin.io.path.createTempDirectory("lynmusic-artwork-cache-home")
            val requestCount = AtomicInteger(0)
            val payload = byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
                0x01,
            )
            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
                createContext("/cover") { exchange ->
                    requestCount.incrementAndGet()
                    exchange.sendResponseHeaders(200, payload.size.toLong())
                    exchange.responseBody.use { it.write(payload) }
                }
                start()
            }
            try {
                System.setProperty("user.home", temporaryUserHome.absolutePathString())
                val store = createJvmArtworkCacheStore()
                val locator = "http://127.0.0.1:${server.address.port}/cover"

                val first = runBlocking { store.cache(locator, locator) }
                val second = runBlocking { store.cache(locator, locator) }

                val firstPath = assertNotNull(first)
                assertEquals(firstPath, second)
                assertTrue(firstPath.endsWith(".png"))
                assertEquals(1, requestCount.get())

                File(firstPath).delete()

                val third = runBlocking { store.cache(locator, locator) }

                assertEquals(firstPath, third)
                assertEquals(2, requestCount.get())
            } finally {
                System.setProperty("user.home", originalUserHome)
                server.stop(0)
            }
        }
    }
}

private val USER_HOME_LOCK = Any()
