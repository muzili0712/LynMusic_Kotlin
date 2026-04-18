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
        // appleMain 无需额外依赖；cinterop 在下方配置
    }

    listOf(iosArm64(), iosSimulatorArm64(), macosArm64()).forEach { target ->
        target.compilations.getByName("main") {
            cinterops {
                create("javascriptcore") {
                    defFile(project.file("src/nativeInterop/cinterop/javascriptcore.def"))
                    packageName("platform.jsc")
                }
            }
        }
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
