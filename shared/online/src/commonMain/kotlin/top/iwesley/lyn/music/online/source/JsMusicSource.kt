package top.iwesley.lyn.music.online.source

import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lynmusic.shared.online.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import top.iwesley.lyn.music.online.types.OnlineLyric
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SearchPage
import top.iwesley.lyn.music.online.types.SourceInfo
import top.iwesley.lyn.music.scripting.JsBridge
import top.iwesley.lyn.music.scripting.JsRuntime
import top.iwesley.lyn.music.scripting.JsRuntimeFactory
import top.iwesley.lyn.music.scripting.JsValue
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * 单源 JsRuntime 驱动器。M0 生命周期：
 *   1) 首次调用任意方法时 [ensureLoaded] 注入 19 个桥函数 + bootstrap 脚本（把裸名映射到
 *      `globalThis.lyn.*`），随后 evaluate 五源中某一源的 bundle（`sdk/<id>.js`）。
 *   2) bundle footer 把入口对象挂到 `globalThis.__lyn_source_<id>`，Kotlin 侧按此路径 invoke。
 *   3) [close] 释放 runtime；JsMusicSource 一次性重建即可（缓存层在 T6）。
 *
 * 注意：
 *  - `runtimeFactory` 可注入，测试用 [FakeJsRuntime]；生产用 [JsRuntimeFactory.create]。
 *  - `getPlayableUrl` 目前会命中 T3 bundle 里 api-source 的 stub（`apis.getMusicUrl` 返回 reject）——
 *    这正是 T9 `SongUrlResolver` 要补的能力；M0 阶段调用会得到 `ScriptRuntimeError`，属预期。
 */
@OptIn(ExperimentalResourceApi::class)
class JsMusicSource(
    private val info: SourceInfo,
    private val bridge: JsBridge,
    private val runtimeFactory: (sourceId: String, bridge: JsBridge) -> JsRuntime = JsRuntimeFactory::create,
) {
    private val loadMutex = Mutex()
    private var runtime: JsRuntime? = null

    val sourceId: String get() = info.id

    private suspend fun ensureLoaded(): JsRuntime = loadMutex.withLock {
        runtime?.let { return it }
        val rt = runtimeFactory(info.id, bridge)
        registerBridgeFunctions(rt)
        rt.evaluate(BRIDGE_NAMESPACE_BOOTSTRAP, "lyn-bridge-bootstrap.js")
        val script = Res.readBytes("files/sdk/${info.id}.js").decodeToString()
        rt.evaluate(script, "${info.id}.js")
        runtime = rt
        rt
    }

    /**
     * 把 JsBridge 的 19 个成员注册为 JS 侧裸名（`globalThis.<name>`），
     * 配合 [BRIDGE_NAMESPACE_BOOTSTRAP] 脚本把它们整体搬到 `globalThis.lyn.*` —— 这正是 T3 shim
     * （`scripts/bundle-sdk-shims.mjs`）期望的调用前缀。
     *
     * 裸名和 bootstrap 脚本里的键一一对应；添加/重命名时两处必须同步。
     */
    private fun registerBridgeFunctions(rt: JsRuntime) {
        rt.register("__lyn_request") { args ->
            val url = args.stringAt(0, "request.url")
            val opts = (args.getOrNull(1) as? JsValue.Obj)?.entries.orEmpty()
            bridge.request(url, opts)
        }
        rt.register("__lyn_md5") { args ->
            JsValue.Str(bridge.md5(args.bytesAt(0, "md5")))
        }
        rt.register("__lyn_sha1") { args ->
            JsValue.Bytes(bridge.sha1(args.bytesAt(0, "sha1")))
        }
        rt.register("__lyn_sha256") { args ->
            JsValue.Bytes(bridge.sha256(args.bytesAt(0, "sha256")))
        }
        rt.register("__lyn_aesEncrypt") { args ->
            // lx 侧调用：aesEncrypt(data, key, iv, mode) — 经 shim 已转换为此签名
            val data = args.bytesAt(0, "aesEncrypt.data")
            val key = args.bytesAt(1, "aesEncrypt.key")
            val iv = args.getOrNull(2).optBytes()
            val mode = (args.getOrNull(3) as? JsValue.Str)?.value ?: "CBC/PKCS5Padding"
            JsValue.Bytes(bridge.aesEncrypt(data, key, iv, mode))
        }
        rt.register("__lyn_desEncrypt") { args ->
            val data = args.bytesAt(0, "desEncrypt.data")
            val key = args.bytesAt(1, "desEncrypt.key")
            val iv = args.getOrNull(2).optBytes()
            val mode = (args.getOrNull(3) as? JsValue.Str)?.value ?: "CBC/PKCS5Padding"
            JsValue.Bytes(bridge.desEncrypt(data, key, iv, mode))
        }
        rt.register("__lyn_rsaEncrypt") { args ->
            val data = args.bytesAt(0, "rsaEncrypt.data")
            val pem = args.stringAt(1, "rsaEncrypt.publicKey")
            JsValue.Bytes(bridge.rsaEncrypt(data, pem))
        }
        rt.register("__lyn_base64Encode") { args ->
            JsValue.Str(bridge.base64Encode(args.bytesAt(0, "base64Encode")))
        }
        rt.register("__lyn_base64Decode") { args ->
            JsValue.Bytes(bridge.base64Decode(args.stringAt(0, "base64Decode")))
        }
        rt.register("__lyn_bufferFrom") { args ->
            val str = args.stringAt(0, "bufferFrom.str")
            val enc = (args.getOrNull(1) as? JsValue.Str)?.value ?: "utf8"
            JsValue.Bytes(bridge.bufferFrom(str, enc))
        }
        rt.register("__lyn_bufferToString") { args ->
            val bytes = args.bytesAt(0, "bufferToString.bytes")
            val enc = (args.getOrNull(1) as? JsValue.Str)?.value ?: "utf8"
            JsValue.Str(bridge.bufferToString(bytes, enc))
        }
        rt.register("__lyn_zlibInflate") { args ->
            JsValue.Bytes(bridge.zlibInflate(args.bytesAt(0, "zlibInflate")))
        }
        rt.register("__lyn_iconvDecode") { args ->
            val bytes = args.bytesAt(0, "iconvDecode.bytes")
            val enc = args.stringAt(1, "iconvDecode.encoding")
            JsValue.Str(bridge.iconvDecode(bytes, enc))
        }
        rt.register("__lyn_iconvEncode") { args ->
            val str = args.stringAt(0, "iconvEncode.str")
            val enc = args.stringAt(1, "iconvEncode.encoding")
            JsValue.Bytes(bridge.iconvEncode(str, enc))
        }
        rt.register("__lyn_setTimeout") { args ->
            // shim 侧签名是 setTimeout(delayMs, callback) 但实际 JS side 只传 delay（host 回调机制暂无）；
            // M0 仅 delay 后立即 resolve，不执行 callback（lx 侧仅用于 Promise 轮询 sleep）。
            val delay = ((args.getOrNull(0) as? JsValue.Num)?.value ?: 0.0).toLong().coerceAtLeast(0L)
            bridge.setTimeout(delay) { /* no-op: host 侧暂不回调 JS 函数 */ }
            JsValue.Num(0.0)
        }
        rt.register("__lyn_clearTimeout") { args ->
            val id = ((args.getOrNull(0) as? JsValue.Num)?.value ?: 0.0).toLong()
            bridge.clearTimeout(id)
            JsValue.Undefined
        }
        rt.register("__lyn_log") { args ->
            val level = (args.getOrNull(0) as? JsValue.Str)?.value ?: "debug"
            bridge.log(level, args.drop(1))
            JsValue.Undefined
        }
        rt.register("__lyn_platformTag") { JsValue.Str(bridge.platformTag) }
        rt.register("__lyn_userAgent") { JsValue.Str(bridge.userAgent) }
    }

    suspend fun search(keyword: String, page: Int, limit: Int): SearchPage<OnlineSong> {
        val rt = ensureLoaded()
        val result = rt.invoke(
            "__lyn_source_${info.id}.musicSearch.search",
            JsValue.Str(keyword),
            JsValue.Num(page.toDouble()),
            JsValue.Num(limit.toDouble()),
        )
        return parseSearchPage(result, info.id, page)
    }

    suspend fun getPlayableUrl(songmid: String, quality: Quality): PlayableUrl {
        val rt = ensureLoaded()
        val musicInfo = JsValue.Obj(
            mapOf(
                "songmid" to JsValue.Str(songmid),
                "source" to JsValue.Str(info.id),
            ),
        )
        val result = rt.invoke(
            "__lyn_source_${info.id}.getMusicUrl",
            musicInfo,
            JsValue.Str(quality.lxKey),
        )
        val url = (result as? JsValue.Obj)?.entries?.get("url")?.let { (it as? JsValue.Str)?.value }
            ?: throw MusicSourceException.Parse(info.id, null)
        return PlayableUrl(url, quality, Clock.System.now())
    }

    suspend fun getLyric(songmid: String): OnlineLyric {
        val rt = ensureLoaded()
        val musicInfo = JsValue.Obj(
            mapOf(
                "songmid" to JsValue.Str(songmid),
                "source" to JsValue.Str(info.id),
            ),
        )
        val r = rt.invoke("__lyn_source_${info.id}.getLyric", musicInfo)
        val o = r as? JsValue.Obj ?: throw MusicSourceException.Parse(info.id, null)
        val lyric = (o.entries["lyric"] as? JsValue.Str)?.value.orEmpty()
        return OnlineLyric(
            original = lyric,
            translation = (o.entries["tlyric"] as? JsValue.Str)?.value?.ifBlank { null },
            enhanced = (o.entries["lxlyric"] as? JsValue.Str)?.value?.ifBlank { null },
        )
    }

    suspend fun getPic(songmid: String): String {
        val rt = ensureLoaded()
        val musicInfo = JsValue.Obj(
            mapOf(
                "songmid" to JsValue.Str(songmid),
                "source" to JsValue.Str(info.id),
            ),
        )
        val r = rt.invoke("__lyn_source_${info.id}.getPic", musicInfo)
        return (r as? JsValue.Str)?.value ?: throw MusicSourceException.Parse(info.id, null)
    }

    fun close() {
        runtime?.close()
        runtime = null
    }

    private companion object {
        /**
         * bootstrap 把 19 个 `__lyn_*` 裸名搬到 `globalThis.lyn.*`，字段名与 T3 shim
         * (`scripts/bundle-sdk-shims.mjs`) 的 `globalThis.lyn.<x>` 调用点一一对应。
         *
         * 其中 `platformTag` / `userAgent` 在 [JsBridge] 是 val（非函数），Kotlin 侧 register
         * 了两个 zero-arg 裸函数，bootstrap 时立即调用并把返回字符串写为 lyn 对象上的静态字段。
         */
        const val BRIDGE_NAMESPACE_BOOTSTRAP: String = """
            (function() {
              globalThis.lyn = globalThis.lyn || {};
              var L = globalThis.lyn;
              L.request = function(url, options) { return globalThis.__lyn_request(url, options); };
              L.md5 = function(input) { return globalThis.__lyn_md5(input); };
              L.sha1 = function(input) { return globalThis.__lyn_sha1(input); };
              L.sha256 = function(input) { return globalThis.__lyn_sha256(input); };
              L.aesEncrypt = function(data, key, iv, mode) { return globalThis.__lyn_aesEncrypt(data, key, iv, mode); };
              L.desEncrypt = function(data, key, iv, mode) { return globalThis.__lyn_desEncrypt(data, key, iv, mode); };
              L.rsaEncrypt = function(data, publicKey) { return globalThis.__lyn_rsaEncrypt(data, publicKey); };
              L.base64Encode = function(input) { return globalThis.__lyn_base64Encode(input); };
              L.base64Decode = function(input) { return globalThis.__lyn_base64Decode(input); };
              L.bufferFrom = function(str, encoding) { return globalThis.__lyn_bufferFrom(str, encoding); };
              L.bufferToString = function(bytes, encoding) { return globalThis.__lyn_bufferToString(bytes, encoding); };
              L.zlibInflate = function(input) { return globalThis.__lyn_zlibInflate(input); };
              L.iconvDecode = function(input, encoding) { return globalThis.__lyn_iconvDecode(input, encoding); };
              L.iconvEncode = function(input, encoding) { return globalThis.__lyn_iconvEncode(input, encoding); };
              L.setTimeout = function(delayMs, callback) { return globalThis.__lyn_setTimeout(delayMs, callback); };
              L.clearTimeout = function(id) { return globalThis.__lyn_clearTimeout(id); };
              L.log = function(level) {
                var args = []; for (var i = 1; i < arguments.length; i++) args.push(arguments[i]);
                return globalThis.__lyn_log.apply(null, [level].concat(args));
              };
              L.platformTag = globalThis.__lyn_platformTag();
              L.userAgent = globalThis.__lyn_userAgent();
            })();
        """
    }
}

// ---------------------------------------------------------------------------
// JS → Kotlin 结构解析
// ---------------------------------------------------------------------------

private fun parseSearchPage(result: JsValue, sourceId: String, page: Int): SearchPage<OnlineSong> {
    val obj = (result as? JsValue.Obj)?.entries ?: throw MusicSourceException.Parse(sourceId, null)
    val list = (obj["list"] as? JsValue.Arr)?.items.orEmpty().mapNotNull { it.toOnlineSong(sourceId) }
    val total = (obj["total"] as? JsValue.Num)?.value?.toInt() ?: list.size
    val allPage = (obj["allPage"] as? JsValue.Num)?.value?.toInt() ?: 1
    return SearchPage(
        items = list,
        page = page,
        totalPages = allPage,
        totalItems = total,
        sourceId = sourceId,
    )
}

private fun JsValue.toOnlineSong(sourceId: String): OnlineSong? {
    val o = (this as? JsValue.Obj)?.entries ?: return null
    fun s(k: String) = (o[k] as? JsValue.Str)?.value
    val songmid = s("songmid") ?: return null
    val name = s("name") ?: return null
    val singer = s("singer") ?: return null
    val interval = s("interval").orEmpty() // "mm:ss"
    val intervalSec = interval.split(":").mapNotNull { it.toIntOrNull() }
        .let { if (it.size == 2) it[0] * 60 + it[1] else 0 }
    val availableLxKeys = ((o["types"] as? JsValue.Arr)?.items.orEmpty())
        .mapNotNull { ((it as? JsValue.Obj)?.entries?.get("type") as? JsValue.Str)?.value }
    val qualities = availableLxKeys.mapNotNull { Quality.fromLxKey(it) }.distinct()
    val albumId = s("albumId")?.ifBlank { null }
    return OnlineSong(
        id = OnlineMusicId(sourceId, songmid, albumId),
        name = name,
        singer = singer,
        album = s("albumName")?.ifBlank { null },
        albumId = albumId,
        intervalSeconds = intervalSec,
        coverUrl = s("img")?.ifBlank { null },
        availableQualities = qualities,
        defaultQuality = qualities.firstOrNull() ?: Quality.K320,
    )
}

// ---------------------------------------------------------------------------
// JsValue 参数抽取辅助
// ---------------------------------------------------------------------------

private fun List<JsValue>.stringAt(index: Int, hint: String): String =
    (getOrNull(index) as? JsValue.Str)?.value
        ?: throw IllegalArgumentException("$hint: expected string at index $index, got ${getOrNull(index)}")

private fun List<JsValue>.bytesAt(index: Int, hint: String): ByteArray =
    when (val v = getOrNull(index)) {
        is JsValue.Bytes -> v.data
        is JsValue.Str -> v.value.encodeToByteArray()
        else -> throw IllegalArgumentException("$hint: expected bytes/string at index $index, got $v")
    }

private fun JsValue?.optBytes(): ByteArray? = when (this) {
    is JsValue.Bytes -> data
    is JsValue.Str -> value.encodeToByteArray()
    null, JsValue.Null, JsValue.Undefined -> null
    else -> null
}
