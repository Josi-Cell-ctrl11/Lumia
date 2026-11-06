plugins { id("com.android.application") }
android {
    namespace = "com.josias.luma"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.josias.luma"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

dependencies {
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta6")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
}
