package de.xyndra.examplemod

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModuleOf

sealed interface ConfigOption<T : Any> {
    val defaultValue: T
    var value: T

    fun predicate(new: Any): Boolean

    fun reset() {
        value = defaultValue
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun Map<String, ConfigOption<*>>.serializeJson(): String {
    val remapped = this.mapValues { (_, option) ->
        option.value
    }
    val json = Json {
        serializersModule = serializersModuleOf(Any::class, DynamicLookupSerializer())
        encodeDefaults = true
        prettyPrint = true
    }
    return json.encodeToString(MapSerializer(String.serializer(), DynamicLookupSerializer()), remapped)
}

@ExperimentalSerializationApi
class DynamicLookupSerializer: KSerializer<Any> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class, null, emptyArray()).descriptor

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Any) {
        if(value is ArrayList<*>){
            encoder.encodeSerializableValue(ListSerializer(DynamicLookupSerializer()), value)
            return
        }
        val actualSerializer = encoder.serializersModule.getContextual(value::class) ?: value::class.serializer()
        @Suppress("UNCHECKED_CAST")
        encoder.encodeSerializableValue(actualSerializer as KSerializer<Any>, value)
    }

    override fun deserialize(decoder: Decoder): Any {
        error("Unsupported")
    }
}


class BooleanConfigOption(
    override val defaultValue: Boolean = false
) : ConfigOption<Boolean> {
    override var value: Boolean = defaultValue
    override fun predicate(new: Any): Boolean {
        return new is Boolean
    }
    override fun toString(): String {
        return value.toString()
    }
}

open class IntConfigOption(
    final override val defaultValue: Int = 0,
    private val minValue: Int = Int.MIN_VALUE,
    private val maxValue: Int = Int.MAX_VALUE
) : ConfigOption<Int> {
    init {
        if (defaultValue < minValue || defaultValue > maxValue) {
            throw IllegalArgumentException("Default value must be between $minValue and $maxValue")
        }
    }

    override var value: Int = defaultValue
    override fun predicate(new: Any): Boolean {
        return new is Int && new in minValue..maxValue
    }
    override fun toString(): String {
        return value.toString()
    }
}
