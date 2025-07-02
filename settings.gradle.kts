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
// Set the names of the subprojects to be like "Example Mod - Fabric"
val modId: String by settings
rootProject.children.forEach { subproject ->
	subproject.name = "$modName - ${subproject.name.replaceFirstChar { it.uppercase() }}"
}
include(":fabric", ":neo", ":common")