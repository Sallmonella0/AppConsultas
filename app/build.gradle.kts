import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    // GARANTA QUE ESTE PLUGIN ESTEJA NO SEU ROOT build.gradle.kts
    // id("com.google.gms.google-services") version "4.4.0" apply false
}

android {
    namespace = "com.example.appconsultas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appconsultas"
        minSdk = 24
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
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // --- Core Dependencies ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Compose BOM Platform ---
    implementation(platform(libs.androidx.compose.bom))

    // --- Compose Core UI ---
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Navigation and ViewModel ---
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ===========================================
    // DEPENDÊNCIAS CRÍTICAS ADICIONADAS/CORRIGIDAS
    // ===========================================

    // 1. Resolve todos os erros de ícones como DarkMode, Sort, Visibility, etc.
    implementation("androidx.compose.material:material-icons-extended")

    // 2. Importa o Firebase BOM para gerenciar versões
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // 3. Resolve o erro de 'collectAsStateWithLifecycle' (se ainda não resolvido)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // --- Networking ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    // --- Testing Dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Testes de UI
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}