package top.iwesley.lyn.music.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal suspend fun readIosRemoteBytes(target: String): ByteArray? = withContext(Dispatchers.Default) {
    val url = NSURL.URLWithString(target) ?: return@withContext null
    NSData.create(contentsOfURL = url, options = 0u, error = null)?.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
internal fun readIosLocalBytes(path: String): ByteArray? {
    val file = fopen(path, "rb") ?: return null
    return try {
        if (fseek(file, 0, SEEK_END) != 0) return null
        val byteCount = ftell(file).toInt()
        if (byteCount < 0) return null
        if (fseek(file, 0, SEEK_SET) != 0) return null
        val byteArray = ByteArray(byteCount)
        val bytesRead = byteArray.usePinned { pinned ->
            fread(
                pinned.addressOf(0).reinterpret<ByteVar>(),
                1.convert(),
                byteCount.convert(),
                file,
            ).toInt()
        }
        if (bytesRead != byteCount) return null
        byteArray
    } finally {
        fclose(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun writeIosFileBytes(path: String, bytes: ByteArray): Boolean {
    val file = fopen(path, "wb") ?: return false
    return try {
        val written = bytes.usePinned { pinned ->
            fwrite(
                pinned.addressOf(0),
                1.convert(),
                bytes.size.convert(),
                file,
            ).toInt()
        }
        written == bytes.size
    } finally {
        fclose(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun filePathFromIosLocator(target: String): String {
    return if (target.startsWith("file://", ignoreCase = true)) {
        NSURL.URLWithString(target)?.path ?: target.removePrefix("file://")
    } else {
        target
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val byteCount = length.toInt()
    if (byteCount <= 0) return ByteArray(0)
    val byteArray = ByteArray(byteCount)
    byteArray.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return byteArray
}
