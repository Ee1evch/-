plugins { id("com.android.application") }
android {
    namespace = "app.alarmchallenge"
    compileSdk = 36
    defaultConfig { applicationId = "app.alarmchallenge"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "1.1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
