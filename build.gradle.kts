// Define os plugins que o projeto e os seus módulos podem usar
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlinAndroid) apply false

    // CORREÇÃO: Declarar o plugin do compilador Compose no nível raiz
    alias(libs.plugins.compose.compiler) apply false
}