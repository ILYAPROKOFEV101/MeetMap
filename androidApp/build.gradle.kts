plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "1.9.0"
    id("kotlin-kapt")

}



android {
    namespace = "com.ilya.MeetingMap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ilya.MeetingMap"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.transport.runtime)
    implementation(libs.protolite.well.known.types)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.common)
    implementation (libs.androidx.fragment.ktx.v185)
    implementation(libs.foundation.layout.android)
    implementation(libs.androidx.ui.test.android)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage) // Убедитесь, что версия соответствует последней







    testImplementation(libs.junit)

    implementation(libs.coil.compose) // Coil для загрузки изображений (Compose)
    implementation(platform("com.google.firebase:firebase-bom:33.1.1")) // Firebase Bill of Materials (BOM)
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication (Kotlin)
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Google Play Services Auth
    implementation("androidx.compose.material3:material3:1.2.1") // Замените на актуальную версию
    implementation("com.google.android.gms:play-services-maps:18.1.0")// для googlemaps
    implementation ("com.google.accompanist:accompanist-permissions:0.35.1-alpha") //

    implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.0.2")

    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // CALENDAR
    implementation ("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2")
    // CLOCK
    implementation ("com.maxkeppeler.sheets-compose-dialogs:clock:1.0.2")
    // MEterial 3 icon
    implementation("androidx.compose.material3:material3:1.2.1") // Или более новая версия
    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("com.google.android.gms:play-services-location:21.3.0") // Or the latest version
    implementation (libs.material.v150)
    implementation (libs.material)

    // Server setings

    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)        // Движок CIO
    implementation(libs.ktor.client.android)       // Движок Android
    implementation(libs.ktor.client.logging)       // Логирование
    implementation(libs.ktor.client.json)          // Работа с JSON
    implementation(libs.ktor.client.serialization) // Сериализация
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.kotlinx.serialization.json.v160) // JSON сериализация от kotlinx
// retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)


    implementation (libs.okhttp)
    implementation (libs.converter.gson)




    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.jetbrains.kotlinx.serialization.json) // Используйте актуальную версию kotlinx-serialization-json
    implementation (libs.konfetti.xml)


    implementation (libs.glide) // load image
    implementation (libs.glide) // load image
    implementation (libs.okhttp.v4120) // Websoket liber for conect
    implementation (libs.logging.interceptor)


    implementation (libs.glide)
    implementation (libs.compiler)



    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")


   
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
    implementation (libs.androidx.core.splashscreen)

    testImplementation (libs.kotlinx.coroutines.test)
    testImplementation (libs.mockk)
    testImplementation (libs.truth)





}