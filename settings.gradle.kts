@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google() // Essencial para plugins do Android
        mavenCentral() // Essencial para plugins Kotlin
        gradlePluginPortal() // Essencial para a maioria dos plugins
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "AppConsultas"
include(":app")