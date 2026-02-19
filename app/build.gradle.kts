plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.bountyapp.yourrtodo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bountyapp.yourrtodo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // ИЗМЕНИТЕ с 11 на 17
        targetCompatibility = JavaVersion.VERSION_17 // ИЗМЕНИТЕ с 11 на 17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = false
        viewBinding = true
    }

}

dependencies {
    // ========== ROOM DB ==========
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    // ========== ОСНОВНЫЕ БИБЛИОТЕКИ ==========
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // ========== ДЛЯ ИНТЕРФЕЙСА ==========
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ========== ДЛЯ НАВИГАЦИИ ==========
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")

    // ========== ДЛЯ РАБОТЫ С БД ==========
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    // ========== ДОПОЛНИТЕЛЬНЫЕ УТИЛИТЫ ==========
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // ========== ТЕСТИРОВАНИЕ ==========
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}