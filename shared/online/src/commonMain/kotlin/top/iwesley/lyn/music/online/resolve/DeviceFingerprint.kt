package top.iwesley.lyn.music.online.resolve

/**
 * 设备指纹。部分源（tx 需要 guid/wid，wy 需要 deviceId）签名算法必需。
 * 首启生成随机值，持久化到 Settings；不可跨会话变化，否则源侧可能拒绝。
 */
data class DeviceFingerprint(
    val guid: String,   // tx 用；长度 32 hex
    val wid: String,    // tx 用；长度 32 hex
    val deviceId: String, // wy 等用；32 位随机
) {
    companion object {
        /** 简单伪随机生成；首启调用一次。 */
        fun generate(): DeviceFingerprint {
            val random = kotlin.random.Random
            fun hex32() = (0 until 16).map {
                random.nextInt(256).toString(16).padStart(2, '0')
            }.joinToString("")
            return DeviceFingerprint(guid = hex32(), wid = hex32(), deviceId = hex32())
        }
    }
}
