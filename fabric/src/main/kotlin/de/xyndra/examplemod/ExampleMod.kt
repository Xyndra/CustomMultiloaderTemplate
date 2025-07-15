package de.xyndra.examplemod

import de.xyndra.examplemod.utils.ProjectProps
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
import net.minecraft.world.level.ItemLike
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
                                when (tabInfo.iconItem) {
                                    is ItemReference.ItemKey -> (tabInfo.iconItem as ItemReference.ItemKey).key.defaultInstance
                                    is ItemReference.ItemName -> items[(tabInfo.iconItem as ItemReference.ItemName).name]?.defaultInstance
                                    else -> Blocks.DIRT.asItem().defaultInstance
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
            for ((tabRef, itemRef) in info.tabs) {
                val itemKey: ItemLike? = when (itemRef) {
                    is ItemReference.ItemKey -> itemRef.key
                    is ItemReference.ItemName -> items[itemRef.name]
                    null -> null
                }

                when (tabRef) {
                    is TabReference.TabKey -> ItemGroupEvents.modifyEntriesEvent(tabRef.key).register { entries ->
                        if (itemKey != null) {
                            entries.addAfter(itemKey, item)
                        } else {
                            entries.accept(item)
                        }
                    }

                    is TabReference.TabName -> ItemGroupEvents.modifyEntriesEvent(
                        ResourceKey.create(
                            Registries.CREATIVE_MODE_TAB,
                            ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], tabRef.name)
                        )
                    ).register { entries ->
                        if (itemKey != null) {
                            entries.addAfter(itemKey, item)
                        } else {
                            entries.accept(item)
                        }
                    }
                }
            }
        }

        ExampleModConfigWrapper.createAndLoad()
    }
}
