plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.obsidian.omot"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.obsidian.omot"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-ALPHA"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build config fields for in-world immersion
        buildConfigField("String", "APP_CODE_NAME", "\"OMOT\"")
        buildConfigField("String", "DIRECTORATE_NAME", "\"Obsidian Directorate\"")
        buildConfigField("String", "VERSION_CODENAME", "\"NIGHTFALL\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "IS_DEBUG", "false")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("Boolean", "IS_DEBUG", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Android Core
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.fragment.ktx)

    // UI & Material design
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.constraintlayout)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Security
    implementation(libs.security.crypto)
    implementation(libs.biometric)

    // Lifecycle & architecture
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    // Database (SQLite with Room)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)

    // Encryption
    implementation(libs.core)
    implementation(libs.android.database.sqlcipher)

    // Animation
    implementation(libs.lottie)

    // Utility
    implementation(libs.timber)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}