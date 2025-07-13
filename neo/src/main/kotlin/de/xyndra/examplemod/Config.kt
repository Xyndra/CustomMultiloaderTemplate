package de.xyndra.examplemod

import de.xyndra.examplemod.Globals.logger
import de.xyndra.examplemod.utils.ProjectProps
import kotlinx.serialization.json.*
import net.neoforged.fml.loading.FMLPaths
import java.io.File

fun loadConfigOptions() {
    val file = File(FMLPaths.CONFIGDIR.get().toFile(), "${ProjectProps.MOD_ID}.json")
    if (!file.exists()) {
        logger.warn("Config file for ${ProjectProps.MOD_ID} not found at ${file.absolutePath}. Using default values.")
        return
    }
    val configText = file.readText()
    val json = Json {
        ignoreUnknownKeys = true
    }
    val jsonObject = json.parseToJsonElement(configText).jsonObject
    val values = jsonObject.toMap()
    info("Loaded ${values.size} config options for ${ProjectProps.MOD_ID} from ${file.absolutePath}.")
    try {
        for ((key, value) in values) {
            val actualValue: Any = when {
                value.jsonPrimitive.intOrNull != null -> value.jsonPrimitive.int
                value.jsonPrimitive.floatOrNull != null -> value.jsonPrimitive.float
                value.jsonPrimitive.booleanOrNull != null -> value.jsonPrimitive.boolean
                value.jsonPrimitive.contentOrNull != null -> value.jsonPrimitive.content
                else -> {
                    logger.warn("Unknown type for key '$key'")
                    continue
                }
            }
            val option = Globals.configOptions[key]
            if (option != null && option.predicate(actualValue)) {
                val field = option::class.java.getDeclaredField("value")
                field.isAccessible = true
                field.set(option, actualValue)
            } else {
                logger.warn("Config option '$key' not found or value does not match predicate in ${ProjectProps.MOD_ID}.")
            }
        }
    } catch (e: Exception) {
        throw RuntimeException("Failed to parse config options for ${ProjectProps.MOD_ID}", e)
    }
}