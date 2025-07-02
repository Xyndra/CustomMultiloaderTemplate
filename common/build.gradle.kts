plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm")
    id("net.neoforged.gradle.vanilla") version "7.0.189"
}

val parchmentVersion: String by project

subsystems {
    parchment {
        addRepository(false)
        parchmentArtifact("org.parchmentmc.data:parchment-${project.property("minecraftVersion") as String}:$parchmentVersion")
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public/")
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    exclusiveContent {
        forRepositories(
            maven {
                name = "ParchmentMC"
                url = uri("https://maven.parchmentmc.org")
            },
            maven {
                name = "NeoForged Maven"
                url = uri("https://maven.neoforged.net/releases")
            }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }
}

val modId: String by project
val modVersion: String by project
val minecraftVersion: String by project
val neoVersion: String by project

base {
    archivesName = "$modId-$minecraftVersion-common-$modVersion"
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    compileOnly("net.minecraft:client:$minecraftVersion")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")

}