package top.iwesley.lyn.music.scripting

/**
 * 音乐源相关错误统一为 sealed hierarchy，上层 Repository/UI 可做精准展示。
 */
sealed class MusicSourceException(
    val sourceId: String,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class SourceDisabled(sourceId: String, reason: String) : MusicSourceException(sourceId, reason)
    class ScriptLoadFailure(sourceId: String, cause: Throwable?) :
        MusicSourceException(sourceId, "script load failed", cause)

    class ScriptRuntimeError(sourceId: String, val jsStack: String, cause: Throwable?) :
        MusicSourceException(sourceId, jsStack, cause)

    class Timeout(sourceId: String, val stage: String) :
        MusicSourceException(sourceId, "timeout at $stage")

    class Network(sourceId: String, val code: Int?) :
        MusicSourceException(sourceId, "network code=$code")

    class Parse(sourceId: String, cause: Throwable?) :
        MusicSourceException(sourceId, "parse error", cause)

    class UrlExpired(sourceId: String) : MusicSourceException(sourceId, "url expired")
    class QuotaOrBlocked(sourceId: String) : MusicSourceException(sourceId, "quota or blocked")
    class UpstreamChanged(sourceId: String, reason: String) :
        MusicSourceException(sourceId, "upstream changed: $reason")
}
