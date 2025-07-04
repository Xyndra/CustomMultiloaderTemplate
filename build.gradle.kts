plugins {
    kotlin("jvm") version "2.1.21" apply false
    id("net.neoforged.gradle.common") version "7.0.189"
}

subprojects {
    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Wisp Forest"
            url = uri("https://maven.wispforest.io/releases")
        }
        mavenLocal()
        mavenCentral()
        google()
    }
}
