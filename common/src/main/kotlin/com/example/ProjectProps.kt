package com.example

import java.util.Properties as JavaProperties

object ProjectProps {
    private val props: Map<String, String>
    // This must be available at compile time because it is used in annotations,
    // which is why it can only be checked and not automatic.
    const val MOD_ID = "examplemod"
    init {
        val properties = JavaProperties()
        properties.load(this::class.java.classLoader.getResourceAsStream("project.props"))
        props = properties.entries.associate { it.key.toString() to it.value.toString() }
        if (props.isEmpty()) {
            throw IllegalStateException("No properties found in project.props")
        } else if (MOD_ID != get("modId")) {
            throw IllegalStateException("modId in project.props does not match the expected value: $MOD_ID")
        }
    }

    operator fun get(key: String): String {
        return props[key] ?: throw NoSuchElementException("Property '$key' not found")
    }

    @JvmStatic
    fun getSafe(key: String): String? {
        return props[key]
    }

    @JvmStatic
    fun forceGet(key: String): String {
        return props[key] ?: throw NoSuchElementException("Property '$key' not found")
    }
}