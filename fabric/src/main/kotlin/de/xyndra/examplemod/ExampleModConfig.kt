package de.xyndra.examplemod

import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonGrammar
import blue.endless.jankson.annotation.Serializer
import de.xyndra.examplemod.Globals.configOptions
import de.xyndra.examplemod.utils.ProjectProps
import io.wispforest.owo.config.ConfigWrapper
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu
import java.io.Writer

@Config(name = ProjectProps.MOD_ID, wrapperName = "")
@Modmenu(modId = ProjectProps.MOD_ID)
class ExampleModConfig {
    @Serializer
    fun serialize(): JsonElement {
        return object : JsonElement() {
            override fun clone(): JsonElement {
                return this // Bad but also I don't care
            }

            @Deprecated("Deprecated in Java")
            override fun toJson(comments: Boolean, newlines: Boolean, depth: Int): String {
                return configOptions.serializeJson()
            }

            override fun toJson(writer: Writer, grammar: JsonGrammar, depth: Int) {
                writer.write(configOptions.serializeJson())
            }
        }
    }
}

class ExampleModConfigWrapper : ConfigWrapper<ExampleModConfig>(ExampleModConfig::class.java) {
    companion object {
        fun createAndLoad(): ExampleModConfigWrapper {
            val wrapper = ExampleModConfigWrapper()
            wrapper.load()
            return wrapper
        }

        fun collectFieldValues(
            parent: Option.Key,
            fields: MutableMap<Option.Key, Option.BoundField<Any>>,
        ) {
            for ((name, option) in configOptions) {
                try {
                    val field = option.javaClass.getDeclaredField("value")
                    field.isAccessible = true
                    fields[parent.child(name)] = Option.BoundField(option, field)
                } catch (e: NoSuchFieldException) {
                    throw RuntimeException(e)
                }
            }

            println("Collected ${fields.size} config options from ExampleModConfigWrapper.")
        }
    }
}

