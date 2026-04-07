package top.iwesley.lyn.music.core.model

data class SystemPlaybackControlCallbacks(
    val play: suspend () -> Unit = {},
    val pause: suspend () -> Unit = {},
    val togglePlayPause: suspend () -> Unit = {},
    val skipNext: suspend () -> Unit = {},
    val skipPrevious: suspend () -> Unit = {},
    val seekTo: suspend (Long) -> Unit = {},
)

interface SystemPlaybackControlsPlatformService {
    fun bind(callbacks: SystemPlaybackControlCallbacks)
    suspend fun updateSnapshot(snapshot: PlaybackSnapshot)
    suspend fun close()
}

object UnsupportedSystemPlaybackControlsPlatformService : SystemPlaybackControlsPlatformService {
    override fun bind(callbacks: SystemPlaybackControlCallbacks) = Unit

    override suspend fun updateSnapshot(snapshot: PlaybackSnapshot) = Unit

    override suspend fun close() = Unit
}
