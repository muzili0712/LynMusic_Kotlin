package top.iwesley.lyn.music.core.model

enum class AppStorageCategory {
    Artwork,
    PlaybackCache,
    LyricsShareTemp,
    TagEditTemp,
}

data class AppStorageCategoryUsage(
    val category: AppStorageCategory,
    val sizeBytes: Long,
)

data class AppStorageSnapshot(
    val totalSizeBytes: Long,
    val categories: List<AppStorageCategoryUsage>,
)

interface AppStorageGateway {
    suspend fun loadStorageSnapshot(): Result<AppStorageSnapshot>
    suspend fun clearCategory(category: AppStorageCategory): Result<Unit>
}

object UnsupportedAppStorageGateway : AppStorageGateway {
    private val error = IllegalStateException("当前平台暂不支持空间管理。")

    override suspend fun loadStorageSnapshot(): Result<AppStorageSnapshot> = Result.failure(error)

    override suspend fun clearCategory(category: AppStorageCategory): Result<Unit> = Result.failure(error)
}
