pluginManagement {
    repositories {
        maven {
            url = uri("https://raw.githubusercontent.com/graalvm/native-build-tools/snapshots")
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "playerdata"