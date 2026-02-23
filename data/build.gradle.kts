import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.room)
}

val appId: String by rootProject.extra
val appJavaVersion: JavaVersion by rootProject.extra
val appTargetSdkVersion: Int by rootProject.extra
val appMinSdkVersion: Int by rootProject.extra

configure<LibraryExtension> {
    namespace = "$appId.data"
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

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // File
    implementation(libs.androidx.documentfile)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
