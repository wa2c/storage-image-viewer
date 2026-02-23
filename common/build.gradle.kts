import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
}

val appId: String by rootProject.extra
val appJavaVersion: JavaVersion by rootProject.extra
val appTargetSdkVersion: Int by rootProject.extra
val appMinSdkVersion: Int by rootProject.extra

configure<LibraryExtension> {
    namespace = "$appId.common"
    compileSdk = appTargetSdkVersion

    defaultConfig {
        minSdk = appMinSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(appJavaVersion.majorVersion))
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
