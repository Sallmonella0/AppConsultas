import java.util.Properties // <--- A CORREÇÃO

// Lê o ficheiro local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.appconsultas" // O namespace correto
    compileSdk = 36 // Atualizado para 36

    defaultConfig {
        applicationId = "com.example.appconsultas"
        minSdk = 24
        targetSdk = 36 // Atualizado para 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // --- Bloco de credenciais seguras ---
        buildConfigField("String", "VIP_USER", "\"${localProperties.getProperty("VIP_USER", "")}\"")
        buildConfigField("String", "VIP_PASS", "\"${localProperties.getProperty("VIP_PASS", "")}\"")
        buildConfigField("String", "CKL_USER", "\"${localProperties.getProperty("CKL_USER", "")}\"")
        buildConfigField("String", "CKL_PASS", "\"${localProperties.getProperty("CKL_PASS", "")}\"")
        buildConfigField("String", "REVERSELOG_USER", "\"${localProperties.getProperty("REVERSELOG_USER", "")}\"")
        buildConfigField("String", "REVERSELOG_PASS", "\"${localProperties.getProperty("REVERSELOG_PASS", "")}\"")
        buildConfigField("String", "RODOLEVE_USER", "\"${localProperties.getProperty("RODOLEVE_USER", "")}\"")
        buildConfigField("String", "RODOLEVE_PASS", "\"${localProperties.getProperty("RODOLEVE_PASS", "")}\"")
        buildConfigField("String", "BELOOG_USER", "\"${localProperties.getProperty("BELOOG_USER", "")}\"")
        buildConfigField("String", "BELOOG_PASS", "\"${localProperties.getProperty("BELOOG_PASS", "")}\"")
        buildConfigField("String", "GALLOTTI_USER", "\"${localProperties.getProperty("GALLOTTI_USER", "")}\"")
        buildConfigField("String", "GALLOTTI_PASS", "\"${localProperties.getProperty("GALLOTTI_PASS", "")}\"")
        buildConfigField("String", "TRANSGIRES_USER", "\"${localProperties.getProperty("TRANSGIRES_USER", "")}\"")
        buildConfigField("String", "TRANSGIRES_PASS", "\"${localProperties.getProperty("TRANSGIRES_PASS", "")}\"")
        buildConfigField("String", "AGREGAMAIS_USER", "\"${localProperties.getProperty("AGREGAMAIS_USER", "")}\"")
        buildConfigField("String", "AGREGAMAIS_PASS", "\"${localProperties.getProperty("AGREGAMAIS_PASS", "")}\"")
        // --- Fim do Bloco ---
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
        isCoreLibraryDesugaringEnabled = true // <--- Habilita uso de java.time em minSdk baixo
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true // Adicione esta linha para re-ativar o BuildConfig

    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core e Testes
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose (BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Dynamic Color está incluído aqui
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling)

    // --- Dependências do seu app (todas as que precisa) ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.androidx.material.icons.extended)

    // Habilita uso de java.time em minSdk < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}