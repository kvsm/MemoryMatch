import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Release signing: environment variables (CI secrets) win if present; otherwise the
// app's own key in app/signing/ is used, so every build installs over the last one.
val signingProps = Properties().apply {
    val f = file("signing/signing.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val keystorePath: String? = System.getenv("MM_KEYSTORE_PATH")
    ?: file("signing/release.jks").takeIf { it.exists() }?.path
fun signingValue(env: String, prop: String): String? =
    System.getenv(env)?.takeIf { it.isNotEmpty() } ?: signingProps.getProperty(prop)

android {
    namespace = "com.kvsm.memorymatch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kvsm.memorymatch"
        minSdk = 26
        targetSdk = 35
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"
    }

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = signingValue("MM_KEYSTORE_PASSWORD", "storePassword")
                keyAlias = signingValue("MM_KEY_ALIAS", "keyAlias")
                keyPassword = signingValue("MM_KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = if (keystorePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    testImplementation("junit:junit:4.13.2")
}
