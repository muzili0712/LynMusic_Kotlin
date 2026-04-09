package top.iwesley.lyn.music.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import java.io.File
import java.net.URI

internal fun hasManageAllFilesAccess(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
}

internal fun buildManageAllFilesAccessIntent(context: Context): Intent {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return Intent(Settings.ACTION_SETTINGS)
    }
    val appSpecificIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    return if (appSpecificIntent.resolveActivity(context.packageManager) != null) {
        appSpecificIntent
    } else {
        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    }
}

internal fun resolveAndroidLocalTrackFile(locator: String): File? {
    val value = locator.trim()
    if (value.isBlank()) return null
    return runCatching {
        when {
            value.startsWith("file://", ignoreCase = true) -> File(URI(value))
            value.startsWith("/") -> File(value)
            Regex("^[A-Za-z]:[/\\\\].*").matches(value) -> File(value)
            else -> null
        }
    }.getOrNull()?.takeIf { it.isAbsolute }
}

internal fun resolveTreeUriToDirectory(context: Context, treeUri: Uri): File? {
    if (!hasManageAllFilesAccess(context)) return null
    if (treeUri.authority != EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY) return null
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
    val volumeId = documentId.substringBefore(':').trim()
    if (volumeId.isBlank()) return null
    val relativePath = documentId.substringAfter(':', "").trim('/')
    val volumeRoot = resolveStorageVolumeRoot(context, volumeId) ?: return null
    return if (relativePath.isBlank()) volumeRoot else File(volumeRoot, relativePath)
}

@Suppress("DEPRECATION")
private fun resolveStorageVolumeRoot(context: Context, volumeId: String): File? {
    if (volumeId.equals(PRIMARY_VOLUME_ID, ignoreCase = true)) {
        return Environment.getExternalStorageDirectory()
    }
    return context.getExternalFilesDirs(null)
        .asSequence()
        .filterNotNull()
        .mapNotNull(File::findStorageVolumeRoot)
        .firstOrNull { root -> root.name.equals(volumeId, ignoreCase = true) }
}

private fun File.findStorageVolumeRoot(): File? {
    var current: File? = this
    while (current != null) {
        if (current.parentFile?.absolutePath == STORAGE_ROOT_PATH) {
            return current
        }
        current = current.parentFile
    }
    return null
}

private const val EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents"
private const val PRIMARY_VOLUME_ID = "primary"
private const val STORAGE_ROOT_PATH = "/storage"
