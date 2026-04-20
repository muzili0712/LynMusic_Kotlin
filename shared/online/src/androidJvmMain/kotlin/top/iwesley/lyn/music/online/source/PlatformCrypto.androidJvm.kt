package top.iwesley.lyn.music.online.source

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * JVM + Android 共享的 [PlatformCrypto] 实现（Android 的 ART 同样实现了 `javax.crypto`
 * 和 `java.util.zip`，两边语义一致）。
 *
 * 原先 jvmMain / androidMain 各写一份副本；本次通过 `androidJvmMain` 中间源集合并。
 *
 * mode 参数示例：`"CBC/PKCS5Padding"` / `"ECB/PKCS7Padding"` / `"CBC"`。
 * - 仅给算法 mode（"CBC"/"ECB"）时默认按 `PKCS5Padding` 组装 transformation。
 * - 完整给 `mode/padding` 时按原样拼入 `AES/<mode>/<padding>`（空格容忍，大小写不敏感）。
 *
 * RSA 按 padding 参数分派 transformation：
 *  - `"PKCS1"` (默认) → `RSA/ECB/PKCS1Padding`
 *  - `"NoPadding"` / `"None"` / `"Raw"` → `RSA/ECB/NoPadding`（wy 源 aesRsaEncrypt 专用，要求 data 长度 == key 长度）
 *  - 其它 → `RSA/ECB/${padding}Padding` 原样下发
 * 公钥格式容忍 PEM 头尾 + 多行；lx 源常以 PEM 传入。
 *
 * zlib inflate 按 format 参数分派：
 *  - `"auto"` (默认) → 按 magic 推断（gzip 1F8B / zlib 78xx / 其它 raw）
 *  - `"zlib"` → `Inflater()` 默认 wrap，识别 zlib header
 *  - `"raw"` → `Inflater(true)` nowrap，pako.inflateRaw 对应
 *  - `"gzip"` → `GZIPInputStream`，pako.ungzip 对应
 */
class JvmPlatformCrypto : PlatformCrypto {

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

    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String, padding: String): ByteArray {
        val clean = publicKeyPem.lines()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
            .replace("\\s".toRegex(), "")
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(clean))
        val key = KeyFactory.getInstance("RSA").generatePublic(keySpec)
        val transform = when (padding.uppercase()) {
            "NOPADDING", "NONE", "RAW" -> "RSA/ECB/NoPadding"
            "PKCS1" -> "RSA/ECB/PKCS1Padding"
            else -> "RSA/ECB/${padding}Padding"
        }
        val cipher = Cipher.getInstance(transform)
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

    override fun zlibInflate(input: ByteArray, format: String): ByteArray {
        val detected = when (format.lowercase()) {
            "auto" -> detectZlibFormat(input)
            else -> format.lowercase()
        }
        return when (detected) {
            "gzip" -> GZIPInputStream(input.inputStream()).use { it.readBytes() }
            "raw" -> inflateWith(input, nowrap = true)
            else -> inflateWith(input, nowrap = false) // "zlib" 或 fallback
        }
    }

    private fun inflateWith(input: ByteArray, nowrap: Boolean): ByteArray {
        val inflater = Inflater(nowrap)
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

    private fun detectZlibFormat(bytes: ByteArray): String {
        if (bytes.size < 2) return "raw"
        val b0 = bytes[0].toInt() and 0xFF
        val b1 = bytes[1].toInt() and 0xFF
        return when {
            b0 == 0x1F && b1 == 0x8B -> "gzip"
            b0 == 0x78 -> "zlib"  // 0x78 0x01/0x9C/0xDA 是常见 zlib header
            else -> "raw"
        }
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
            // 已经是 "MODE/PADDING" 或 "ALGO/MODE/PADDING" 形式
            val segs = normalized.split("/")
            val raw = when (segs.size) {
                2 -> "$algo/${segs[0].uppercase()}/${segs[1]}"
                3 -> "${segs[0].uppercase()}/${segs[1].uppercase()}/${segs[2]}"
                else -> "$algo/CBC/PKCS5Padding"
            }
            return normalizePadding(raw)
        }
        // 仅 "CBC" / "ECB"
        return "$algo/${normalized.uppercase()}/PKCS5Padding"
    }

    /**
     * JVM 默认 SunJCE provider 不注册 "PKCS7Padding" 字面量（会抛
     * `NoSuchAlgorithmException: Cannot find any provider supporting
     * AES/ECB/PKCS7Padding`），但注册了 "PKCS5Padding"。对于 16 字节块的 AES，
     * PKCS#5 和 PKCS#7 产生完全相同的填充字节 —— PKCS#5 只是 PKCS#7 在 8 字节块
     * 上的特化。Android (BC provider) 和 iOS (CommonCrypto) 都支持 "PKCS7Padding"。
     * 这里把传入的 "PKCS7Padding" 在 JVM 上改写成 "PKCS5Padding"，密文不变，
     * 避免 caller 用 Kotlin-common-style 名字（wy eapi / kg 某些端点）就炸。
     */
    private fun normalizePadding(transformation: String): String =
        transformation.replace(Regex("/PKCS7Padding$", RegexOption.IGNORE_CASE), "/PKCS5Padding")

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

actual fun createPlatformCrypto(): PlatformCrypto = JvmPlatformCrypto()
