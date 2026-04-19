package top.iwesley.lyn.music

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import top.iwesley.lyn.music.online.DefaultMusicSourceFacade
import top.iwesley.lyn.music.online.MusicSourceFacade
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.resolve.DefaultSongUrlResolver
import top.iwesley.lyn.music.online.resolve.FindMusicM0
import top.iwesley.lyn.music.online.resolve.SongUrlResolver
import top.iwesley.lyn.music.online.source.JsBridgeImpl
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.store.OnlineSearchStore

/**
 * M0 在线音乐 DI 容器 —— 封装 `HttpClient / JsBridge / Facade / Repository / Store` 的构造链。
 *
 * 不与已有 [LynMusicAppComponent] 竞争；后者聚合"本地库 + 播放器 + 偏好"等 feature store，
 * 而 [AppContainer] 只关心 T7 新增的在线搜索链。将来 T10/T11 引入 FavoritesStore / PlaybackRepository
 * 等交互时，会在 Composable 层把两边的回调串起来，不做全局合并。
 *
 * 构造原则：
 *  - [HttpClient] 无参构造 —— 依赖各平台 classpath 上的 ktor engine 自动发现
 *    （androidMain = OkHttp；jvmMain = OkHttp；apple = Darwin；见 build.gradle.kts）。
 *  - 五源共享同一个 [HttpClient]；UA / platformTag 单源独立（lx SDK 里个别源靠 UA 差异做判断）。
 *  - 每源独立 [JsBridgeImpl]；共享 crypto 与 http 实例即可。
 */
class AppContainer(
    scope: CoroutineScope,
    userAgent: String = DEFAULT_USER_AGENT,
) {
    private val http: HttpClient = HttpClient()
    private val crypto = createPlatformCrypto()

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
     * T9 产物：URL 解析器（同源降级 + 跨源 findMusic 兜底）。
     *
     * 会被 `buildPlayerAppComponent` 传给 [top.iwesley.lyn.music.data.repository.DefaultPlaybackRepository]；
     * 当前阶段（T10 OnlineSongToTrack 尚未上线）所有 Track 的 mediaLocator 都不是 `online-lazy://` 形式，
     * 因此 resolver 实际不会被触达，为"代码就绪但运行期 no-op"状态。
     */
    val songUrlResolver: SongUrlResolver = DefaultSongUrlResolver(
        repository = onlineMusicRepository,
        findMusic = FindMusicM0(onlineMusicRepository),
    )

    companion object {
        /**
         * 默认 UA —— lx-music-mobile 源脚本里大多数站点对桌面 Chrome UA 放宽风控；
         * 单源若有特殊 UA 需求，可由 T10+ 的 per-source override 接入。
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
