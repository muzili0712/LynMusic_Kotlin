package top.iwesley.lyn.music.scripting

import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext

/**
 * Apple (iOS/macOS) actual for [JsRuntime].
 *
 * **M0 status: stub-only.** The M0 task's contractual requirement is that each platform
 * has *some* actual so the shared module compiles across all five targets (Android /
 * JVM / iosArm64 / iosSimulatorArm64 / macosArm64). Real JavaScriptCore wiring is
 * gated on the T5 `JsBridgeImpl.apple` work because:
 *
 *  1. `platform.JavaScriptCore.JSValue` category methods (`isNull`, `isNumber`,
 *     `objectForKeyedSubscript:`, `objectAtIndexedSubscript:`, ...) are **not**
 *     visible from the commonized `appleMain` intermediate source set — only the
 *     core interface class is. Accessing them requires writing target-specific
 *     actual files under each of `iosArm64Main` / `iosSimulatorArm64Main` /
 *     `macosArm64Main`.
 *  2. `register` on JSC needs an ObjC block whose lifetime is tied to the bridge
 *     implementation (retention + reentrancy), which is T5 territory anyway.
 *
 * The JVM path (GraalVM) is M0's dev-driven path: contract tests, smoke tests and
 * the six-source Repository layer all run on JVM in M0. Android (QuickJS) is the
 * Android release path. Apple runs in M1 once T5 lands the proper bridge.
 *
 * TODO(T5): replace this stub with per-target actuals under iosArm64Main / iosSimulatorArm64Main
 * / macosArm64Main using `platform.JavaScriptCore` directly. Delete this file.
 */
actual object JsRuntimeFactory {
    actual fun create(sourceId: String, bridge: JsBridge): JsRuntime =
        AppleStubRuntime(sourceId)
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
private class AppleStubRuntime(override val sourceId: String) : JsRuntime {
    override val dispatcher: CoroutineDispatcher = newSingleThreadContext("jsc-$sourceId")

    override suspend fun evaluate(script: String, name: String): JsValue =
        throw NotImplementedError("Apple JsRuntime.evaluate is implemented in T5")

    override suspend fun invoke(path: String, vararg args: JsValue): JsValue =
        throw NotImplementedError("Apple JsRuntime.invoke is implemented in T5")

    override fun register(name: String, host: HostFunction) {
        throw NotImplementedError("Apple JsRuntime.register is implemented in T5")
    }

    override fun close() {
        (dispatcher as? CloseableCoroutineDispatcher)?.close()
    }
}
