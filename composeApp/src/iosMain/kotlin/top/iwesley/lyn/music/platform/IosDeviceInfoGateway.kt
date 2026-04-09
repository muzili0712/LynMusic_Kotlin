package top.iwesley.lyn.music.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.posix.uname
import platform.posix.utsname
import top.iwesley.lyn.music.core.model.DeviceInfoGateway
import top.iwesley.lyn.music.core.model.DeviceInfoSnapshot

fun createIosDeviceInfoGateway(): DeviceInfoGateway = IosDeviceInfoGateway()

private class IosDeviceInfoGateway : DeviceInfoGateway {
    override suspend fun loadDeviceInfoSnapshot(): Result<DeviceInfoSnapshot> = withContext(Dispatchers.Default) {
        runCatching {
            val device = UIDevice.currentDevice
            val processInfo = NSProcessInfo.processInfo
            DeviceInfoSnapshot(
                systemName = device.systemName(),
                systemVersion = device.systemVersion,
                resolution = iosResolution(),
                cpuDescription = iosCpuDescription(processInfo),
                totalMemoryBytes = processInfo.physicalMemory.toLong().takeIf { it > 0L },
                deviceModel = iosDeviceModel(device, processInfo),
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun iosResolution(): String? {
    val bounds = UIScreen.mainScreen.nativeBounds
    return bounds.useContents {
        val width = size.width.toInt().takeIf { it > 0 }
        val height = size.height.toInt().takeIf { it > 0 }
        if (width != null && height != null) "$width × $height px" else null
    }
}

private fun iosCpuDescription(processInfo: NSProcessInfo): String? {
    val architecture = iosSimulatorArchitectures(processInfo)
        ?.substringBefore(' ')
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "arm64"
    val cores = processInfo.activeProcessorCount.toInt().takeIf { it > 0 }?.let { "$it 核" }
    return listOfNotNull(architecture, cores).joinToString(" · ").takeIf { it.isNotBlank() }
}

private fun iosDeviceModel(
    device: UIDevice,
    processInfo: NSProcessInfo,
): String? {
    val identifier = iosSimulatorModelIdentifier(processInfo)
        ?: iosMachineIdentifier()?.takeIf(::isAppleDeviceIdentifier)
    val fallback = device.model.takeIf { it.isNotBlank() }
    return identifier ?: fallback
}

@OptIn(ExperimentalForeignApi::class)
private fun iosMachineIdentifier(): String? = memScoped {
    val systemInfo = alloc<utsname>()
    if (uname(systemInfo.ptr) != 0) return null
    systemInfo.machine.toKString().trim().takeIf { it.isNotBlank() }
}

private fun iosSimulatorModelIdentifier(processInfo: NSProcessInfo): String? {
    return (processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] as? String)
        ?.trim()
        ?.takeIf(::isAppleDeviceIdentifier)
}

private fun iosSimulatorArchitectures(processInfo: NSProcessInfo): String? {
    return (processInfo.environment["SIMULATOR_ARCHS"] as? String)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

private fun isAppleDeviceIdentifier(value: String): Boolean {
    return value.contains(",") ||
        value.startsWith("iPhone") ||
        value.startsWith("iPad") ||
        value.startsWith("iPod")
}
