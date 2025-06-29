plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm")
}

val modId: String by project
val modVersion: String by project
val minecraftVersion: String by project

base {
    archivesName = "$modId-$minecraftVersion-common-$modVersion"
}
