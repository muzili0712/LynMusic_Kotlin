package top.iwesley.lyn.music.platform

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPlatformImageBitmap(bytes: ByteArray?): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, bytes) {
        value = loadPlatformPreviewBitmap(bytes)
    }
    return bitmap
}

private suspend fun loadPlatformPreviewBitmap(bytes: ByteArray?): ImageBitmap? = withContext(Dispatchers.IO) {
    val payload = bytes ?: return@withContext null
    runCatching {
        BitmapFactory.decodeByteArray(payload, 0, payload.size)?.asImageBitmap()
    }.getOrNull()
}
