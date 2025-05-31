plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "net.aetherialkilix"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}

println("GRAALVM_HOME: ${System.getenv("GRAALVM_HOME")}")
graalvmNative {
    binaries {
        named("main") {
            imageName = "playerdata"
            mainClass = "MainKt"
            fallback = false
        }
    }
}