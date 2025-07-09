package de.xyndra.examplemod

import de.xyndra.examplemod.utils.LoadUtil
import de.xyndra.examplemod.utils.ProjectProps
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class CreativeTabInfo(
        val name: String,
        val iconItem: String? = null,
        val items: MutableList<String> = mutableListOf()
)

data class ItemInfo(
    val tab: ResourceKey<CreativeModeTab>? = null,
    val tabName: String? = null,
) {
    init {
        if (tab == null && tabName == null) {
            throw IllegalArgumentException("Either 'tab' or 'tabName' must be provided for ItemInfo")
        } else if (tab != null && tabName != null) {
            throw IllegalArgumentException("Only one of 'tab' or 'tabName' should be provided for ItemInfo")
        }
    }
}

data class BlockInfo(
    val shouldCreateItem: Boolean = true,
)

sealed interface ConfigOption<T> {
    val defaultValue: T
    var value: T

    fun predicate(new: Any): Boolean

    fun reset() {
        value = defaultValue
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

object Globals {
    val logger: Logger = LoggerFactory.getLogger(ProjectProps["modName"])

    val items: MutableMap<String, (props: Item.Properties) -> Item> = mutableMapOf()
    val itemInfos: MutableMap<String, ItemInfo> = mutableMapOf()
    val blocks: MutableMap<String, (props: BlockBehaviour.Properties) -> Block> = mutableMapOf()
    val blockInfos: MutableMap<String, BlockInfo> = mutableMapOf()
    val creativeTabs: MutableMap<String, CreativeTabInfo> = mutableMapOf()
    // The Any is the instance that holds the Field
    val configOptions: MutableMap<String, ConfigOption<*>> = mutableMapOf()

    fun loadAssets() {
        LoadUtil.loadAll("de.xyndra.${ProjectProps["modId"]}.items")
        LoadUtil.loadAll("de.xyndra.${ProjectProps["modId"]}.blocks")
        LoadUtil.loadAll("de.xyndra.${ProjectProps["modId"]}.tabs")
        LoadUtil.loadAll("de.xyndra.${ProjectProps["modId"]}.options")
        info("Finished loading all assets from de.xyndra.${ProjectProps["modId"]}")
    }
}

fun info(message: String, vararg args: Any?) {
    Globals.logger.info(message, *args)
}
