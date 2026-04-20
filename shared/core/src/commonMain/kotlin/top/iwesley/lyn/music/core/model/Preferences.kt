package top.iwesley.lyn.music.core.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface SambaCachePreferencesStore {
    val useSambaCache: StateFlow<Boolean>

    suspend fun setUseSambaCache(enabled: Boolean)
}

interface ThemePreferencesStore {
    val selectedTheme: StateFlow<AppThemeId>
    val customThemeTokens: StateFlow<AppThemeTokens>
    val textPalettePreferences: StateFlow<AppThemeTextPalettePreferences>

    suspend fun setSelectedTheme(themeId: AppThemeId)
    suspend fun setCustomThemeTokens(tokens: AppThemeTokens)
    suspend fun setTextPalette(themeId: AppThemeId, palette: AppThemeTextPalette)
}

interface CompactPlayerLyricsPreferencesStore {
    val showCompactPlayerLyrics: StateFlow<Boolean>

    suspend fun setShowCompactPlayerLyrics(enabled: Boolean)
}

object UnsupportedCompactPlayerLyricsPreferencesStore : CompactPlayerLyricsPreferencesStore {
    private val mutableShowCompactPlayerLyrics = MutableStateFlow(false)

    override val showCompactPlayerLyrics: StateFlow<Boolean> = mutableShowCompactPlayerLyrics

    override suspend fun setShowCompactPlayerLyrics(enabled: Boolean) {
        mutableShowCompactPlayerLyrics.value = enabled
    }
}

/**
 * 在线歌曲默认音质偏好（lx-music SDK 约定的 lxKey 字符串，如 "320k"/"flac"）。
 * 存字符串以便平台直接持久化，UI/业务层再按需映射为 Quality 枚举。
 */
interface DefaultQualityPreferencesStore {
    val defaultQualityKey: StateFlow<String>

    suspend fun setDefaultQualityKey(key: String)
}

object UnsupportedDefaultQualityPreferencesStore : DefaultQualityPreferencesStore {
    private val mutableDefaultQualityKey = MutableStateFlow(DEFAULT_QUALITY_FALLBACK_KEY)

    override val defaultQualityKey: StateFlow<String> = mutableDefaultQualityKey

    override suspend fun setDefaultQualityKey(key: String) {
        mutableDefaultQualityKey.value = key
    }
}

const val DEFAULT_QUALITY_FALLBACK_KEY: String = "320k"

/**
 * lx-music 设备指纹持久化：部分源（tx 的 guid/wid、wy 的 deviceId）签名算法依赖
 * 跨会话稳定的 fingerprint；首启后必须持久化到各平台偏好里，避免源侧风控拒绝。
 *
 * 存储格式为单串 "guid|wid|deviceId"（由 [top.iwesley.lyn.music.online.resolve.DeviceFingerprint]
 * 的三字段按管道分隔拼接，空字符串表示尚未生成）。把三段并在一起主要是为了复用已有
 * key-value 存储通道；未来要扩三段之外的字段，可以再切 JSON 重编码。
 */
interface DeviceFingerprintPreferencesStore {
    val fingerprint: StateFlow<String>

    suspend fun setFingerprint(value: String)
}

object UnsupportedDeviceFingerprintPreferencesStore : DeviceFingerprintPreferencesStore {
    private val mutableFingerprint = MutableStateFlow("")

    override val fingerprint: StateFlow<String> = mutableFingerprint

    override suspend fun setFingerprint(value: String) {
        mutableFingerprint.value = value
    }
}

interface DesktopVlcPreferencesStore {
    val desktopVlcManualPath: StateFlow<String?>
    val desktopVlcAutoDetectedPath: StateFlow<String?>
    val desktopVlcEffectivePath: StateFlow<String?>

    suspend fun setDesktopVlcManualPath(path: String?)
    suspend fun setDesktopVlcAutoDetectedPath(path: String?)
}

object UnsupportedDesktopVlcPreferencesStore : DesktopVlcPreferencesStore {
    private val mutableManualPath = MutableStateFlow<String?>(null)
    private val mutableAutoDetectedPath = MutableStateFlow<String?>(null)
    private val mutableEffectivePath = MutableStateFlow<String?>(null)

    override val desktopVlcManualPath: StateFlow<String?> = mutableManualPath
    override val desktopVlcAutoDetectedPath: StateFlow<String?> = mutableAutoDetectedPath
    override val desktopVlcEffectivePath: StateFlow<String?> = mutableEffectivePath

    override suspend fun setDesktopVlcManualPath(path: String?) {
        mutableManualPath.value = path?.takeIf { it.isNotBlank() }
        mutableEffectivePath.value = mutableManualPath.value ?: mutableAutoDetectedPath.value
    }

    override suspend fun setDesktopVlcAutoDetectedPath(path: String?) {
        mutableAutoDetectedPath.value = path?.takeIf { it.isNotBlank() }
        mutableEffectivePath.value = mutableManualPath.value ?: mutableAutoDetectedPath.value
    }
}
