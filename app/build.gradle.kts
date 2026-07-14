plugins {
    alias(libs.plugins.android.application)
    // Đọc file app/google-services.json (tải từ Firebase Console).
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.oto"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.oto"
        minSdk = 24

        // targetSdk 35+ ép app vẽ tràn viền (edge-to-edge), khiến nội dung chui
        // xuống dưới thanh trạng thái và ActionBar. Giữ ở 34 để hệ thống tự chừa
        // chỗ cho các thanh hệ thống — layout của nhóm không phải xử lý insets.
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)

    // Đọc thẻ EXIF để xoay đúng hướng ảnh chụp từ camera
    implementation(libs.exifinterface)

    // Room (SQLite) — cơ sở dữ liệu offline
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Lifecycle — MVVM (ViewModel + LiveData)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Firebase Authentication — đăng nhập, đăng ký, gửi email đặt lại mật khẩu
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    // Firestore: lưu hồ sơ người dùng + vai trò (user / admin)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}