plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.LandMarkUpload"
    compileSdk = 34

    packagingOptions {
        exclude ("com/itextpdf/text/pdf/fonts/cmap_info.txt")
    }
    defaultConfig {
        applicationId = "com.example.LandMarkUpload"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.itextpdf:itextg:5.5.10")
    implementation("com.itextpdf:itext-asian:5.2.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("id.zelory:compressor:2.1.1")
}