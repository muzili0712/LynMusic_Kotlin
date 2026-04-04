package top.iwesley.lyn.music.core.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ArtworkTintTheme(
    val rimColorArgb: Int,
    val glowColorArgb: Int,
    val innerGlowColorArgb: Int,
)

fun deriveArtworkTintTheme(sampledPixels: Iterable<Int>): ArtworkTintTheme? {
    val binCount = 18
    val bins = Array(binCount) { ArgbColorAccumulator() }
    sampledPixels.forEach { argb ->
        val alpha = argbAlpha(argb)
        if (alpha < 0.35f) return@forEach
        val hsl = argbToHsl(argb)
        val saturation = hsl[1]
        val lightness = hsl[2]
        if (saturation < 0.16f) return@forEach
        if (lightness < 0.12f || lightness > 0.84f) return@forEach
        val weight = saturation * (1f - abs(lightness - 0.52f)).coerceAtLeast(0.18f)
        if (weight <= 0f) return@forEach
        val binIndex = (((hsl[0] / 360f) * binCount).toInt()).coerceIn(0, binCount - 1)
        bins[binIndex].add(argb, weight)
    }
    val dominant = bins.maxByOrNull { it.weight }?.takeIf { it.weight > 0f }?.averageColorArgb() ?: return null
    val hsl = argbToHsl(dominant)
    val safeHue = hsl[0]
    val safeSaturation = min(max(hsl[1], 0.24f), 0.58f)
    return ArtworkTintTheme(
        rimColorArgb = hslToArgb(safeHue, safeSaturation, 0.64f),
        glowColorArgb = hslToArgb(safeHue, safeSaturation, 0.42f),
        innerGlowColorArgb = hslToArgb(safeHue, safeSaturation * 0.82f, 0.56f),
    )
}

fun argbWithAlpha(argb: Int, alpha: Float): Int {
    val clampedAlpha = alpha.coerceIn(0f, 1f)
    return ((clampedAlpha * 255f).roundToInt().coerceIn(0, 255) shl 24) or (argb and 0x00FFFFFF)
}

private fun argbAlpha(argb: Int): Float = ((argb ushr 24) and 0xFF) / 255f

private fun argbToHsl(argb: Int): FloatArray {
    val red = ((argb ushr 16) and 0xFF) / 255f
    val green = ((argb ushr 8) and 0xFF) / 255f
    val blue = (argb and 0xFF) / 255f
    val max = max(red, max(green, blue))
    val min = min(red, min(green, blue))
    val delta = max - min
    val lightness = (max + min) / 2f
    if (delta == 0f) return floatArrayOf(0f, 0f, lightness)
    val saturation = delta / (1f - abs(2f * lightness - 1f)).coerceAtLeast(1e-6f)
    val hue = when (max) {
        red -> 60f * (((green - blue) / delta).modPositive(6f))
        green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }
    return floatArrayOf(hue, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
}

private fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Int {
    val clampedHue = ((hue % 360f) + 360f) % 360f
    val clampedSaturation = saturation.coerceIn(0f, 1f)
    val clampedLightness = lightness.coerceIn(0f, 1f)
    if (clampedSaturation <= 0f) {
        val channel = (clampedLightness * 255f).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (channel shl 16) or (channel shl 8) or channel
    }
    val chroma = (1f - abs(2f * clampedLightness - 1f)) * clampedSaturation
    val huePrime = clampedHue / 60f
    val second = chroma * (1f - abs((huePrime % 2f) - 1f))
    val (redPrime, greenPrime, bluePrime) = when {
        huePrime < 1f -> Triple(chroma, second, 0f)
        huePrime < 2f -> Triple(second, chroma, 0f)
        huePrime < 3f -> Triple(0f, chroma, second)
        huePrime < 4f -> Triple(0f, second, chroma)
        huePrime < 5f -> Triple(second, 0f, chroma)
        else -> Triple(chroma, 0f, second)
    }
    val match = clampedLightness - chroma / 2f
    val red = ((redPrime + match) * 255f).roundToInt().coerceIn(0, 255)
    val green = ((greenPrime + match) * 255f).roundToInt().coerceIn(0, 255)
    val blue = ((bluePrime + match) * 255f).roundToInt().coerceIn(0, 255)
    return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
}

private fun Float.modPositive(divisor: Float): Float {
    val remainder = this % divisor
    return if (remainder < 0f) remainder + divisor else remainder
}

private class ArgbColorAccumulator {
    var red = 0f
    var green = 0f
    var blue = 0f
    var weight = 0f

    fun add(argb: Int, itemWeight: Float) {
        red += ((argb ushr 16) and 0xFF) * itemWeight
        green += ((argb ushr 8) and 0xFF) * itemWeight
        blue += (argb and 0xFF) * itemWeight
        weight += itemWeight
    }

    fun averageColorArgb(): Int {
        if (weight <= 0f) return 0
        val avgRed = (red / weight).roundToInt().coerceIn(0, 255)
        val avgGreen = (green / weight).roundToInt().coerceIn(0, 255)
        val avgBlue = (blue / weight).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (avgRed shl 16) or (avgGreen shl 8) or avgBlue
    }
}
