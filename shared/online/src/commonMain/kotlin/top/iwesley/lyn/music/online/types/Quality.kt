package top.iwesley.lyn.music.online.types

/**
 * 音质等级。
 *
 * `lxKey` 与 lx-music-mobile SDK 约定一致，作为 JS 侧传入/传出时的 discriminator；
 * `displayName` 仅供 UI 呈现；`bitrate` 为典型 bps 参考值，便于粗略排序与展示。
 *
 * `degradationOrder` 定义从高到低的降级检索顺序：FLAC24BIT → FLAC → WAV → APE → 320K → 192K → 128K。
 * 播放层在首选音质缺失时按此序列依次回退。
 */
enum class Quality(
    val lxKey: String,
    val displayName: String,
    val bitrate: Int,
) {
    FLAC24BIT(lxKey = "flac24bit", displayName = "Hi-Res FLAC", bitrate = 2_000_000),
    FLAC(lxKey = "flac", displayName = "FLAC", bitrate = 1_000_000),
    WAV(lxKey = "wav", displayName = "WAV", bitrate = 1_411_200),
    APE(lxKey = "ape", displayName = "APE", bitrate = 900_000),
    K320(lxKey = "320k", displayName = "320K MP3", bitrate = 320_000),
    K192(lxKey = "192k", displayName = "192K MP3", bitrate = 192_000),
    K128(lxKey = "128k", displayName = "128K MP3", bitrate = 128_000);

    companion object {
        /**
         * 返回与 `key` 匹配（忽略大小写）的 [Quality]，未命中时返回 `null`。
         */
        fun fromLxKey(key: String): Quality? =
            entries.firstOrNull { it.lxKey.equals(key, ignoreCase = true) }

        /**
         * 音质降级顺序：从高到低。
         */
        val degradationOrder: List<Quality> =
            listOf(FLAC24BIT, FLAC, WAV, APE, K320, K192, K128)
    }
}
