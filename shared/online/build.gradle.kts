import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        // T1 deleted applyDefaultHierarchyTemplate(); declare apple* source sets manually
        // (gradle.properties also disables it globally).
        val appleMain by creating { dependsOn(commonMain.get()) }
        val appleTest by creating { dependsOn(commonTest.get()) }
        val iosArm64Main by getting { dependsOn(appleMain) }
        val iosSimulatorArm64Main by getting { dependsOn(appleMain) }
        val macosArm64Main by getting { dependsOn(appleMain) }
        val iosArm64Test by getting { dependsOn(appleTest) }
        val iosSimulatorArm64Test by getting { dependsOn(appleTest) }
        val macosArm64Test by getting { dependsOn(appleTest) }

        // androidJvmMain 中间源集：Android (ART) 和 JVM 都能用 javax.crypto/java.util.zip，
        // 共享一份 JvmPlatformCrypto 实现（原先 androidMain 手抄了一份 JVM 副本）。
        val androidJvmMain by creating { dependsOn(commonMain.get()) }
        val androidJvmTest by creating { dependsOn(commonTest.get()) }
        val jvmMain by getting { dependsOn(androidJvmMain) }
        val androidMain by getting { dependsOn(androidJvmMain) }
        val jvmTest by getting { dependsOn(androidJvmTest) }

        commonMain.dependencies {
            implementation(project(":shared:core"))
            implementation(project(":shared:data"))
            implementation(project(":shared:scripting"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

android {
    namespace = "top.iwesley.lyn.music.shared.online"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// T3 资产管线 task；当前仅注册声明，scripts/bundle-sdk.mjs 与 vendor/lx-sdk 将在 T3 补齐。
// 不要在 T4 阶段实际运行本 task，配置阶段的 inputs 缺失会在 Gradle 日志里出现 warning，属于预期行为。
tasks.register<Exec>("bundleMusicSdk") {
    group = "build"
    description = "Re-bundle lx-music-mobile SDK via esbuild"
    workingDir = rootProject.projectDir
    commandLine("node", "scripts/bundle-sdk.mjs")
    inputs.dir(rootProject.file("vendor/lx-sdk"))
    outputs.dir(project.file("src/commonMain/composeResources/files/sdk"))
}
