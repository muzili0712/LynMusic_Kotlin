package top.iwesley.lyn.music.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

@Composable
actual fun rememberPlatformImageBitmap(bytes: ByteArray?): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, bytes) {
        value = loadPlatformPreviewBitmap(bytes)
    }
    return bitmap
}

private suspend fun loadPlatformPreviewBitmap(bytes: ByteArray?): ImageBitmap? = withContext(Dispatchers.Default) {
    val payload = bytes ?: return@withContext null
    runCatching {
        Image.makeFromEncoded(payload).toComposeImageBitmap()
    }.getOrNull()
}
