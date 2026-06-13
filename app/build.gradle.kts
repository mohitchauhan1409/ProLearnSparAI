import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

val localProps = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { load(it) }
}


android {
    namespace = "com.prolearn.spar"

    // UPDATED
    compileSdk = 36


    defaultConfig {
        applicationId = "com.prolearn.spar"

        minSdk = 26

        // UPDATED
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProps.getProperty("GEMINI_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "ELEVENLABS_API_KEY",
            "\"${localProps.getProperty("ELEVENLABS_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "ANTHROPIC_API_KEY",
            "\"${localProps.getProperty("ANTHROPIC_API_KEY", "")}\""
        )
    }


    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }


    buildFeatures {

        compose = true

        buildConfig = true
    }
}


// Kotlin JVM target
tasks.withType<KotlinCompile>().configureEach {

    kotlinOptions {
        jvmTarget = "17"
    }
}


dependencies {


    implementation(libs.androidx.compose.foundation.layout)
    val composeBom =
        platform(libs.androidx.compose.bom)

    implementation(composeBom)


    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)


    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.ui.graphics)

    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.compose.ui.text.google.fonts)

    implementation(libs.androidx.core.splashscreen)


    implementation(libs.androidx.navigation.compose)


    implementation(libs.hilt.android)

    ksp(libs.hilt.compiler)

    implementation(libs.androidx.hilt.navigation.compose)


    implementation(libs.ktor.client.android)

    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.ktor.client.logging)


    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)


    testImplementation(libs.junit)


    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(composeBom)

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )


    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}