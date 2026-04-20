package top.iwesley.lyn.music

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.iwesley.lyn.music.data.repository.SettingsRepository
import top.iwesley.lyn.music.online.DefaultMusicSourceFacade
import top.iwesley.lyn.music.online.MusicSourceFacade
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.resolve.DefaultSongUrlResolver
import top.iwesley.lyn.music.online.resolve.DeviceFingerprint
import top.iwesley.lyn.music.online.resolve.FindMusicM0
import top.iwesley.lyn.music.online.resolve.KgUrlResolver
import top.iwesley.lyn.music.online.resolve.KwUrlResolver
import top.iwesley.lyn.music.online.resolve.MgUrlResolver
import top.iwesley.lyn.music.online.resolve.SongUrlResolver
import top.iwesley.lyn.music.online.resolve.SourceUrlResolver
import top.iwesley.lyn.music.online.resolve.TxUrlResolver
import top.iwesley.lyn.music.online.resolve.WyUrlResolver
import top.iwesley.lyn.music.online.source.JsBridgeImpl
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.store.OnlineSearchStore

/**
 * M0/M1.0 在线音乐 DI 容器 —— 封装 `HttpClient / JsBridge / Facade / Repository / Store /
 * 5 源 Kotlin Resolver` 的构造链。
 *
 * 不与已有 [LynMusicAppComponent] 竞争；后者聚合"本地库 + 播放器 + 偏好"等 feature store，
 * 而 [AppContainer] 只关心在线搜索/URL 解析链。将来 FavoritesStore / PlaybackRepository
 * 等交互时，会在 Composable 层把两边的回调串起来，不做全局合并。
 *
 * 构造原则：
 *  - [HttpClient] 无参构造 —— 依赖各平台 classpath 上的 ktor engine 自动发现
 *    （androidMain = OkHttp；jvmMain = OkHttp；apple = Darwin；见 build.gradle.kts）。
 *  - 五源共享同一个 [HttpClient]；UA / platformTag 单源独立（lx SDK 里个别源靠 UA 差异做判断）。
 *  - 每源独立 [JsBridgeImpl]；共享 crypto 与 http 实例即可。
 *  - 设备指纹从 [SettingsRepository.deviceFingerprint] 读取；首启为空时 [DeviceFingerprint.generate]
 *    生成一次并异步持久化，保持跨会话稳定以通过 tx/wy 的签名风控。
 */
class AppContainer(
    scope: CoroutineScope,
    settingsRepository: SettingsRepository,
    userAgent: String = DEFAULT_USER_AGENT,
) {
    private val http: HttpClient = HttpClient()
    private val crypto = createPlatformCrypto()

    /** 首启生成 + 持久化，后续从 repository 读回；保证 tx/wy 签名依赖的 guid/deviceId 稳定。 */
    private val fingerprint: DeviceFingerprint = run {
        val persisted = settingsRepository.deviceFingerprint.value
        if (persisted.isBlank()) {
            val generated = DeviceFingerprint.generate()
            scope.launch {
                settingsRepository.setDeviceFingerprint(
                    "${generated.guid}|${generated.wid}|${generated.deviceId}",
                )
            }
            generated
        } else {
            val parts = persisted.split("|")
            DeviceFingerprint(
                guid = parts.getOrNull(0).orEmpty(),
                wid = parts.getOrNull(1).orEmpty(),
                deviceId = parts.getOrNull(2).orEmpty(),
            )
        }
    }

    val musicSourceFacade: MusicSourceFacade = DefaultMusicSourceFacade.build { sourceId ->
        JsBridgeImpl(
            http = http,
            platformTag = currentPlatformTag(),
            userAgent = userAgent,
            crypto = crypto,
        )
    }

    val onlineMusicRepository: OnlineMusicRepository = OnlineMusicRepository(musicSourceFacade)

    val onlineSearchStore: OnlineSearchStore = OnlineSearchStore(
        repository = onlineMusicRepository,
        scope = scope,
    )

    /**
     * M1.0 T13：5 源 Kotlin-first URL resolver 路由表。
     *
     * 与 [DefaultSongUrlResolver.sourceResolvers] 对齐：命中 map 的 source id 优先用 Kotlin
     * 路径拿直链；miss（如其它第三方源）则退回 [OnlineMusicRepository] 的 JS 引擎路径。
     *
     * 其中 [TxUrlResolver] 需要稳定 [DeviceFingerprint]，其它四源仅需 http+crypto。
     * [KgUrlResolver] 暂用 md5 key 方案（Task 11 revision），无 JS bridge 依赖。
     */
    private val sourceResolvers: Map<String, SourceUrlResolver> = mapOf(
        "tx" to TxUrlResolver(http, crypto, fingerprint),
        "wy" to WyUrlResolver(http, crypto),
        "kw" to KwUrlResolver(http, crypto),
        "kg" to KgUrlResolver(http, crypto),
        "mg" to MgUrlResolver(http, crypto),
    )

    /**
     * URL 解析器：同源降级 + 跨源 findMusic 兜底。
     *
     * 会被 `buildPlayerAppComponent` 传给 [top.iwesley.lyn.music.data.repository.DefaultPlaybackRepository]；
     * Task 10+ 的 `online-lazy://` locator 会走这条路径。
     */
    val songUrlResolver: SongUrlResolver = DefaultSongUrlResolver(
        repository = onlineMusicRepository,
        findMusic = FindMusicM0(onlineMusicRepository),
        sourceResolvers = sourceResolvers,
    )

    companion object {
        /**
         * 默认 UA —— lx-music-mobile 源脚本里大多数站点对桌面 Chrome UA 放宽风控；
         * 单源若有特殊 UA 需求，可由 per-source override 接入。
         */
        const val DEFAULT_USER_AGENT: String =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}

/**
 * 平台标签 —— 部分 lx 源脚本会按 `navigator.platform` 返回值分支走不同路径；
 * 五平台各自在 actual 里返回 `android / ios / macos / jvm`。
 */
expect fun currentPlatformTag(): String
