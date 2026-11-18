plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Versão única do app; o site (updates.json) e o aviso de atualização usam este valor.
// Release solicitada pelo mantenedor. versionCode > v0.1.0 para atualização sem perda de dados.
val appVersionName = "1.4"
val appVersionCode = 20

// Canal Google Play: `./gradlew bundleRelease -Pplay=true`. A Play proíbe que o app se atualize fora da loja,
// então nesse build o aviso de nova versão do site (#39) fica desligado. Sem a propriedade = APK do site.
val playBuild = (findProperty("play") as String?)?.toBoolean() == true

android {
    namespace = "com.pt2vhf.aprs"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pt2vhf.aprs"
        minSdk = 26
        targetSdk = 36  // exigência da Google Play desde 31/08/2026
        versionCode = appVersionCode
        versionName = appVersionName
        buildConfigField("boolean", "SELF_UPDATE", (!playBuild).toString())
    }

    // Assinatura vinda dos segredos do GitHub Actions (nunca do repositório).
    val keystorePath = System.getenv("ANDROID_KEYSTORE_FILE")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    // #68 Android Auto (modelos da biblioteca Car App)
    implementation("androidx.car.app:app:1.4.0")
    testImplementation("junit:junit:4.13.2")
}
