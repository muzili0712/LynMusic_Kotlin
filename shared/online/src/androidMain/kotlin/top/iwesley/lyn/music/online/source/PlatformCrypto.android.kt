package top.iwesley.lyn.music.online.source

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Android actual。Android 也跑 JVM（Dalvik/ART），`javax.crypto` / `java.util.zip` 全部可用。
 *
 * 本文件是 [JvmPlatformCrypto] 的复刻——因为 androidMain / jvmMain 是各自独立的 KMP source set，
 * 跨 set 继承需要额外的 source set dependsOn 结构；为避免动 build.gradle，选择最直接的：
 * 同 package 同名 class 在两个 source set 各写一份。两份逻辑必须保持严格一致。
 */
class AndroidPlatformCrypto : PlatformCrypto {

    override fun md5Hex(input: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(input)
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    override fun sha1(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-1").digest(input)

    override fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)

    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray =
        symmetricEncrypt("AES", data, key, iv, mode)

    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray =
        symmetricEncrypt("DES", data, key, iv, mode)

    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String): ByteArray {
        val clean = publicKeyPem.lines()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
            .replace("\\s".toRegex(), "")
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(clean))
        val key = KeyFactory.getInstance("RSA").generatePublic(keySpec)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    override fun base64Encode(input: ByteArray): String = Base64.getEncoder().encodeToString(input)
    override fun base64Decode(input: String): ByteArray = Base64.getDecoder().decode(input)

    override fun hexEncode(input: ByteArray): String =
        input.joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    override fun hexDecode(input: String): ByteArray {
        val trimmed = input.trim()
        require(trimmed.length % 2 == 0) { "hex string must have even length: len=${trimmed.length}" }
        return ByteArray(trimmed.length / 2) { i ->
            trimmed.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    override fun zlibInflate(input: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(input)
        val buffer = ByteArray(4096)
        val out = java.io.ByteArrayOutputStream(input.size * 4)
        try {
            while (!inflater.finished()) {
                val n = inflater.inflate(buffer)
                if (n == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) break
                }
                out.write(buffer, 0, n)
            }
        } finally {
            inflater.end()
        }
        return out.toByteArray()
    }

    override fun iconvDecode(input: ByteArray, encoding: String): String =
        String(input, charsetFromLxName(encoding))

    override fun iconvEncode(input: String, encoding: String): ByteArray =
        input.toByteArray(charsetFromLxName(encoding))

    private fun symmetricEncrypt(
        algo: String,
        data: ByteArray,
        key: ByteArray,
        iv: ByteArray?,
        mode: String,
    ): ByteArray {
        val transformation = buildTransformation(algo, mode)
        val cipher = Cipher.getInstance(transformation)
        val secret = SecretKeySpec(key, algo)
        if (iv != null && !transformation.contains("ECB", ignoreCase = true)) {
            cipher.init(Cipher.ENCRYPT_MODE, secret, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secret)
        }
        return cipher.doFinal(data)
    }

    private fun buildTransformation(algo: String, modeRaw: String): String {
        val normalized = modeRaw.trim().replace("\\s+".toRegex(), "")
        if (normalized.contains("/")) {
            val segs = normalized.split("/")
            return when (segs.size) {
                2 -> "$algo/${segs[0].uppercase()}/${segs[1]}"
                3 -> "${segs[0].uppercase()}/${segs[1].uppercase()}/${segs[2]}"
                else -> "$algo/CBC/PKCS5Padding"
            }
        }
        return "$algo/${normalized.uppercase()}/PKCS5Padding"
    }

    private fun charsetFromLxName(name: String): java.nio.charset.Charset {
        val normalized = name.trim().uppercase().replace("_", "-")
        return when (normalized) {
            "UTF8", "UTF-8" -> Charsets.UTF_8
            "GBK", "GB2312", "GB18030" -> java.nio.charset.Charset.forName(normalized)
            "ASCII", "US-ASCII" -> Charsets.US_ASCII
            else -> runCatching { java.nio.charset.Charset.forName(normalized) }
                .getOrElse { Charsets.UTF_8 }
        }
    }
}

actual fun createPlatformCrypto(): PlatformCrypto = AndroidPlatformCrypto()
