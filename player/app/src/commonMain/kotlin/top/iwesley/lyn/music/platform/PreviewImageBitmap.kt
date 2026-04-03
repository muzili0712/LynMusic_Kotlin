package top.iwesley.lyn.music.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberPlatformImageBitmap(bytes: ByteArray?): ImageBitmap?
