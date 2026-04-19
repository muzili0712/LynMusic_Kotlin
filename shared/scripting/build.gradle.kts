import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.quickjs.wechat.android)
        }
        jvmMain.dependencies {
            implementation(libs.graal.polyglot)
            implementation(libs.graal.js.community)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
        // appleMain 直接使用 Kotlin/Native 内置的 `platform.JavaScriptCore` 包，
        // 无需自建 cinterop（def 文件保留作参考，但未注册）。
    }
}

android {
    namespace = "top.iwesley.lyn.music.shared.scripting"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
