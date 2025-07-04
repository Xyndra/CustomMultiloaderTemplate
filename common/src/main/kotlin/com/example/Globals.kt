package com.example

import com.example.utils.LoadUtil
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Globals {
    val logger: Logger = LoggerFactory.getLogger(ProjectProps["modName"])

    val items: MutableMap<String, (props: Item.Properties) -> Item> = mutableMapOf()
    val blocks: MutableMap<String, () -> Block> = mutableMapOf()

    fun loadAssets() {
        LoadUtil.loadAll("com.example.items")
        LoadUtil.loadAll("com.example.blocks")
        info("Finished loading all items and blocks from com.example")
    }
}

fun info(message: String, vararg args: Any?) {
    Globals.logger.info(message, *args)
}