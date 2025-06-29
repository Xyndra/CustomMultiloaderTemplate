plugins {
	id("fabric-loom")
	id("maven-publish")
	kotlin("jvm")
}

val minecraftVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project
val parchmentVersion: String by project
val modName: String by project
val modDescription: String by project
val modAuthors: String by project
val modLicense: String by project
val modVersion: String by project
val mavenGroup: String by project
val modId: String by project

base {
	archivesName = "$modId-$minecraftVersion-fabric-$modVersion"
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

fabricApi {
	configureDataGeneration {
		client = true
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${minecraftVersion}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${parchmentVersion}")
	})
	modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

	implementation(project(":common"))
}

val replacements = mapOf(
	"minecraft_version" to minecraftVersion,
	"loader_version" to loaderVersion,
	"fabric_version" to fabricVersion,
	"fabric_kotlin_version" to fabricKotlinVersion,
	"parchment_version" to parchmentVersion,
	"mod_version" to modVersion,
	"group_id" to mavenGroup,
	"mod_name" to modName,
	"mod_description" to modDescription,
	"mod_authors" to modAuthors.replace(", ", "\", \""),
	"mod_license" to modLicense,
	"mod_id" to modId
)

tasks.named<ProcessResources>("processResources") {
	filesNotMatching(mutableSetOf("**/*.png")) {
		expand(replacements)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}


java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.named<Jar>("jar") {
	from("LICENSE") {
		rename { "${it}_${modId}" }
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = modId
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}