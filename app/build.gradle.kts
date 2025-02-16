plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val appId: String by rootProject.extra
val appJavaVersion: JavaVersion by rootProject.extra
val appTargetSdkVersion: Int by rootProject.extra
val appMinSdkVersion: Int by rootProject.extra
val appVersionCode: Int by rootProject.extra
val appVersionName: String by rootProject.extra

android {
    namespace = appId
    compileSdk = appTargetSdkVersion

    defaultConfig {
        applicationId = applicationId
        minSdk = appMinSdkVersion
        targetSdk = appTargetSdkVersion
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        base.archivesName.set("StorageImageViewer-${versionName}")
    }

    buildTypes {
        debug {
            versionNameSuffix = "D"
            applicationIdSuffix = ".dev"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    signingConfigs  {
        getByName("debug") {
            storeFile = file("$rootDir/debug.keystore")
        }
    }

    compileOptions {
        sourceCompatibility = appJavaVersion
        targetCompatibility = appJavaVersion
    }
    kotlinOptions {
        jvmTarget = appJavaVersion.majorVersion
        freeCompilerArgs = listOf(
            "-Xstring-concat=inline",
        )
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":presentation"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
