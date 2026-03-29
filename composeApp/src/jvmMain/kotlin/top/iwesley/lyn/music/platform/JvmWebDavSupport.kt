package top.iwesley.lyn.music.platform

import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import com.github.sardine.impl.SardineException
import com.github.sardine.impl.SardineImpl
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.ssl.SSLContexts
import uk.co.caprica.vlcj.media.callback.CallbackMedia
import uk.co.caprica.vlcj.media.callback.seekable.SeekableCallbackMedia
import top.iwesley.lyn.music.core.model.buildBasicAuthorizationHeader
import top.iwesley.lyn.music.core.model.DiagnosticLogger
import top.iwesley.lyn.music.core.model.ImportScanReport
import top.iwesley.lyn.music.core.model.ImportedTrackCandidate
import top.iwesley.lyn.music.core.model.SecureCredentialStore
import top.iwesley.lyn.music.core.model.WebDavSourceDraft
import top.iwesley.lyn.music.core.model.buildWebDavLocator
import top.iwesley.lyn.music.core.model.buildWebDavTrackUrl
import top.iwesley.lyn.music.core.model.debug
import top.iwesley.lyn.music.core.model.describeWebDavHttpFailure
import top.iwesley.lyn.music.core.model.error
import top.iwesley.lyn.music.core.model.info
import top.iwesley.lyn.music.core.model.normalizeWebDavRootUrl
import top.iwesley.lyn.music.core.model.parseWebDavLocator
import top.iwesley.lyn.music.core.model.resolveWebDavRelativePath
import top.iwesley.lyn.music.core.model.warn
import top.iwesley.lyn.music.data.db.LynMusicDatabase

internal data class JvmWebDavPlaybackTarget(
    val media: CallbackMedia,
    val requestUrl: String,
)

internal suspend fun scanJvmWebDav(
    draft: WebDavSourceDraft,
    sourceId: String,
    logger: DiagnosticLogger,
): ImportScanReport {
    val rootUrl = normalizeWebDavRootUrl(draft.rootUrl)
    val authEnabled = draft.username.isNotBlank()
    val tracks = mutableListOf<ImportedTrackCandidate>()
    val startedAt = System.currentTimeMillis()
    logger.info(WEBDAV_LOG_TAG) {
        "scan-start source=$sourceId rootUrl=$rootUrl auth=$authEnabled insecureTls=${draft.allowInsecureTls} client=sardine"
    }
    return runCatching {
        val sardine = buildJvmSardine(
            rootUrl = rootUrl,
            username = draft.username,
            password = draft.password,
            allowInsecureTls = draft.allowInsecureTls,
        )
        try {
            collectJvmWebDavTracks(
                sardine = sardine,
                rootUrl = rootUrl,
                relativeDirectory = "",
                sourceId = sourceId,
                authEnabled = authEnabled,
                logger = logger,
                sink = tracks,
            )
        } finally {
            sardine.shutdownQuietly()
        }
        ImportScanReport(tracks)
    }.onSuccess { report ->
        logger.info(WEBDAV_LOG_TAG) {
            "scan-complete source=$sourceId rootUrl=$rootUrl trackCount=${report.tracks.size} elapsedMs=${System.currentTimeMillis() - startedAt}"
        }
    }.onFailure { throwable ->
        logger.error(WEBDAV_LOG_TAG, throwable) {
            "scan-failed source=$sourceId rootUrl=$rootUrl elapsedMs=${System.currentTimeMillis() - startedAt}"
        }
    }.getOrThrow()
}

internal suspend fun resolveJvmWebDavPlaybackTarget(
    database: LynMusicDatabase,
    secureCredentialStore: SecureCredentialStore,
    locator: String,
    logger: DiagnosticLogger,
): JvmWebDavPlaybackTarget? {
    val webDav = parseWebDavLocator(locator) ?: return null
    val source = database.importSourceDao().getById(webDav.first) ?: error("Missing WebDAV source")
    val password = source.credentialKey?.let { secureCredentialStore.get(it) }.orEmpty()
    val requestUrl = buildWebDavTrackUrl(source.rootReference, webDav.second)
    val authEnabled = source.username.orEmpty().isNotBlank()
    val mediaSize = resolveJvmWebDavContentLength(
        requestUrl = requestUrl,
        username = source.username.orEmpty(),
        password = password,
        allowInsecureTls = source.allowInsecureTls,
    )
    logger.info(WEBDAV_LOG_TAG) {
        "play-start source=${webDav.first} url=$requestUrl auth=$authEnabled insecureTls=${source.allowInsecureTls} size=$mediaSize client=http-range"
    }
    return JvmWebDavPlaybackTarget(
        media = object : SeekableCallbackMedia() {
            private var currentStream: JvmWebDavOpenedStream? = null

            override fun onGetSize(): Long = mediaSize.coerceAtLeast(0L)

            override fun onOpen(): Boolean {
                currentStream = openJvmWebDavStream(
                    requestUrl = requestUrl,
                    username = source.username.orEmpty(),
                    password = password,
                    allowInsecureTls = source.allowInsecureTls,
                    startByte = 0L,
                )
                return true
            }

            override fun onRead(buffer: ByteArray, bufferSize: Int): Int {
                val stream = currentStream ?: return -1
                return stream.inputStream.read(buffer, 0, bufferSize)
            }

            override fun onSeek(offset: Long): Boolean {
                if (offset < 0L) return false
                return runCatching {
                    closeJvmWebDavStream(currentStream)
                    currentStream = openJvmWebDavStream(
                        requestUrl = requestUrl,
                        username = source.username.orEmpty(),
                        password = password,
                        allowInsecureTls = source.allowInsecureTls,
                        startByte = offset,
                    )
                    true
                }.getOrElse { throwable ->
                    logger.warn(WEBDAV_LOG_TAG) {
                        "play-seek-failed source=${webDav.first} url=$requestUrl offset=$offset reason=${throwable.message.orEmpty()}"
                    }
                    false
                }
            }

            override fun onClose() {
                closeJvmWebDavStream(currentStream)
                currentStream = null
            }
        },
        requestUrl = requestUrl,
    )
}

internal suspend fun requestJvmWebDavMetadata(
    database: LynMusicDatabase,
    secureCredentialStore: SecureCredentialStore,
    locator: String,
    logger: DiagnosticLogger,
): ImportedTrackCandidate? {
    val webDav = parseWebDavLocator(locator) ?: return null
    val source = database.importSourceDao().getById(webDav.first) ?: return null
    val password = source.credentialKey?.let { secureCredentialStore.get(it) }.orEmpty()
    val requestUrl = buildWebDavTrackUrl(source.rootReference, webDav.second)
    val extension = webDav.second.substringAfterLast('.', "").ifBlank { "bin" }
    val tempFile = File.createTempFile("lynmusic-webdav-meta-", ".$extension")
    return runCatching {
        var bytesRead = downloadJvmWebDavHead(
            requestUrl = requestUrl,
            username = source.username.orEmpty(),
            password = password,
            allowInsecureTls = source.allowInsecureTls,
            rangeBytes = WEBDAV_METADATA_RANGE_BYTES,
            target = tempFile,
        )
        val requiredBytes = JvmAudioTagReader.requiredRemoteSnippetBytes(tempFile.toPath(), webDav.second)
        if (requiredBytes != null && requiredBytes > bytesRead && requiredBytes <= WEBDAV_MAX_METADATA_RANGE_BYTES) {
            bytesRead = downloadJvmWebDavHead(
                requestUrl = requestUrl,
                username = source.username.orEmpty(),
                password = password,
                allowInsecureTls = source.allowInsecureTls,
                rangeBytes = requiredBytes,
                target = tempFile,
            )
            logger.debug(WEBDAV_LOG_TAG) {
                "metadata-range-expand source=${webDav.first} url=$requestUrl bytes=$bytesRead"
            }
        }
        logger.debug(WEBDAV_LOG_TAG) {
            "metadata-range-read source=${webDav.first} url=$requestUrl bytes=$bytesRead"
        }
        val candidate = JvmAudioTagReader.readRemoteSnippet(tempFile.toPath(), webDav.second, logger)
        candidate.takeIf { it.hasMeaningfulMetadata(webDav.second) }
    }.onSuccess { candidate ->
        logger.info(WEBDAV_LOG_TAG) {
            if (candidate == null) {
                "metadata-miss source=${webDav.first} url=$requestUrl"
            } else {
                "metadata-hit source=${webDav.first} url=$requestUrl title=${candidate.title} artist=${candidate.artistName.orEmpty()} album=${candidate.albumTitle.orEmpty()}"
            }
        }
    }.onFailure { throwable ->
        logger.warn(WEBDAV_LOG_TAG) {
            "metadata-failed source=${webDav.first} url=$requestUrl reason=${throwable.message.orEmpty()}"
        }
    }.getOrNull().also {
        tempFile.delete()
    }
}

private fun collectJvmWebDavTracks(
    sardine: Sardine,
    rootUrl: String,
    relativeDirectory: String,
    sourceId: String,
    authEnabled: Boolean,
    logger: DiagnosticLogger,
    sink: MutableList<ImportedTrackCandidate>,
) {
    val directoryUrl = if (relativeDirectory.isBlank()) {
        rootUrl
    } else {
        buildWebDavTrackUrl(rootUrl, relativeDirectory).ensureTrailingSlash()
    }
    logger.debug(WEBDAV_LOG_TAG) {
        "propfind source=$sourceId url=$directoryUrl client=sardine"
    }
    val resources = try {
        sardine.list(directoryUrl, 1)
    } catch (throwable: Throwable) {
        throw throwable.asJvmWebDavIOException("扫描", authEnabled)
    }

    val currentDirectoryKey = relativeDirectory.trim('/')
    resources.forEach { resource ->
        val relativePath = resolveWebDavRelativePath(rootUrl, resource.href.toString()) ?: return@forEach
        if (relativePath.isBlank() || relativePath.trim('/') == currentDirectoryKey) return@forEach
        if (resource.isDirectory) {
            collectJvmWebDavTracks(
                sardine = sardine,
                rootUrl = rootUrl,
                relativeDirectory = relativePath,
                sourceId = sourceId,
                authEnabled = authEnabled,
                logger = logger,
                sink = sink,
            )
        } else {
            val fileName = resource.name ?: relativePath.substringAfterLast('/')
            if (isSupportedJvmWebDavAudio(fileName)) {
                sink += ImportedTrackCandidate(
                    title = fileName.substringBeforeLast('.'),
                    mediaLocator = buildWebDavLocator(sourceId, relativePath),
                    relativePath = relativePath,
                    sizeBytes = resource.contentLength ?: 0L,
                    modifiedAt = resource.modified?.time ?: 0L,
                )
            }
        }
    }
}

private fun buildJvmSardine(
    rootUrl: String,
    username: String,
    password: String,
    allowInsecureTls: Boolean,
): Sardine {
    val sardine = if (allowInsecureTls) {
        val sslContext = SSLContexts.custom()
            .loadTrustMaterial(null) { _, _ -> true }
            .build()
        val builder = HttpClientBuilder.create()
            .setSSLSocketFactory(SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
        if (username.isBlank()) {
            SardineImpl(builder)
        } else {
            SardineImpl(builder, username, password)
        }
    } else if (username.isBlank()) {
        SardineFactory.begin()
    } else {
        SardineFactory.begin(username, password)
    }
    if (username.isNotBlank()) {
        sardine.enablePreemptiveAuthentication(URI(rootUrl).toURL())
    }
    return sardine
}

private fun Throwable.asJvmWebDavIOException(operation: String, authEnabled: Boolean): IOException {
    val sardineException = this as? SardineException
    if (sardineException != null) {
        return IOException(
            describeWebDavHttpFailure(
                operation = operation,
                statusCode = sardineException.statusCode,
                authSent = authEnabled,
                serverDetail = sardineException.responsePhrase,
            ),
            this,
        )
    }
    return if (this is IOException) this else IOException(
        "WebDAV $operation 失败: ${message ?: this::class.simpleName.orEmpty()}",
        this,
    )
}

private fun Sardine?.shutdownQuietly() {
    val instance = this ?: return
    runCatching { instance.shutdown() }
}

private data class JvmWebDavOpenedStream(
    val connection: HttpURLConnection,
    val inputStream: InputStream,
)

private fun closeJvmWebDavStream(stream: JvmWebDavOpenedStream?) {
    val activeStream = stream ?: return
    runCatching { activeStream.inputStream.close() }
    activeStream.connection.disconnect()
}

private fun resolveJvmWebDavContentLength(
    requestUrl: String,
    username: String,
    password: String,
    allowInsecureTls: Boolean,
): Long {
    val connection = openJvmWebDavConnection(
        requestUrl = requestUrl,
        username = username,
        password = password,
        allowInsecureTls = allowInsecureTls,
    )
    return try {
        connection.requestMethod = "GET"
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Range", "bytes=0-0")
        connection.connect()
        when (val statusCode = connection.responseCode) {
            HttpURLConnection.HTTP_PARTIAL -> {
                parseContentRangeTotalLength(connection.getHeaderField("Content-Range"))
                    ?: connection.getHeaderFieldLong("Content-Length", 0L)
            }

            in 200..299 -> connection.getHeaderFieldLong("Content-Length", 0L)
            else -> throw IOException("WebDAV content length probe failed with HTTP $statusCode")
        }
    } finally {
        connection.inputStreamOrNull()?.close()
        connection.disconnect()
    }
}

private fun openJvmWebDavStream(
    requestUrl: String,
    username: String,
    password: String,
    allowInsecureTls: Boolean,
    startByte: Long,
): JvmWebDavOpenedStream {
    val connection = openJvmWebDavConnection(
        requestUrl = requestUrl,
        username = username,
        password = password,
        allowInsecureTls = allowInsecureTls,
    )
    return try {
        connection.requestMethod = "GET"
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        if (startByte > 0L) {
            connection.setRequestProperty("Range", "bytes=$startByte-")
        }
        connection.connect()
        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            throw IOException("WebDAV playback stream failed with HTTP $statusCode")
        }
        val inputStream = connection.inputStream
        if (startByte > 0L && statusCode == HttpURLConnection.HTTP_OK) {
            skipJvmWebDavBytes(inputStream, startByte)
        }
        JvmWebDavOpenedStream(connection, inputStream)
    } catch (throwable: Throwable) {
        connection.disconnect()
        throw throwable
    }
}

fun downloadJvmWebDavHead(
    requestUrl: String,
    username: String,
    password: String,
    allowInsecureTls: Boolean,
    rangeBytes: Long,
    target: File,
): Long {
    val connection = openJvmWebDavConnection(
        requestUrl = requestUrl,
        username = username,
        password = password,
        allowInsecureTls = allowInsecureTls,
    )
    return try {
        connection.requestMethod = "GET"
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Range", "bytes=0-${rangeBytes - 1}")
        connection.connect()
        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            throw IOException("WebDAV metadata range request failed with HTTP $statusCode")
        }
        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
            target.length()
        }
    } finally {
        connection.disconnect()
    }
}

private fun parseContentRangeTotalLength(headerValue: String?): Long? {
    val value = headerValue?.trim().orEmpty()
    if (value.isEmpty()) return null
    return CONTENT_RANGE_TOTAL_PATTERN.matchEntire(value)
        ?.groupValues
        ?.getOrNull(1)
        ?.toLongOrNull()
}

private fun skipJvmWebDavBytes(inputStream: InputStream, bytesToSkip: Long) {
    var remaining = bytesToSkip
    while (remaining > 0L) {
        val skipped = inputStream.skip(remaining)
        if (skipped > 0L) {
            remaining -= skipped
            continue
        }
        val nextByte = inputStream.read()
        if (nextByte == -1) {
            throw IOException("WebDAV stream ended before reaching requested offset $bytesToSkip")
        }
        remaining -= 1L
    }
}

private fun openJvmWebDavConnection(
    requestUrl: String,
    username: String,
    password: String,
    allowInsecureTls: Boolean,
): HttpURLConnection {
    val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
        buildBasicAuthorizationHeader(username, password)?.let { header ->
            setRequestProperty("Authorization", header)
        }
    }
    if (allowInsecureTls && connection is HttpsURLConnection) {
        connection.sslSocketFactory = insecureSslContext.socketFactory
        connection.hostnameVerifier = insecureHostnameVerifier
    }
    return connection
}

private fun HttpURLConnection.inputStreamOrNull(): InputStream? {
    return runCatching { inputStream }.getOrNull() ?: runCatching { errorStream }.getOrNull()
}

private fun ImportedTrackCandidate.hasMeaningfulMetadata(relativePath: String): Boolean {
    val fallbackTitle = relativePath.substringAfterLast('/').substringBeforeLast('.')
    return title != fallbackTitle ||
        !artistName.isNullOrBlank() ||
        !albumTitle.isNullOrBlank() ||
        durationMs > 0L ||
        trackNumber != null ||
        discNumber != null ||
        artworkLocator != null ||
        !embeddedLyrics.isNullOrBlank()
}

private val insecureSslContext: SSLContext by lazy {
    SSLContext.getInstance("TLS").apply {
        init(
            null,
            arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                },
            ),
            SecureRandom(),
        )
    }
}

private val insecureHostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }

private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"

private fun isSupportedJvmWebDavAudio(fileName: String): Boolean {
    return fileName.substringAfterLast('.', "").lowercase() in setOf("mp3", "m4a", "aac", "wav")
}

private const val WEBDAV_LOG_TAG = "WebDav"
const val WEBDAV_METADATA_RANGE_BYTES = 262_144L
private const val WEBDAV_MAX_METADATA_RANGE_BYTES = 2_097_152L
private val CONTENT_RANGE_TOTAL_PATTERN = Regex("""bytes\s+\d+-\d+/(\d+)""")
