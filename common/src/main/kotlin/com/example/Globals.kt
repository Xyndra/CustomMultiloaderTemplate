package com.example

import com.example.utils.LoadUtil
import com.example.utils.ProjectProps
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

object Globals {
    val logger: Logger = LoggerFactory.getLogger(ProjectProps["modName"])

    val items: MutableMap<String, (props: Item.Properties) -> Item> = mutableMapOf()
    val itemInfos: MutableMap<String, ItemInfo> = mutableMapOf()
    val blocks: MutableMap<String, (props: BlockBehaviour.Properties) -> Block> = mutableMapOf()
    val blockInfos: MutableMap<String, BlockInfo> = mutableMapOf()
    val creativeTabs: MutableMap<String, CreativeTabInfo> = mutableMapOf()

    fun loadAssets() {
        LoadUtil.loadAll("com.example.items")
        LoadUtil.loadAll("com.example.blocks")
        LoadUtil.loadAll("com.example.tabs")
        info("Finished loading all items, blocks and creative tabs from com.example")
    }
}

fun info(message: String, vararg args: Any?) {
    Globals.logger.info(message, *args)
}
