package top.iwesley.lyn.music.feature.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.iwesley.lyn.music.core.model.AppThemeId
import top.iwesley.lyn.music.core.model.AppThemeTextPalette
import top.iwesley.lyn.music.core.model.AppThemeTextPalettePreferences
import top.iwesley.lyn.music.core.model.AppThemeTokens
import top.iwesley.lyn.music.core.model.LyricsSourceConfig
import top.iwesley.lyn.music.core.model.LyricsSourceDefinition
import top.iwesley.lyn.music.core.model.WorkflowLyricsSourceConfig
import top.iwesley.lyn.music.core.model.defaultCustomThemeTokens
import top.iwesley.lyn.music.core.model.defaultThemeTextPalettePreferences
import top.iwesley.lyn.music.core.model.withThemePalette
import top.iwesley.lyn.music.data.repository.SettingsRepository
import top.iwesley.lyn.music.domain.parseWorkflowLyricsSourceConfig

/**
 * 测试共享的 SettingsRepository Fake：支持按构造参数预置各偏好，以及对 write-through 的断言。
 *
 * 与平台实现保持行为一致：
 * - `useSambaCache` / `showCompactPlayerLyrics` / `defaultQualityKey` 等 StateFlow 随 set* 同步更新；
 * - 歌词源名称去重规则与 `DefaultSettingsRepository.assertUniqueLyricsSourceName` 等价；
 * - workflow 源修改时禁止改 id，与正式实现一致。
 */
internal class FakeSettingsRepository(
    sources: List<LyricsSourceDefinition> = emptyList(),
    showCompactPlayerLyrics: Boolean = false,
    defaultQualityKey: String = "320k",
    selectedTheme: AppThemeId = AppThemeId.Ocean,
    customThemeTokens: AppThemeTokens = defaultCustomThemeTokens(),
    textPalettePreferences: AppThemeTextPalettePreferences = defaultThemeTextPalettePreferences(),
    desktopVlcAutoDetectedPath: String? = null,
    desktopVlcManualPath: String? = null,
) : SettingsRepository {
    private val mutableSources = MutableStateFlow(sources)
    private val mutableUseSambaCache = MutableStateFlow(false)
    private val mutableShowCompactPlayerLyrics = MutableStateFlow(showCompactPlayerLyrics)
    private val mutableDefaultQualityKey = MutableStateFlow(defaultQualityKey)
    private val mutableSelectedTheme = MutableStateFlow(selectedTheme)
    private val mutableCustomThemeTokens = MutableStateFlow(customThemeTokens)
    private val mutableTextPalettePreferences = MutableStateFlow(textPalettePreferences)
    private val mutableDesktopVlcAutoDetectedPath = MutableStateFlow(desktopVlcAutoDetectedPath)
    private val mutableDesktopVlcManualPath = MutableStateFlow(desktopVlcManualPath)
    private val mutableDesktopVlcEffectivePath = MutableStateFlow(
        desktopVlcManualPath ?: desktopVlcAutoDetectedPath,
    )

    override val lyricsSources: Flow<List<LyricsSourceDefinition>> = mutableSources.asStateFlow()
    override val useSambaCache: StateFlow<Boolean> = mutableUseSambaCache.asStateFlow()
    override val showCompactPlayerLyrics: StateFlow<Boolean> = mutableShowCompactPlayerLyrics.asStateFlow()
    override val defaultQualityKey: StateFlow<String> = mutableDefaultQualityKey.asStateFlow()
    override val selectedTheme: StateFlow<AppThemeId> = mutableSelectedTheme.asStateFlow()
    override val customThemeTokens: StateFlow<AppThemeTokens> = mutableCustomThemeTokens.asStateFlow()
    override val textPalettePreferences: StateFlow<AppThemeTextPalettePreferences> = mutableTextPalettePreferences.asStateFlow()
    override val desktopVlcAutoDetectedPath: StateFlow<String?> = mutableDesktopVlcAutoDetectedPath.asStateFlow()
    override val desktopVlcManualPath: StateFlow<String?> = mutableDesktopVlcManualPath.asStateFlow()
    override val desktopVlcEffectivePath: StateFlow<String?> = mutableDesktopVlcEffectivePath.asStateFlow()

    var setCustomThemeTokensCalls: Int = 0
        private set

    fun currentSources(): List<LyricsSourceDefinition> = mutableSources.value
    fun currentShowCompactPlayerLyrics(): Boolean = mutableShowCompactPlayerLyrics.value
    fun currentDefaultQualityKey(): String = mutableDefaultQualityKey.value
    fun currentSelectedTheme(): AppThemeId = mutableSelectedTheme.value
    fun currentCustomThemeTokens(): AppThemeTokens = mutableCustomThemeTokens.value
    fun currentTextPalettePreferences(): AppThemeTextPalettePreferences = mutableTextPalettePreferences.value
    fun currentDesktopVlcManualPath(): String? = mutableDesktopVlcManualPath.value

    override suspend fun ensureDefaults() = Unit

    override suspend fun setUseSambaCache(enabled: Boolean) {
        mutableUseSambaCache.value = enabled
    }

    override suspend fun setShowCompactPlayerLyrics(enabled: Boolean) {
        mutableShowCompactPlayerLyrics.value = enabled
    }

    override suspend fun setDefaultQualityKey(key: String) {
        mutableDefaultQualityKey.value = key
    }

    override suspend fun setSelectedTheme(themeId: AppThemeId) {
        mutableSelectedTheme.value = themeId
    }

    override suspend fun setCustomThemeTokens(tokens: AppThemeTokens) {
        setCustomThemeTokensCalls += 1
        mutableCustomThemeTokens.value = tokens
    }

    override suspend fun setTextPalette(themeId: AppThemeId, palette: AppThemeTextPalette) {
        mutableTextPalettePreferences.value = mutableTextPalettePreferences.value.withThemePalette(themeId, palette)
    }

    override suspend fun setDesktopVlcManualPath(path: String) {
        mutableDesktopVlcManualPath.value = path
        mutableDesktopVlcEffectivePath.value = path
    }

    override suspend fun clearDesktopVlcManualPath() {
        mutableDesktopVlcManualPath.value = null
        mutableDesktopVlcEffectivePath.value = mutableDesktopVlcAutoDetectedPath.value
    }

    override suspend fun saveLyricsSource(config: LyricsSourceConfig) {
        val normalized = normalizeLyricsSourceName(config.name)
        if (mutableSources.value.any { it.id != config.id && normalizeLyricsSourceName(it.name) == normalized }) {
            error("歌词源名称已存在。")
        }
        mutableSources.value = (mutableSources.value.filterNot { it.id == config.id } + config)
            .sortedWith(compareByDescending<LyricsSourceDefinition> { it.priority }.thenBy { it.name.lowercase() })
    }

    override suspend fun saveWorkflowLyricsSource(rawJson: String, editingId: String?): WorkflowLyricsSourceConfig {
        val parsed = parseWorkflowLyricsSourceConfig(rawJson)
        val config = parsed
        if (editingId != null && config.id != editingId) {
            error("Workflow 源 id 不支持修改。")
        }
        val normalized = normalizeLyricsSourceName(config.name)
        if (mutableSources.value.any { it.id != config.id && normalizeLyricsSourceName(it.name) == normalized }) {
            error("歌词源名称已存在。")
        }
        mutableSources.value = (mutableSources.value.filterNot { it.id == config.id } + config)
            .sortedWith(compareByDescending<LyricsSourceDefinition> { it.priority }.thenBy { it.name.lowercase() })
        return config
    }

    override suspend fun setLyricsSourceEnabled(sourceId: String, enabled: Boolean) = Unit

    override suspend fun deleteLyricsSource(configId: String) {
        mutableSources.value = mutableSources.value.filterNot { it.id == configId }
    }
}

private fun normalizeLyricsSourceName(value: String): String = value.trim().lowercase()
