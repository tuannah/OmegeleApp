// Cấu hình build cho ứng dụng Omegle Android
// Sử dụng Kotlin DSL cho Gradle build script

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.omegeleapp"  // Package name của ứng dụng
    compileSdk = 34  // Android SDK version để compile

    defaultConfig {
        applicationId = "com.example.omegeleapp"  // ID duy nhất của ứng dụng trên Google Play
        minSdk = 26  // Minimum Android version hỗ trợ (Android 8.0)
        targetSdk = 34  // Target Android version (Android 15)
        versionCode = 1  // Version code cho Google Play Store
        versionName = "1.0"  // Version name hiển thị cho người dùng

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"  // Test runner cho instrumentation tests
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // Tắt code minification cho release build
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),  // ProGuard rules mặc định
                "proguard-rules.pro"  // ProGuard rules tùy chỉnh
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        pickFirst("lib/arm64-v8a/libaosl.so")
        pickFirst("lib/armeabi-v7a/libaosl.so")
        pickFirst("lib/x86/libaosl.so")
        pickFirst("lib/x86_64/libaosl.so")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Agora SDK cho voice call (bản mới nhất)
    implementation("io.agora.rtc:voice-sdk:4.5.2")
    // Agora RTM SDK cho signaling thật
    implementation("io.agora.rtm:rtm-sdk:1.5.1")
    
    // OkHttp cho HTTP requests (có thể giữ lại cho các API khác)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    // Dexter cho xử lý runtime permissions
    implementation("com.karumi:dexter:6.2.3")
    // ========== Testing Dependencies ==========
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}