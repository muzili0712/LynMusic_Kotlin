# M1 lx-music-mobile 在线生态移植 — 修遗留 & 让五平台真跑通 设计

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:writing-plans` → `superpowers:subagent-driven-development` to implement this spec. This spec is the continuation of `2026-04-17-lx-online-ecosystem-port-design.md` (M0 spec) and `2026-04-18-lx-online-m0.md` (M0 plan).

## Goal

让 M0 的"五平台搜索→播放→歌词→收藏→重开再播"核心闭环在 **iOS / macOS / Android / Windows / Linux 真机真正跑通**。M0 在 JVM+Android 编译绿但 Apple 侧 JsRuntime + PlatformCrypto 都是 stub，且 tx/wy getMusicUrl 因 `api-source` shim 只抛错，URL 拿不到；本 spec 把这两个主链缺口补齐，并修复 M0 review/调研过程中发现的两个隐藏 bug。

## Scope

11 项 M0 遗留 + 2 个 M0 隐藏 bug，共 13 项，分两阶段：

### M1.0 — 核心主链能跑（P0/P1，~1500 行）

| 序 | 项 | 为何 M1.0 | 估算行数 |
|---|---|---|---|
| M1.0-1 | **Apple JsRuntime real impl**（darwinMain 中间源集，Obj-C block register） | iOS/macOS 主链前置 | ~190 |
| M1.0-2 | **Apple PlatformCrypto real impl**（CommonCrypto+SecKey+platform.zlib+CFStringEncoding） | iOS/macOS 主链前置 | ~260 |
| M1.0-3 | **5 源 Kotlin UrlResolver**（B3 方案：tx zzcSign / wy eapi / kw mobi / kg Kotlin resolver + JS 引擎调 infSign helper / mg by=orpheus） | tx/wy getMusicUrl 当前是 stub；M1.0 让 URL 真拿到 | ~800 |
| M1.0-4 | **JsBridge 契约扩展**：`rsaEncrypt(data, pem, padding)` + `zlibInflate(data, format)` | M0 隐藏 bug，三平台适配 | ~50 |
| M1.0-5 | **register 异步桥（JVM/Android）**：`registerSync` 新接口 + `register(suspend)` 改 Promise 返回；QuickJS 用 `__lyn_makePromise` helper，GraalVM 开 top-level-await | 消除 `runBlocking` 死锁定时炸弹 | ~280 |
| M1.0-6 | **M1.0 集成冒烟**：JVM/Android/macOS 跑通 "搜 kw → 拿 URL → 播放" E2E | 验收 | ~100（test） |

**M1.0 里程碑**：健哥在 macOS 本机（JVM + macosArm64 native）能真跑 kw 源搜索并取到 URL；iOS 模拟器 + Android 真机验证同款路径。

### M1.1 — 体验完善（P2/P3，~1100 行）

| 序 | 项 | 估算行数 |
|---|---|---|
| M1.1-1 | **收藏/歌单读回在线条目** — Repository Kotlin combine merge + UI 源角标 | ~230 |
| M1.1-2 | **findMusic 3-pass + 9 层 sort 重写**（新增 `FindMusicM1` 实现同接口，保留 `FindMusicM0` 作 fallback/rollback；M2 再删 M0）；签名 `find() → List<OnlineSong>`；5 源并行 search | ~400 |
| M1.1-3 | **OnlineLyric translation/romanization/enhanced 真填充**（M0 现在全走 null） | ~30 |
| M1.1-4 | **Apple register 异步桥**（`JSValue.valueWithNewPromiseInContext`；三个 target-specific actual） | ~150 |
| M1.1-5 | **诊断日志 UI 入口**（"关于 → 诊断日志"页，只读最近 N 条 MSRC.*） | ~120 |
| M1.1-6 | **Settings UI 暴露默认音质**（设置页加 QualityPickerDialog 入口，dispatch `DefaultQualityChanged`） | ~60 |
| M1.1-7 | **Android unit test infra**（scripting `unitTests.returnDefaultValues=true` 或排除 QuickJS/compose-resources 测试）| ~20 |
| M1.1-8 | **pre-existing artwork flaky** 修复（`PlaybackRepositoriesTest.temporary_playback_artwork_override_...`） | ~30 |

**M1.1 里程碑**：所有 M0 遗留清零；五平台 `./gradlew test` 100% 绿（含 Android unit test）；UI 完整度接近 lx-music-mobile 原版。

### Out of scope（留 M2+）

- lx 老数据导入（收藏/歌单从 lx JSON 迁到本项目 Room）
- xm 源替代实现（保持 disabled，lx 自身就是 stub）
- 自定义用户 API 脚本支持（Kotlin 层 B3 路径与 lx 用户脚本生态解耦；未来 M2 若需要可加 `global.lx.apis[source]` 注入回归 B3'）
- Promise microtask queue 完整语义一致化（M1 只保证"await hostFn 能拿到值"）
- findMusic 的繁简转换（lx 原版也没做）

## 架构变更

### `shared:scripting` 契约扩展

```kotlin
// 新增：纯同步 host，不进 Promise 桥
interface JsRuntime {
    suspend fun evaluate(script: String, name: String): JsValue
    suspend fun invoke(path: String, vararg args: JsValue): JsValue
    fun register(name: String, host: HostFunction)          // 保留，语义改为返 Promise
    fun registerSync(name: String, host: (List<JsValue>) -> JsValue)  // 新增，同步直返
    // ... dispatcher, sourceId 不变
}
```

**语义变更**：`register(name, suspend host)` 现在同步返回一个 JS Promise 给 JS 侧，Kotlin 侧在 dispatcher 协程 scope 上 `launch { resolve(host(args)) }`。这是**行为破坏性变更**，但 ABI 兼容（签名不变）。调用方需要更新：`JsMusicSource.registerBridgeFunctions` 把 17 个同步 host 迁到 `registerSync`，保留 `__lyn_request`/`__lyn_setTimeout` 用 `register`。

### `shared:online` 契约扩展

```kotlin
interface JsBridge {
    // 扩展：rsaEncrypt 加 padding 参数
    fun rsaEncrypt(data: ByteArray, publicKeyPem: String, padding: String = "PKCS1"): ByteArray
    // 扩展：zlibInflate 加 format 参数（auto / zlib / raw / gzip）
    fun zlibInflate(input: ByteArray, format: String = "auto"): ByteArray
    // 其余 17 个方法签名不变
}
```

### `shared:online` 新增 resolve 层

```kotlin
// shared/online/src/commonMain/kotlin/.../resolve/
interface SourceUrlResolver {
    suspend fun resolve(songmid: String, quality: Quality, context: OnlineSong?): PlayableUrl
}
class TxUrlResolver(http, crypto, cookieStore) : SourceUrlResolver
class WyUrlResolver(http, crypto, cookieStore) : SourceUrlResolver
class KwUrlResolver(http, crypto) : SourceUrlResolver
class KgUrlResolver(http, crypto, infSignLoader) : SourceUrlResolver
class MgUrlResolver(http, crypto) : SourceUrlResolver

// SongUrlResolver 改造：
class DefaultSongUrlResolver(
    private val repository: OnlineMusicRepository,
    private val findMusic: FindMusic,    // ← 改 M1 新 FindMusic
    private val sourceResolvers: Map<String, SourceUrlResolver> = emptyMap(), // ← M1 注入
) : SongUrlResolver {
    override suspend fun resolve(id, preferredQuality, context): ResolvedUrl {
        val sourceResolver = sourceResolvers[id.source]
        // M1.0: 优先走 Kotlin resolver
        if (sourceResolver != null) { ...try sourceResolver.resolve... }
        // fallback: M0 走 JS（留 rollback 窗口）
        ...
    }
}
```

### `shared:online` findMusic 重写

```kotlin
// 新签名
interface FindMusic {
    suspend fun find(target: OnlineSong, excludeSource: String): List<OnlineSong>
}
class FindMusicM1(
    private val repository: OnlineMusicRepository,
    private val totalTimeoutMs: Long = 12_000,
) : FindMusic { ... }

// 保留 FindMusicM0（M2 删除）
```

### Apple 代码组织

新增 **`darwinMain` 中间源集**（手写，不靠 `applyDefaultHierarchyTemplate`）在 `shared:scripting` 和 `shared:online` build.gradle.kts 里：

```kotlin
kotlin {
    sourceSets {
        val darwinMain by creating { dependsOn(appleMain.get()) }
        val darwinTest by creating { dependsOn(appleTest.get()) }
        iosArm64Main by getting { dependsOn(darwinMain) }
        iosSimulatorArm64Main by getting { dependsOn(darwinMain) }
        macosArm64Main by getting { dependsOn(darwinMain) }
        // iOS/macOS Test 同
    }
}
```

**darwinMain 相对 appleMain 的优势**：`platform.JavaScriptCore.JSValue` 的 category methods（isNull/isNumber/...）在 darwinMain 可见（非 commonized），在 appleMain 不可见。

## 每项技术方案摘要

### M1.0-1 Apple JsRuntime real impl

- 路径：`shared/scripting/src/darwinMain/kotlin/.../JsCoreRuntime.darwin.kt`（删 appleMain stub）
- Dispatcher：沿用 `newSingleThreadContext`
- 值转换：JS→Kotlin 用 category methods（`isNull/isNumber/isString/isArray/valueForProperty`）；对象用 `JSON.stringify` 兜底
- register：Kotlin lambda `(NSArray?) -> NSObject?` → `JSValue.valueWithObject:inContext:` 自动包 Obj-C block → JS callable
- close：显式置 `globalObject[name] = undefined` 防止 VM retain cycle

### M1.0-2 Apple PlatformCrypto real impl

- 路径：`shared/online/src/darwinMain/kotlin/.../PlatformCrypto.darwin.kt`（删 appleMain stub）
- MD5/SHA1/SHA256：`platform.CommonCrypto.CC_*`
- AES：`CCCrypt` + `kCCOptionPKCS7Padding | kCCOptionECBMode?`
- RSA：`SecKeyCreateWithData` + `SecKeyCreateEncryptedData`；处理 X.509 SPKI（wy 源用）和 PKCS#1 DER；**padding 参数**（`PKCS1` / `NoPadding`）
- DES：保持 `NotImplementedError`（已确认五源无调用），但错误消息加 "no lx source uses DES as of bundle-$SHA"
- zlib：`platform.zlib.inflateInit2(windowBits=47)` 自动识别 zlib/gzip，或 `-15` for raw；**format 参数**
- iconv：`CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000)` + `NSString dataUsingEncoding:`

### M1.0-3 5 源 Kotlin UrlResolver

每个源一个独立文件 + Unit test（用 ktor MockEngine stub 官方 API 响应），公共依赖 HttpClient + PlatformCrypto + CookieJar + 平台设备指纹（tx 需要 guid/wid）。

- **tx**：`zzcSign`（SHA1-based），请求 `u.y.qq.com/cgi-bin/musics.fcg`，需 `Cookie: uin; musicid; qqmusic_key`
- **wy**：`eapi` 签名（AES-CBC key + RSA NoPadding），请求 `/api/song/enhance/player/url/v1`
- **kw**：`api.kuwo.cn/api/v1/www/music/playUrl`，参数带 `secret` (md5(tkm_... + mid))
- **kg**：`infSign.min.js` 加载成 Kotlin helper 或继续让 JS 引擎跑（折衷：kg 的 sign 太复杂，考虑沿用 JS invoke 这条 sign 函数）
- **mg**：`app.c.nf.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do` + `by=orpheus` + 简单签名

首启时生成并持久化 `guid / wid / deviceId` 到 `SettingsRepository`（新增 `deviceFingerprintKey` 字段）。

### M1.0-4 JsBridge 契约扩展

- `rsaEncrypt(data, pem, padding)`：JVM 用 `RSA/ECB/${padding}Padding`（PKCS1Padding / NoPadding → `RSA/ECB/NoPadding`）；Android 复用；Apple 用 `kSecKeyAlgorithmRSAEncryptionPKCS1` / `kSecKeyAlgorithmRSAEncryptionRaw`
- `zlibInflate(input, format)`：JVM 用 `Inflater(true)` for raw，`Inflater()` for zlib，`GZIPInputStream` for gzip，`auto` 检测 magic bytes（0x1F8B = gzip，0x78/0x78DA = zlib，其他 = raw）；Android 复用；Apple 用 `platform.zlib.inflateInit2` 各自 windowBits

### M1.0-5 register 异步桥

**GraalVM**：
- `Context.Builder` 加 `.option("js.top-level-await", "true")`（让 `rt.evaluate("await ...")` 顶层 await 可用；不影响五源 bundle 的 IIFE/CommonJS 解析，已核对 bundle 产物顶层无 await）
- `register` 实现：构造 JS Promise（捕获 resolve/reject proxy），同步返回给 JS；后台 `scope.launch(dispatcher) { resolve.execute(host(args).toHost()) }`
- `registerSync` 实现：保留 M0 风格 `ProxyExecutable { ... host(args) }` 同步返回

**QuickJS**：
- bootstrap 脚本注入 `__lyn_makePromise = () => { let r,j; const p = new Promise((a,b)=>{r=a;j=b;}); return [p, r, j]; }`
- `register` 实现：在 JSCallFunction 回调里调 `__lyn_makePromise` 拿三元组，同步返回 promise；后台 `scope.launch(dispatcher) { resolve.call(host(args).toHost()) }` 注意所有 JSFunction.call 必须在 dispatcher 线程
- `registerSync` 实现：JSCallFunction 里同步返回 host 结果

**Apple**（M1.1 做）：见 M1.1-4。

**调用方更新**：`JsMusicSource.registerBridgeFunctions` 17 个同步 host 迁 `registerSync`，2 个 suspend host 继续 `register`。

### M1.0-6 E2E 冒烟

新增 `commonTest/KwSearchE2ETest.kt`（`@Ignore` 默认，手动开）：
- 用真实 `JsRuntime` + 真实 `JsMusicSource` + 真实 `JsBridgeImpl` + MockEngine stub ktor 响应
- 验证 search("周杰伦") 能返回解析后的 `SearchPage<OnlineSong>`
- getPlayableUrl 通过 KwUrlResolver 能返回 PlayableUrl
- JVM / macosArm64 / Android 各跑一次

### M1.1-1 收藏/歌单读回在线条目

- `OnlineSongDao` 新增 `observeAll(): Flow<List<OnlineSongEntity>>`
- `FavoritesRepositories.favoriteTracks` 和 `PlaylistsRepositories.observePlaylistDetail` 的 `combine` 加一路 `onlineSongDao.observeAll()`，merge 时 `if (row.origin == "ONLINE") onlineByKey[...].toDomainTrack() else localById[...]`
- `OnlineSongEntity.toDomainTrack(sourceId, trackId)` 新 mapper
- **关键**：`enabledSourceIds` 过滤中显式放行 `origin='ONLINE'`
- UI：`TrackRow` 加可选 `onlineSourceBadge`（解析 mediaLocator scheme 拿 source code 显示 "kw"/"wy"）

### M1.1-2 findMusic 重写

- 新 `FindMusic` 接口 + `FindMusicM1` 实现
- `filterStr` 实现：`trim → toLowerCase → 去 \s/'/./,/，/&/"/、/()/（）/<>/|/\/[]/!/！`；singer 按 `、 & ; ； / , ， |` split + localeCompare 升序排序后重连
- 3 pass + 9 层 sort（参见调研 #4 详细流程）
- 5 源并行 `async + awaitAll`；`withTimeoutOrNull(12_000)` 总超时
- `FindMusic.find()` 签名返 `List<OnlineSong>`；`SongUrlResolver` 配合遍历尝试

### M1.1-3 OnlineLyric 真填充

- M0 的 `JsMusicSource.getLyric` 其实已经尝试取 `tlyric/lxlyric`，但没取 `romanization`。
- 扫 lx 源 `getLyric` 返回结构，补 romanization 字段路径（可能是 `rlyric` 或 `romalrc`）
- `OnlineLyric` 类型已就位（T4 定义了字段），只改 mapper

### M1.1-4 Apple register 异步桥

- `JSValue.valueWithNewPromiseInContext(ctx) { resolve, reject in ... }`
- Obj-C block 拿到 resolve/reject JSValue，后台协程 fulfill
- `registerSync` 路径：直接同步返回
- 路径：`shared/scripting/src/darwinMain/kotlin/.../JsCoreRuntime.darwin.kt`（M1.0-1 时先占位，M1.1 填真）

### M1.1-5 诊断日志 UI 入口

- `GlobalDiagnosticLogger` 扩一个 `RingBufferLogger(capacity=500)` 作为 strategy
- 新 Screen `DiagnosticsLogScreen.kt` 读 ring buffer 展示（tag / level / message / timestamp）
- 在 AppShell "关于" 页面加入口路由

### M1.1-6 Settings UI 默认音质

- 设置页加一个条目"默认音质"，点击打开 `QualityPickerDialog`
- dispatch `SettingsIntent.DefaultQualityChanged(lxKey)`
- 显示当前 `settingsState.defaultQualityLxKey`

### M1.1-7 Android unit test infra

- `shared:scripting:testDebugUnitTest` + `:shared:online:testDebugUnitTest` 加 `android { testOptions { unitTests.returnDefaultValues = true } }`
- 或者直接在 build.gradle.kts 里排除那些依赖 native so 的测试：`testDebug { exclude("**/*QuickJsRuntime*") }`
- 目标：`./gradlew test` 全绿

### M1.1-8 artwork flaky 修复

- 读 `PlaybackRepositoriesTest.temporary_playback_artwork_override_...` 两个测试
- 根因可能是 coroutine 调度顺序 + SQLite statement 生命周期
- 改为 event-based wait 或修 implementation

## 依赖拓扑（子任务推荐派发顺序）

### M1.0 依赖

```
M1.0-4 (JsBridge 契约扩展) — 前置
  └─ M1.0-1 (Apple JsRuntime)  [独立，darwinMain sourceset]
  └─ M1.0-2 (Apple PlatformCrypto) [依赖 M1.0-4 的新契约]
       └─ M1.0-3 (5 源 UrlResolver) [依赖 M1.0-2/4 的 JsBridge 完整实现；Apple 后也可跑]
            └─ M1.0-6 (E2E 冒烟)

M1.0-5 (register 异步桥) [独立，仅 shared:scripting JVM+Android 改动]
  └─ 整合到 M1.0-6 的 E2E
```

### M1.1 依赖

```
M1.1-2 (findMusic 重写) [独立]
  └─ 改 SongUrlResolver 使用新接口

M1.1-1 (收藏读回) [独立，Room+Repository 改动]
M1.1-3 (lyric 真填充) [独立，M0 mapper 扩展]
M1.1-4 (Apple register 异步桥) [依赖 M1.0-1 darwinMain 就位]
M1.1-5/6/7/8 (UI / test infra / bug fix) [独立]
```

## 工程量总结

| 阶段 | 代码 | 测试 | 合计 |
|---|---|---|---|
| M1.0 | ~1500 | ~400 | ~1900 |
| M1.1 | ~1100 | ~300 | ~1400 |
| **合计** | **~2600** | **~700** | **~3300** |

## 风险与已知坑

1. **RSA NoPadding X.509 SPKI 剥头**：wy 的 publicKey 是 X.509 SPKI，SecKey API 要 PKCS#1 raw；需硬编码剥 24 字节 ASN.1 前缀（或用 SecItemImport，但它要 keychain 权限，过重）
2. **5 源签名算法随 lx 客户端版本漂移**：建议把各 UrlResolver 内部的 key/salt/endpoint 做成 class constructor 参数，未来"签名模块更新包"可以 inject 替换
3. **tx/wy 反爬风控**：2024+ 对 Cookie/IP 风控严；M1.0 仅保证单机小流量 OK，高频会被 403；设置 retry + UA 轮换 延到 M2
4. **kg infSign.min.js**：已被 bundle 进 JS 资产，考虑保留 JS 引擎跑这个签名（而不是 Kotlin 重写），kg UrlResolver 内部通过 `jsRuntime.invoke("kg_infSign", params)` 拿签名
5. **Promise 行为破坏性变更**：M0 `register(suspend host)` 的 runBlocking 同步阻塞语义在 M1 变为 Promise 返回；需要 M1.0-5 的测试覆盖 + CHANGELOG 明记
6. **QuickJS JSObject 引用泄漏**：`{p, resolve, reject}` 持有后必须显式 release，否则大量请求 OOM
7. **darwinMain commonization 警告**：可能出现 "intermediate source set not in known template"，`kotlin.mpp.applyDefaultHierarchyTemplate=false` 下不阻塞
8. **M0 已接受的缺口继续存在**：Promise microtask queue 语义一致化（只保证 await 能拿值，不保证 then 链顺序）；DES 不实现
9. **findMusic `localeCompare` 跨平台**：JS 用 UCA，Kotlin String 用 UTF-16 码点；中文排序可能不同，用 `Collator` 或固定比较器 + snapshot test
10. **OnlineSongDao.observeAll** 流重组性能：收藏 1k+ 时每次变化全列表重算，M2 改窄 Flow

## 测试策略

- 每个 M1 子项对应 2-5 个 TDD 测试（commonTest 优先）
- 5 源 UrlResolver 各 1 个 `@Ignore` 的 integration test（网络 flaky，手动开）
- M1.0 E2E：`KwSearchE2ETest` 在 JVM + macosArm64 + Android 各跑一次（ktor MockEngine）
- M1.1 完工后验收：`./gradlew test` 全绿（含 Android unit test）
- 真机冒烟：M1.0 完 + M1.1 完 各一轮，健哥回填 M0_SMOKE_TEST.md

## 不可回撤决策

- **M1.0/M1.1 分阶段**：M1.0 必须先于 M1.1；M1.0 里程碑是 "iOS/macOS 能真跑搜索+播放"
- **B3 Kotlin UrlResolver**：放弃 B1（JS shim 真实现）方案；不做 user_api 脚本兼容
- **darwinMain 中间源集**：不加回 `applyDefaultHierarchyTemplate()`；不走 expect/actual typealias 或 3 份重复文件
- **xm 源保持 disabled**：M2 之前不找替代
- **双 register 接口**：`register(suspend)` + `registerSync` 并存，不强行合一
- **JsBridge 契约扩展字段用默认值**：`rsaEncrypt` 第三参数默认 `"PKCS1"`，`zlibInflate` 第二参数默认 `"auto"`——向后兼容 M0 调用

## 交付节奏

- **M1.0**：按依赖拓扑用 subagent-driven-development 串行执行（每项 implementer → spec review → quality review），预计 6-8 个 session 小时
- **M1.0 完工后停**：健哥真机冒烟 iOS/macOS/Android；若 OK 进 M1.1，若发现主链问题先修
- **M1.1**：同样串行，6 个 subtask，预计 4-6 小时
- **M1 完工**：M0_SMOKE_TEST.md 回填 5 平台实测数据；memory 更新；考虑 push + PR

---

**本 spec 基于**：
- M0 spec `docs/superpowers/specs/2026-04-17-lx-online-ecosystem-port-design.md`
- M0 plan `docs/superpowers/plans/2026-04-18-lx-online-m0.md`
- M0 session 执行记录（commits `ea7773d..308fcf0`，18 commits）
- 5 份 M1 调研（Apple JSC / api-source / 收藏读回 / findMusic / register 异步桥）— 未提交的 markdown，内容已融入本 spec
