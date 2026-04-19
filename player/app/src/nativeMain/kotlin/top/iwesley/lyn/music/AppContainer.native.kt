package top.iwesley.lyn.music

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * nativeMain 是 player/app 里 Apple 系（iOS + macOS）的共享集合。
 * 用 [Platform.osFamily] 区分 ios / macos，避免重复写两份 actual。
 */
@OptIn(ExperimentalNativeApi::class)
actual fun currentPlatformTag(): String = when (Platform.osFamily) {
    kotlin.native.OsFamily.IOS -> "ios"
    kotlin.native.OsFamily.MACOSX -> "macos"
    else -> "native"
}
