package top.iwesley.lyn.music.online

import top.iwesley.lyn.music.online.source.PlatformCrypto
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * M1.0 Task 1 契约测试：PlatformCrypto 的 rsaEncrypt(padding) + zlibInflate(format)。
 *
 * - rsa padding: wy 源用 NoPadding，默认保持 PKCS1（其它源沿用）。
 * - zlib format: kg 源同时用 inflate/inflateRaw/ungzip，需要 auto 识别 + 显式 raw/gzip。
 *
 * Apple stub 抛 NotImplementedError，此测试只在有真实现的 target（JVM/Android）有意义；
 * Apple target 跑到这里会抛，留给 M1.0-6 做真实现后解锁。
 */
class PlatformCryptoContractTest {

    private val crypto: PlatformCrypto = createPlatformCrypto()

    @Test
    fun rsa_encrypt_defaults_to_pkcs1_padding() {
        val pem = TEST_RSA_PUBLIC_PEM
        val data = "hello".encodeToByteArray()
        val result = crypto.rsaEncrypt(data, pem)  // 默认 PKCS1
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun rsa_encrypt_accepts_nopadding_for_wy_source() {
        val pem = TEST_RSA_PUBLIC_PEM
        // NoPadding 要求 data 长度等于 key 长度 (128 bytes for 1024-bit RSA)
        val data = ByteArray(128) { 0x00 }
        val result = crypto.rsaEncrypt(data, pem, padding = "NoPadding")
        assertEquals(128, result.size)
    }

    @Test
    fun zlib_inflate_defaults_to_auto_format() {
        // auto 识别 zlib magic (0x78 前缀)：encode("a") in zlib = 78 9C 4B 04 00 00 62 00 62
        val zlibBytes = byteArrayOf(0x78.toByte(), 0x9C.toByte(), 0x4B, 0x04, 0x00, 0x00, 0x62, 0x00, 0x62)
        val result = crypto.zlibInflate(zlibBytes)
        assertEquals("a", result.decodeToString())
    }

    @Test
    fun zlib_inflate_raw_format_accepts_raw_deflate() {
        // raw deflate of "a"
        val rawBytes = byteArrayOf(0x4B, 0x04, 0x00)
        val result = crypto.zlibInflate(rawBytes, format = "raw")
        assertEquals("a", result.decodeToString())
    }

    @Test
    fun zlib_inflate_gzip_format_accepts_gzip_stream() {
        // gzip magic 0x1F8B + deflate of "a"
        val gzipBytes = byteArrayOf(
            0x1F, 0x8B.toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x4B, 0x04, 0x00, 0x43, 0xBE.toByte(), 0xB7.toByte(), 0xE8.toByte(), 0x01, 0x00, 0x00, 0x00
        )
        val result = crypto.zlibInflate(gzipBytes, format = "gzip")
        assertEquals("a", result.decodeToString())
    }
}

// 1024-bit RSA 公钥，仅用于测试；通过 `openssl genrsa 1024 | openssl rsa -pubout` 生成。
// 对应的私钥已丢弃；测试只断言加密输出非空 / 长度正确，不验证解密。
private const val TEST_RSA_PUBLIC_PEM = """-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4QQIruZX5TeAC0alVGK/PlMHd
OVj2GHTlRqQih05Bu9GdDnCdZG2UFyg1rlW8MuyQnC05JxOlWWLvX3LEWC+3THFB
SB2awIso28NzahAhW2Z5an1N2z1pNi2xlI7gaahqBfxNBwIIUnLOkyUjsrKlyH91
BgOxcTp4d6O9r2WeUQIDAQAB
-----END PUBLIC KEY-----"""
