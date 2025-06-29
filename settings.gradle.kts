pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
			name = "NeoForged Maven"
			url = uri("https://maven.neoforged.net/releases")
		}
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
	}
	val loomVersion: String by settings
	plugins {
		id("fabric-loom") version loomVersion apply false
		id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
	}
}



val modName: String by settings
rootProject.name = modName
include(":fabric", ":neo", ":common")