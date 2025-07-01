plugins {
    kotlin("jvm") version "2.1.21" apply false
}

subprojects {
    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
        mavenLocal()
        mavenCentral()
        google()
    }
}
