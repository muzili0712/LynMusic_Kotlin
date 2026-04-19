package top.iwesley.lyn.music.online

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.source.JsMusicSource
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SourceManifest
import top.iwesley.lyn.music.scripting.JsBridge
import top.iwesley.lyn.music.scripting.JsValue

/**
 * JsMusicSource 契约测试：用 [FakeJsRuntime] 替代真实 JS 引擎，聚焦 Kotlin 侧的参数拼装
 * (path / args) 和响应解析（SearchPage / PlayableUrl / OnlineLyric）。
 *
 * 注：测试方法名全部用下划线；KMP commonTest 在 Apple/Android 目标上反引号名字会失败。
 */
class JsMusicSourceTest {

    private fun fakeBridge(): JsBridge = object : JsBridge {
        override suspend fun request(url: String, options: Map<String, JsValue>) = JsValue.Null
        override fun md5(input: ByteArray) = ""
        override fun sha1(input: ByteArray) = ByteArray(0)
        override fun sha256(input: ByteArray) = ByteArray(0)
        override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
        override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
        override fun rsaEncrypt(data: ByteArray, publicKeyPem: String) = data
        override fun base64Encode(input: ByteArray) = ""
        override fun base64Decode(input: String) = ByteArray(0)
        override fun bufferFrom(str: String, encoding: String) = str.encodeToByteArray()
        override fun bufferToString(bytes: ByteArray, encoding: String) = bytes.decodeToString()
        override fun zlibInflate(input: ByteArray) = input
        override fun iconvDecode(input: ByteArray, encoding: String) = input.decodeToString()
        override fun iconvEncode(input: String, encoding: String) = input.encodeToByteArray()
        override suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit) = 0L
        override fun clearTimeout(id: Long) {}
        override fun log(level: String, args: List<JsValue>) {}
        override val platformTag = "test"
        override val userAgent = "t"
    }

    @Test
    fun search_passes_keyword_and_page_to_correct_js_path() = runTest {
        val info = SourceManifest.byId("kw")!!
        var captured: Pair<String, List<JsValue>>? = null
        val fake = FakeJsRuntime("kw") { path, args ->
            captured = path to args
            JsValue.Obj(
                mapOf(
                    "list" to JsValue.Arr(emptyList()),
                    "total" to JsValue.Num(0.0),
                    "allPage" to JsValue.Num(1.0),
                ),
            )
        }
        val source = JsMusicSource(info, fakeBridge()) { _, _ -> fake }
        val page = source.search("周杰伦", 2, 30)

        val (path, args) = checkNotNull(captured) { "invokeHandler did not run" }
        assertEquals("__lyn_source_kw.musicSearch.search", path)
        assertEquals(JsValue.Str("周杰伦"), args[0])
        assertEquals(JsValue.Num(2.0), args[1])
        assertEquals(JsValue.Num(30.0), args[2])
        assertEquals(0, page.totalItems)
        assertEquals(2, page.page)
        assertEquals("kw", page.sourceId)
        // ensureLoaded 应完成 19 条 bridge register + bootstrap；这里只做下限核验
        assertTrue(
            fake.registeredNames.containsAll(
                listOf(
                    "__lyn_request",
                    "__lyn_md5",
                    "__lyn_aesEncrypt",
                    "__lyn_platformTag",
                    "__lyn_userAgent",
                ),
            ),
            "expected bridge names registered, got ${fake.registeredNames}",
        )
    }

    @Test
    fun getPlayableUrl_maps_lx_quality_key_and_wraps_in_PlayableUrl() = runTest {
        val info = SourceManifest.byId("kw")!!
        var seenType: JsValue? = null
        val fake = FakeJsRuntime("kw") { _, args ->
            seenType = args[1]
            JsValue.Obj(mapOf("url" to JsValue.Str("https://example/play.mp3")))
        }
        val source = JsMusicSource(info, fakeBridge()) { _, _ -> fake }
        val r = source.getPlayableUrl("abc123", Quality.K320)
        assertEquals(JsValue.Str("320k"), seenType)
        assertEquals("https://example/play.mp3", r.url)
        assertEquals(Quality.K320, r.quality)
    }

    @Test
    fun getLyric_parses_original_translation_and_enhanced() = runTest {
        val info = SourceManifest.byId("kw")!!
        val fake = FakeJsRuntime("kw") { _, _ ->
            JsValue.Obj(
                mapOf(
                    "lyric" to JsValue.Str("[00:01]hello"),
                    "tlyric" to JsValue.Str("[00:01]你好"),
                    "lxlyric" to JsValue.Str("[00:01]h,e,l,l,o"),
                ),
            )
        }
        val source = JsMusicSource(info, fakeBridge()) { _, _ -> fake }
        val ly = source.getLyric("abc")
        assertEquals("[00:01]hello", ly.original)
        assertEquals("[00:01]你好", ly.translation)
        assertEquals("[00:01]h,e,l,l,o", ly.enhanced)
    }
}
