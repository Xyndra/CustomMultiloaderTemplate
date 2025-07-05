package com.example

import com.example.utils.ProjectProps
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import org.slf4j.LoggerFactory

object ExampleMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("assets/examplemod")

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        logger.info("Hello Fabric world!")

        Globals.loadAssets()

        // Register items
        val items: MutableMap<String, Item> = mutableMapOf()
        for ((name, constructor) in Globals.items) {
            val resourceLocation = ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], name)
            val item = constructor(
                Item.Properties().setId(
                    ResourceKey.create(
                        Registries.ITEM,
                        resourceLocation)))
            items[name] = item
            Registry.register(
                    BuiltInRegistries.ITEM,
                    resourceLocation,
                    item)
        }

        // Register blocks
        val blocks: MutableMap<String, Block> = mutableMapOf()
        for ((name, constructor) in Globals.blocks) {
            val resourceLocation = ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], name)
            val block = constructor(
                BlockBehaviour.Properties.of().setId(
                    ResourceKey.create(
                        Registries.BLOCK,
                        resourceLocation)))
            blocks[name] = block
            Registry.register(
                BuiltInRegistries.BLOCK,
                resourceLocation,
                block
            )

            if (Globals.blockInfos[name]?.shouldCreateItem == false) {
                continue // Skip item creation
            }
            val blockItem = BlockItem(block,
                Item.Properties().setId(
                    ResourceKey.create(
                        Registries.ITEM,
                        resourceLocation))
                    .useBlockDescriptionPrefix())
            items[name] = blockItem
            Registry.register(
                    BuiltInRegistries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], name),
                    blockItem)
        }


        // Register creative tabs
        val tabs: MutableMap<String, CreativeModeTab> = mutableMapOf()
        for ((tabName, tabInfo) in Globals.creativeTabs) {
            val tab =
                    FabricItemGroup.builder()
                            .title(Component.translatable("itemGroup.${ProjectProps["modId"]}.$tabName"))
                            .icon {
                                if (tabInfo.iconItem == null) {
                                    Blocks.DIRT.asItem().defaultInstance
                                } else {
                                    items[tabInfo.iconItem]?.defaultInstance ?: Blocks.DIRT.asItem().defaultInstance
                                }
                            }
                            .build()
            tabs[tabName] = tab
            Registry.register(
                    BuiltInRegistries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], tabName),
                    tab)
        }

        for ((name, info) in Globals.itemInfos) {
            val item = items[name] ?: continue
            if (info.tab != null) {
                ItemGroupEvents.modifyEntriesEvent(info.tab).register { entries -> entries.accept(item.defaultInstance) }
            } else if (info.tabName != null) {
                val resourceLocation = ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], info.tabName!!)
                val resourceKey = ResourceKey.create(Registries.CREATIVE_MODE_TAB, resourceLocation)
                ItemGroupEvents.modifyEntriesEvent(resourceKey).register { entries -> entries.accept(item.defaultInstance) }
            } else {
                logger.warn("Item $name has no tab set in ItemInfo")
            }
        }
    }
}
