import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.internal.os.OperatingSystem

group = "org.example"
version = "1.0-SNAPSHOT"

plugins {
    java
    kotlin("jvm") version "1.3.50"
    application
}

repositories {
    maven( "https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
    mavenCentral()
    mavenLocal()
    jcenter()
}

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.openjfx:javafx-plugin:0.0.8")
    }
}

apply {
    plugin("java")
    plugin("kotlin")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val os = OperatingSystem.current()!!
val platform = when { os.isWindows -> "win"; os.isLinux-> "linux"; os.isMacOsX -> "mac"; else -> error("Unknown OS") }

dependencies {
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-RC2")
    implementation(kotlin("stdlib-jdk8"))
    compile("org.json", "json", "20090211")
    compile("org.jetbrains.kotlin", "kotlin-reflect", "1.3.21")


}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.verbose = true // Let the compiler spill some more messages when there is an error
}

tasks.withType(Jar::class) {
    manifest {
        attributes("Main-Class" to "week3.dronesimulationjava.DroneUI")
    }

    from (
        configurations.compile.get().map { if (it.isDirectory) it else zipTree(it) }
    )
}