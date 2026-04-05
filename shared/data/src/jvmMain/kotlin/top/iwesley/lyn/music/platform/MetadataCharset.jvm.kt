package top.iwesley.lyn.music.platform

import java.nio.charset.Charset

internal actual fun decodeMetadataBytes(bytes: ByteArray, charset: MetadataCharset): String {
    val resolved = when (charset) {
        MetadataCharset.LATIN1 -> Charsets.ISO_8859_1
        MetadataCharset.UTF16 -> Charset.forName("UTF-16")
        MetadataCharset.UTF16BE -> Charset.forName("UTF-16BE")
        MetadataCharset.UTF8 -> Charsets.UTF_8
    }
    return String(bytes, resolved)
}
