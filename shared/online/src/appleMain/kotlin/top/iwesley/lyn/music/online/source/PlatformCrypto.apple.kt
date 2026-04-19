package top.iwesley.lyn.music.online.source

/**
 * Apple (iOS/macOS) actual for [PlatformCrypto].
 *
 * **M0 status: stub-only.** 按 M0 计划（Task 5 Step 4 line 1770）的立场：
 * - Apple 的 JsRuntime 本身在 T2 只有 stub（`AppleStubRuntime`），iOS/macOS 主链不是 M0 冒烟目标；
 * - 不引入 `com.soywiz.korlibs.krypto` 是刻意选择：该坐标在 korlibs 5.x 重组后处于不稳定状态，
 *   接入风险高于收益；其它 KMP crypto 库（e.g. cryptography-provider-apple）也未进 `libs.versions.toml`。
 *
 * 因此本文件所有方法均抛 [NotImplementedError]，调用方应捕获并回退（Repository 层的 Result/
 * Promise.catch 语义足以让 lx 源走备用路径 / 记录"某源该能力不可用"）。
 *
 * TODO(M1)：当 Apple JsRuntime 走真实 JSC 接入 + iOS 冒烟覆盖时，把这些方法换成：
 *  - md5/sha1/sha256: CommonCrypto
 *  - aes/des: CommonCrypto + CCCrypt
 *  - rsa: Security.framework SecKeyCreateWithData + SecKeyCreateEncryptedData
 *  - base64: NSData.base64EncodedString / NSData(base64Encoded:)
 *  - zlib: NSData.decompressedData(using:) (iOS 13+, macOS 10.15+)
 *  - iconv: NSString.data(using:) + NSString(data:encoding:)
 */
class ApplePlatformCryptoStub : PlatformCrypto {
    override fun md5Hex(input: ByteArray): String =
        throw NotImplementedError(MSG)

    override fun sha1(input: ByteArray): ByteArray =
        throw NotImplementedError(MSG)

    override fun sha256(input: ByteArray): ByteArray =
        throw NotImplementedError(MSG)

    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray =
        throw NotImplementedError(MSG)

    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray =
        throw NotImplementedError(MSG)

    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String): ByteArray =
        throw NotImplementedError(MSG)

    override fun base64Encode(input: ByteArray): String =
        throw NotImplementedError(MSG)

    override fun base64Decode(input: String): ByteArray =
        throw NotImplementedError(MSG)

    override fun hexEncode(input: ByteArray): String {
        // Kotlin/Native 没有 `"%02x".format(...)`；手写 nibble → hex。
        val sb = StringBuilder(input.size * 2)
        val hex = "0123456789abcdef"
        for (b in input) {
            val v = b.toInt() and 0xFF
            sb.append(hex[v ushr 4])
            sb.append(hex[v and 0x0F])
        }
        return sb.toString()
    }

    override fun hexDecode(input: String): ByteArray {
        val trimmed = input.trim()
        require(trimmed.length % 2 == 0) { "hex string must have even length: len=${trimmed.length}" }
        return ByteArray(trimmed.length / 2) { i ->
            trimmed.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    override fun zlibInflate(input: ByteArray): ByteArray =
        throw NotImplementedError(MSG)

    override fun iconvDecode(input: ByteArray, encoding: String): String =
        throw NotImplementedError(MSG)

    override fun iconvEncode(input: String, encoding: String): ByteArray =
        throw NotImplementedError(MSG)

    private companion object {
        const val MSG = "Apple PlatformCrypto is stub-only in M0; implement with CommonCrypto / Security in M1."
    }
}

actual fun createPlatformCrypto(): PlatformCrypto = ApplePlatformCryptoStub()
