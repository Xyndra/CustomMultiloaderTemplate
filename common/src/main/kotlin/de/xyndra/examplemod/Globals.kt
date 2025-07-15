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
    val iconItem: ItemReference? = null,
)

sealed class TabReference {
    data class TabKey(val key: ResourceKey<CreativeModeTab>) : TabReference()
    data class TabName(val name: String) : TabReference()
}

sealed class ItemReference {
    data class ItemKey(val key: Item) : ItemReference()
    data class ItemName(val name: String) : ItemReference()
}

data class ItemInfo(
    val tabs: List<Pair<TabReference, ItemReference?>>,
)

data class BlockInfo(
    val shouldCreateItem: Boolean = true,
)

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
