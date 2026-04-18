# lx-music-mobile 在线音乐生态移植到 LynMusic_Kotlin 全平台 — 设计文档

- 日期：2026-04-17
- 作者：健哥 + Claude（brainstorming pair）
- 源项目：`/Users/lijian/CatVod/lx-music-mobile`（React Native，Android only）
- 目标项目：`/Users/lijian/CatVod/CatPawLocal/LynMusic_Kotlin`（Kotlin Multiplatform + Compose Multiplatform；Android / iOS / macOS / Windows / Linux）

---

## 1. 目标与范围

将 lx-music-mobile 的"在线音乐生态"能力迁移到 LynMusic_Kotlin，使 Lyn 同时覆盖「本地私有曲库」与「公开在线源」两类场景，并保持现有 KMP 全平台支持。

**范围内（In Scope）：**

- 六大内置在线源：酷我 (kw)、酷狗 (kg)、QQ 音乐 (tx)、网易云 (wy)、咪咕 (mg)、虾米兼容 (xm) —— 搜索 / 歌曲 URL / 歌词 / 封面 / 排行榜 / 歌单 / 热搜 / Tip 搜索 / 评论
- 自定义源扩展机制（userApi，动态加载 JS 脚本）
- 在线歌曲接入现有 PlayerStore / PlaybackGateway
- 我的歌单批量管理 + lx 老数据 JSON 导入导出
- 不喜欢列表、临时播放队列、定时退出
- 在线下载 + lx-music-sync-server 多端同步
- 桌面歌词（四平台分别策略）
- deeplink / i18n 基础

**范围外（Out of Scope）：**

- 重写或重构 Lyn 现有的 Library / Tags / Theme / Settings / LyricsSearch 模块（仅做最小接入改动）
- 上架商店的合规隔离（用户已确认内置六源、合规压力自担）
- 歌词翻译 / 罗马音在 M0 不做（M1 起）

---

## 2. 关键决策与取舍（经澄清确认）

| 决策 | 选定 | 取舍理由 |
|---|---|---|
| 合规策略 | **内置六源**（不做分版本） | 用户自担合规成本，追求与 lx 等价体验 |
| 技术路径 | **先 JS 引擎跑 lx 原版 JS；稳定后按价值渐进 Kotlin 化** | 4-6 周内拿到端到端结果；lx 源站协议变动时可直接拉新 js 资产；userApi 天然复用 |
| 工程组织 | **新增 `shared:scripting` + `shared:online` 双模块** | scripting 是基础设施、online 是领域层；userApi 与六源共享 scripting |
| 在线歌曲入库 | **只在收藏/我的歌单时入 Room；搜索/排行/歌单详情内存缓存** | 库体积小；URL 失效不污染数据 |
| URL 失效处理 | **懒加载（入库不存 URL，每次播放重取）** | kw/kg 等 URL 只有几分钟到几小时时效 |
| 音质切换 | **全局默认 + 单曲手动切换**（lx 做法） | 兼顾新手与进阶 |
| 歌曲详情页 | **合并进现有 `PlayerUi`，不新建 PlayDetail** | Lyn 的 PlayerUi 已完备 |
| 歌词翻译 / 罗马音 | M0 仅原文；M1 起加翻译 / 罗马音 | 缩小 M0 范围，保证核心闭环按期交付 |
| lx 老数据兼容 | **M2 支持导入 lx `*.lxmf.json` / json** | 老用户搬家刚需 |
| 硬约束 | 无 | 用户明确放行 |

---

## 3. 模块拓扑与依赖

```
shared/
├── core/          (已有)  模型、MVI Store、诊断日志
├── data/          (已有)  Room、Repository、WorkflowLyricsEngine
├── features/      (已有)  Library/Playlists/Favorites/Settings/Tags/Importing
├── scripting/     🆕 基础设施：JS 引擎抽象 + Host Bridge
└── online/        🆕 领域层：六源 SDK（JS 资产 + Facade）+ 在线 Repository / Store

player/
├── core/          (已有)  PlayerStore、PlaybackGateway、SystemPlaybackControls
└── app/           🆕 追加 Search / Discover / SonglistDetail / LeaderboardDetail / CommentsBottomSheet / SourcePickerBar / QualityPickerDialog

composeApp/        (已有)  各平台 host；本次不改模块结构，仅补平台特化（桌面歌词窗口等）
```

**依赖（单向、无环）：**

- `composeApp` → `player:app` → `player:core` + `shared:features` + `shared:online`
- `shared:online` → `shared:scripting` + `shared:data` + `shared:core`
- `shared:features` → `shared:data` → `shared:core`
- `shared:scripting` → 仅平台标准库 + Ktor client

`player:core` 的 `PlayerStore` 通过 `OnlineMusicRepository.getPlayableUrl(song)` 消费在线 URL；**不感知 JS 引擎存在**。`shared:scripting` 不含业务。六源 js 资产打进 `shared:online/src/commonMain/composeResources/files/sdk/{source}.js`，KMP Resources API 读取。

---

## 4. 核心类型

**位置：`shared:core`（扩展现有模型）与 `shared:online`（新领域类型）。**

```kotlin
// shared:core
enum class MusicOrigin { LOCAL, SAMBA, WEBDAV, NAVIDROME, ONLINE }   // 新增 ONLINE

data class OnlineMusicId(
    val source: String,         // "kw" | "kg" | "tx" | "wy" | "mg" | "xm" | userApi 注入 id
    val songmid: String,
    val albumId: String? = null,
)

// shared:online
interface MusicSourceFacade {
    val sources: List<SourceInfo>
    suspend fun search(sourceId: String, keyword: String, page: Int, limit: Int): SearchPage<OnlineSong>
    suspend fun getPlayableUrl(id: OnlineMusicId, quality: Quality): PlayableUrl
    suspend fun getLyric(id: OnlineMusicId): OnlineLyric              // M0: 原文; M1: 含翻译/罗马音
    suspend fun getPic(id: OnlineMusicId): String
    suspend fun getLeaderboards(sourceId: String): List<Leaderboard>
    suspend fun getSonglistCategories(sourceId: String): List<SonglistCategory>
    suspend fun getSonglists(sourceId: String, categoryId: String?, page: Int): SearchPage<Songlist>
    suspend fun getSonglistDetail(sourceId: String, listId: String): SonglistDetail
    suspend fun getHotSearch(sourceId: String): List<String>
    suspend fun getTipSearch(sourceId: String, keyword: String): List<String>
    suspend fun getComments(id: OnlineMusicId, page: Int): SearchPage<Comment>    // M2
}

data class PlayableUrl(val url: String, val quality: Quality, val fetchedAt: Instant)  // 不入库
```

**Room 新表（M0 上线）：**

```
online_song(
  pk (source, songmid), name, singer, album, interval, cover_url,
  default_quality, created_at
)
```

- 仅在加入收藏或我的歌单时插入；不存 URL
- 现有 `Playlist / Favorite` 关联表加 `origin` 字段区分本地 vs 在线

**Room 新表（M2 上线）：** `dislike_rule(source, songmid, keyword_regex)`

---

## 5. 数据流（以"搜索并播放"为例）

```
SearchScreen (用户输入 q)
  → OnlineSearchStore.onQueryChanged(q)
  → OnlineMusicRepository.search(q)          // 多源并发
  → MusicSourceFacade.search("kw", ...)
  → JsMusicSource.call("kw", "musicSearch.search", args)
  → shared:scripting JsRuntime.invoke(...) + HostBridge (request/crypto/...)
  → 返回 List<OnlineSong>

用户点击播放
  → PlayerStore.enqueueAndPlay(OnlineSong)   // 现有 API 扩展入参
  → PlaybackGateway.prepare(queueItem)
  → SongUrlResolver.resolve(queueItem)       // 🆕 commonMain 实现
       ├─ OnlineMusicRepository.getPlayableUrl(id, 默认音质)
       ├─ 401/403/404/超时 → 同源降级音质重取
       └─ 仍失败 → 跨源 findMusic 替换
  → 平台 Gateway（Android Media3 / Apple AVPlayer / JVM VLCJ）拿 URL 播放
```

---

## 6. `shared:scripting` 设计

### 6.1 平台实现

| 平台 | JS 引擎 | 依赖 | 说明 |
|---|---|---|---|
| Android | QuickJS-Android (`com.quickjs:quickjs-android`) | Maven | ~1.5MB / ABI |
| JVM (Win/Mac/Linux) | GraalVM JS (`org.graalvm.js:js`) | Maven | 关闭高级优化以降冷启动延迟 |
| iOS / macOS | 系统 `JavaScriptCore.framework` | 零依赖 | Kotlin/Native cinterop 封装（`.def` 文件） |

### 6.2 Kotlin 接口（commonMain）

```kotlin
interface JsRuntime : Closeable {
    suspend fun evaluate(script: String, name: String = "anon.js"): JsValue
    suspend fun invoke(path: String, vararg args: Any?): JsValue          // 如 "kw.musicSearch.search"
    fun register(name: String, host: HostFunction)                         // 注册桥接函数
    val dispatcher: CoroutineDispatcher                                    // 绑定单线程
    companion object {
        fun create(bridge: JsBridge): JsRuntime                            // actual 提供
    }
}
typealias HostFunction = suspend (args: List<Any?>) -> Any?
```

### 6.3 注入 JS 全局的最小 Polyfill（`JsBridge`）

```
globalThis.lyn = {
  request(url, opts) -> Promise<{status, headers, body, bodyBytes}>,   // Ktor 实现
  crypto.md5 / sha1 / sha256 / aesEncrypt / desEncrypt / rsaEncrypt,
  buffer.from(str, encoding) / toString(bytes, encoding),
  base64.encode / decode,
  setTimeout / clearTimeout / setInterval / clearInterval,              // 协程实现
  console.log / warn / error,
  env = { platform: "android"|"jvm"|"ios"|"macos", userAgent: "..." },
}
```

### 6.4 lx 原版 js 改造

- `src/utils/request.js` → shim：`globalThis.request = (url, opts) => lyn.request(...)`
- `musicSdk/**/util.js` 中的 `react-native-quick-md5 / buffer / base64` 调用 → 由 **esbuild 预处理脚本** 自动替换为 `lyn.crypto.md5 / lyn.buffer / lyn.base64`
- 每个源产出**单个 bundled js 文件**（约 40-120KB），存 `composeResources/files/sdk/{source}.js`
- 预处理管线由 Gradle task 触发（调用本机 Node + esbuild），**产物 checkin git**；Node 仅是**开发机工具链**，用户运行时无需 Node 环境

### 6.5 并发、错误、生命周期

- 每个 `JsRuntime` 绑定单线程 Dispatcher（QuickJS / JSC 非线程安全；GraalVM 需 enter/leave context）
- **六源各自一个 JsRuntime 实例**，互相隔离——一源崩不连坐
- `invoke` 默认 15s 超时，超时或 JS 异常统一抛 `MusicSourceException`
- `JsRuntime.close()` 在 App 退出 / 源被用户禁用 / 连续 3 次崩溃时触发
- 源脚本**懒加载**：首次命中某源才 `evaluate`；可选预热（Settings 开关）

---

## 7. `shared:online` 设计

### 7.1 类组织

```
shared/online/src/commonMain/kotlin/top/iwesley/lyn/music/online/
├── MusicSourceFacade.kt              // 接口（见第 4 节）
├── DefaultMusicSourceFacade.kt       // 聚合器：委派给 JsMusicSource / (未来) KotlinKwMusicSource
├── source/
│   ├── JsMusicSource.kt              // 通用 JS 适配器，驱动单个源的 JsRuntime
│   └── SourceManifest.kt             // 元数据（id/name/supported quality/methods）
├── repository/
│   ├── OnlineMusicRepository.kt      // search / url / lyric / pic
│   ├── LeaderboardRepository.kt
│   ├── SonglistRepository.kt
│   ├── HotSearchRepository.kt
│   └── CommentsRepository.kt         // M2
├── store/
│   ├── OnlineSearchStore.kt
│   ├── DiscoverStore.kt
│   ├── SonglistDetailStore.kt
│   └── CommentsStore.kt              // M2
├── cache/
│   └── OnlineMemoryCache.kt          // LRU，按 (source, type, key)
└── userapi/                          // M3
    └── UserApiRegistry.kt
```

### 7.2 资产装载

- `JsMusicSource.init(sourceId)`：从 `Res.readBytes("files/sdk/$sourceId.js")` 读取 → `JsRuntime.evaluate(..., name = "$sourceId.js")`
- 首次调用任意方法触发；失败以 `SourceDisabled` 降级
- 资产升级走 App 发版；userApi 脚本由 `UserApiRegistry` 写入 App 私有目录、运行时装载

### 7.3 Kotlin 化迁移点（未来）

- `MusicSourceFacade` 对外稳定；内部 `DefaultMusicSourceFacade` 按源选择 `JsMusicSource` 或原生 Kotlin 实现
- 一次只替换一个源、一次只替换一个方法；可共存过渡

---

## 8. UI 与 Store 层

### 8.1 新增 Screen（放 `player:app/commonMain`）

| Screen | 职责 | 里程碑 |
|---|---|---|
| `OnlineSearchScreen` | 全局搜索（六源 Tab）；热搜 + Tip + 历史 | M0 入口版 / M1 完整版 |
| `DiscoverScreen` | 排行榜 / 歌单广场 / 热搜 三 Tab；顶部 SourcePickerBar | M1 |
| `SonglistDetailScreen` | 歌单详情，多选整单入库 | M1 |
| `LeaderboardDetailScreen` | 排行榜单 | M1 |
| `CommentsBottomSheet` | 歌曲评论 | M2 |
| `SourcePickerBar`（组件） | 横向源切换 chips | M0 |
| `QualityPickerDialog` | 播放前音质切换 | M0 |

### 8.2 AppShell 改动

底部 / 侧栏新增 **Discover** Tab，与 Library / Playlists / Settings 平级。全局搜索图标放 AppBar 右上角，任何 Tab 都能唤出。

### 8.3 新增 Store（放 `shared:online/commonMain`，沿用现有 MVI 模式）

```kotlin
OnlineSearchStore(state = { query, activeSource, perSourceResults, loadingBySource, hotSearch, tips, history })
DiscoverStore     (state = { activeSource, leaderboards, categories, songlistsByCategory })
SonglistDetailStore(listId: OnlineListId)
CommentsStore(songId: OnlineMusicId)            // M2
```

每个 Store 订阅对应 Repository；Repository 调 `MusicSourceFacade`；Store 按源并发并聚合。

### 8.4 与现有 Store 的耦合面（共 3 个）

1. `PlayerStore.enqueue(...)` 扩展接受 `OnlineSong`（内部转 `QueueItem(origin = ONLINE)`）
2. `PlaylistsStore.addSongsToPlaylist(...)` 处理在线歌曲时先写 `online_song` 表再建关联
3. `FavoritesStore.toggleFavorite(...)` 同上

LibraryStore / SettingsStore / MusicTagsStore / Theme 全部不动。

### 8.5 复用现有能力

- **歌词**：在线 Facade 返回原文失败 → 回落到现有 `WorkflowLyricsEngine`（LrcApi / Musixmatch / Oiapi QQ / Navidrome），**0 改动**
- **响应式布局**：沿用 `LayoutProfile` 的 Mobile/Compact/Medium/Expanded 分支
- **图片**：沿用 `ArtworkBitmap` + `ArtworkCacheStore`，在线封面缓存 key = `source + songmid`
- **iOS 键盘**：沿用 `ImeAwareOutlinedTextField`
- **主题**：沿用 `LynMusicTheme`，不新增色板
- **PlayerUi** / **LyricsSearchUi** / **PlaylistsUi** / **SettingsUi** 全部复用

### 8.6 交互细节

- 播放队列内的在线歌曲显示源图标徽章
- 每首歌"更多"菜单在在线来源时多出：查看评论（M2）/ 切换音质 / 查看同名歌手作品（M1）
- 网络失败 Snackbar 区分「本源失败——切换源重试」

---

## 9. 错误处理、韧性、可观测性

### 9.1 统一异常层级

```kotlin
sealed class MusicSourceException(val sourceId: String) : Exception() {
    class SourceDisabled          : MusicSourceException(...)
    class ScriptLoadFailure       : MusicSourceException(...)
    class ScriptRuntimeError(val jsStack: String): MusicSourceException(...)
    class Timeout(val stage: String) : MusicSourceException(...)
    class Network(val code: Int?)    : MusicSourceException(...)
    class Parse                      : MusicSourceException(...)
    class UrlExpired                 : MusicSourceException(...)
    class QuotaOrBlocked             : MusicSourceException(...)
    class UpstreamChanged            : MusicSourceException(...)   // 协议变动告警
}
```

### 9.2 降级策略（让用户"能听"是 P0）

1. **搜索多源并发**：任一源失败不影响别的；失败源 UI 显示灰态 + 重试
2. **URL 失效**：401/403/404/超时 → 同源降级音质重取（sq→hq→128k）→ 仍失败则跨源 `findMusic` 替换（Kotlin 层复用 lx 同名算法）→ 仍失败才 `onError`
3. **歌词失败**：当前源 → `WorkflowLyricsEngine` 链 → 无歌词占位
4. **封面失败**：占位图；缓存 key 稳定，后续恢复
5. **JS 崩溃**：Runtime 立即 `close()` 后重建；连续 3 次崩 → 本次会话内禁用该源 + 告警

### 9.3 网络韧性

- 请求默认超时 10s；`lyn.request` 层指数退避重试最多 2 次（仅对 5xx / 超时）
- 并发限流：每源 6 / 全局 16（`Semaphore`）
- UA / Cookie / Header 完全由 js 侧生成，Kotlin 不干预

### 9.4 可观测性

- 复用现有 `GlobalDiagnosticLogger`，新增 tag `MSRC.{source}`
- 埋点到内存环形 buffer（"关于 → 诊断日志"可查）：`search-ok/err / url-ok/err / script-crash / quality-fallback / cross-source-fallback / upstream-changed`
- 崩溃上报沿用 `JvmCrashReporter / CrashReportActivity`；新增字段：`js_stack`、`source_id`、`method`

---

## 10. 测试策略

| 层 | 测试类型 | 工具 / 范围 |
|---|---|---|
| `shared:scripting` | 单元 + 冒烟 | 每平台 actual 各一个烟雾测试（`1+1` / `lyn.request` / `crypto.md5`） |
| `MusicSourceFacade` | 集成 | `jvmTest` 下 `MockWebServer` + 真 GraalVM 跑一段简化假 kw js |
| Repository / Store | 单元（TDD） | `FakeMusicSourceFacade` 注入；沿用 `commonTest` 模式 |
| UI | Compose 单元测试 | 沿用 `player/app/src/jvmTest` 已有风格（参考 `SettingsNavigationTest.kt`） |
| 录制回放 | 手动 + 脚本 | 真实响应 fixture 防协议无声断档；**CI 不跑真实网络** |

手动联通测试：`./gradlew :shared:online:smokeTest -PonlineSmoke=true`（只开发者跑）。

---

## 11. 里程碑与验收标准

### M0 核心闭环（2-3 周）— "能搜到就能听"

任务：
1. `shared:scripting` 三平台 `JsRuntime` + `JsBridge`
2. `shared:online` 骨架 + 资产管线（Node esbuild → `composeResources/files/sdk/{source}.js`）
3. `MusicSourceFacade` + `JsMusicSource`：六源接通 `search / getPlayableUrl / getLyric / getPic`
4. `OnlineMusicRepository` + `OnlineSearchStore` + `OnlineSearchScreen`（含 SourcePicker）
5. `SongUrlResolver` 接入 `PlaybackGateway.prepare`（含 URL 懒加载 + quality 降级 + 跨源 findMusic）
6. `PlayerStore / FavoritesStore / PlaylistsStore` 接受 `OnlineSong`
7. `QualityPickerDialog` + Settings 里"默认音质"项
8. 诊断日志接线

退出条件：
- 五平台（Android / iOS / macOS / Windows / Linux）各跑一次烟雾：搜"周杰伦" → 任一源命中 → 播放 → 歌词随动 → 收藏 → 重开能从收藏再播
- scripting 烟雾测试 + `FakeMusicSourceFacade` 的 Store 单测全绿
- 包体积增量：APK ≤ 4MB / ABI；iOS ipa ≤ 3MB；desktop JVM ≤ 8MB

### M1 发现层（2 周）— "像 lx 一样逛"

任务：
1. Facade 补齐 `getLeaderboards / getSonglistCategories / getSonglists / getSonglistDetail / getHotSearch / getTipSearch`
2. `LeaderboardRepository / SonglistRepository / HotSearchRepository`
3. `DiscoverStore / SonglistDetailStore`
4. `DiscoverScreen` + `SonglistDetailScreen` + `LeaderboardDetailScreen`
5. 搜索页接入 Tip / 热搜 / 历史搜索
6. 歌词翻译 / 罗马音：`OnlineLyric(original, translation?, romanization?)`；PlayerUi 歌词行切换控件

退出条件：六源排行 / 歌单 / 热搜逐源可浏览；歌单"整单加到我的歌单"端到端通；翻译歌词至少 2 源可用。

### M2 列表、元数据、评论（2 周）— "老 lx 用户能搬家"

任务：
1. `ImportLxPlaylistsUseCase`：解析 lx 的 `*.lxmf.json` / 原生 json，字段映射到 Lyn
2. 我的歌单批量：多选、批量删除、跨歌单迁移、去重
3. 临时播放队列（tempPlayList，不入库，退出清空）
4. 不喜欢列表（`dislike_rule` 新表，播放/搜索自动过滤）
5. 评论：`CommentsRepository / CommentsStore / CommentsBottomSheet`
6. 列表导出：Lyn 原生 json + lx 兼容 json 两种格式

退出条件：用户能一键导入 lx 3 种典型导出文件；dislike 规则在搜索结果和自动播放时都生效；评论分页可滚动。

### M3 扩展与长尾（2-3 周）

任务：
1. **userApi**：`UserApiRegistry`（安装/卸载/升级脚本；注册后自动进入 `sources` 列表获得全部入口）
2. **在线下载**：`OnlineDownloadManager`（Android/JVM 直写 `Downloads/LynMusic/`；iOS 走 Files App 导出）；完成后可回流到本地曲库
3. **定时退出**：`SleepTimerStore`（按时长 / 播放完当前 / 播放完 N 首）；通知/状态栏倒计时
4. **桌面歌词**（四平台）：
   - Android：`SYSTEM_ALERT_WINDOW` + 独立 Compose overlay
   - JVM：`ComposeWindow(alwaysOnTop = true, transparent = true)`
   - macOS：`NSWindow(level = floating)` + Compose
   - iOS：Live Activity / Dynamic Island（锁屏已由现有 Now Playing 处理）
5. **lx-music-sync-server 客户端**：WebSocket + 列表 / 收藏 / dislike / userApi 配置增量同步
6. **deeplink**：`lynmusic://search?q=` / `lynmusic://song?source=kw&id=...`
7. **i18n 基础**：`composeResources` 多语言；首期中英双语

退出条件：装社区 userApi 脚本后能搜索/播放；下载一首在线歌能脱机播；定时退出生效；桌面歌词在 Android / JVM / macOS 显示；同步开关能与自建 sync-server 握手。

---

## 12. 风险与对策

| 风险 | 可能性 | 影响 | 对策 |
|---|---|---|---|
| tx / wy 签名算法复杂，JS 引擎跑不通 | 中 | 阻塞 M0 | 各源独立 JsRuntime + esbuild 单源 bundle，隔离排查；先完成 kw / kg / mg / xm 四源可交付最小闭环 |
| iOS 的 JavaScriptCore KMP cinterop 无成熟样板 | 中 | 阻塞 M0 iOS 平台 | 预先搭一个 30 行 `.def` POC；最差回落到 `swift-interop` 桥 |
| macOS Compose 透明窗口渲染 bug（桌面歌词） | 低-中 | 影响 M3 一个平台 | M3 末期专项灰度；失败则退化为普通窗口 |
| 源站协议变动（lx 断维护后无上游可追） | 高（长期） | 影响持续可用性 | `UpstreamChanged` 告警 + fixture 录制回放；Kotlin 化迁移作为长期对冲 |
| 包体积超标 | 低 | 用户体验 | 源脚本 lazy evaluate；JS 引擎按 ABI 分包；Android 已有 ABI splits |
| 合规被第三方投诉 | 中-高 | 分发风险 | 用户已确认自担；文档注明"仅 GitHub 分发、不进应用商店"；保留快速下架能力 |

---

## 13. 总体规模

- **总时长预估**：8-10 周单人全职等价
- **每里程碑独立 git 分支 + 独立 PR**
- 所有新增的 Store / Repository 走 TDD（沿用现有 `commonTest` 模式）
- 每个里程碑交付时跑完 5 平台冒烟测试

---

## 14. 决策日志（便于未来追溯）

- 2026-04-17 — 目标锁定 A（内置六源在线生态）；合规自担
- 2026-04-17 — 技术路径 D（JS 引擎起步 → 渐进 Kotlin 化）
- 2026-04-17 — 工程组织 C（shared:scripting + shared:online 双模块）
- 2026-04-17 — 四里程碑划分 M0→M3；老数据兼容接入 M2
- 2026-04-17 — 全部细节按推荐：入库仅收藏/列表、URL 懒加载、M0 仅原文歌词、音质默认 + 单曲切换、合并 PlayDetail 进 PlayerUi、无硬约束
