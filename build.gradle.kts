// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.abount.libraries) apply false
}

// Global Definition
val appId by extra ("com.wa2c.android.storageimageviewer")
val appJavaVersion by extra (JavaVersion.VERSION_21)
val appTargetSdkVersion by extra (35)
val appMinSdkVersion by extra (26)
val appVersionCode by extra(1)
val appVersionName by extra("1.0.0")

tasks.register("convertString") {
    // Spreadsheet URL
    System.setProperty(
        "string_converter_spreadsheet_url",
        "https://docs.google.com/spreadsheets/d/1UjFqywhYgNrng6ZNJ6efUKvGIR7LTGAcrSltTMsNZvA/export?format=csv#gid=0"
    )
    // res folder path
    System.setProperty(
        "string_converter_res_path",
        "../../presentation/src/main/res/"
    )
    dependsOn(gradle.includedBuild("string_converter").task(":convertString"))
}
