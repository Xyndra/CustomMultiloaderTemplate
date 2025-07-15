package de.xyndra.examplemod

import de.xyndra.examplemod.utils.ProjectProps
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ProjectProps.MOD_ID)
class ExampleMod(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        loadConfigOptions()
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)

        NeoForge.EVENT_BUS.register(NeoEvents)
        modEventBus.register(this)

    }

    object NeoEvents {
        @SubscribeEvent
        fun onServerStarting(event: ServerStartingEvent?) {
            // Do something when the server starts
            info("HELLO from server starting")
        }
    }

    @SubscribeEvent
    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        info("HELLO FROM COMMON SETUP")
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class
    // annotated with @SubscribeEvent
    @EventBusSubscriber(modid = ProjectProps.MOD_ID, value = [Dist.CLIENT])
    object ClientModEvents {
        @SubscribeEvent
        @JvmStatic
        fun onClientSetup(event: FMLClientSetupEvent?) {
            // Some client setup code
            info("HELLO FROM CLIENT SETUP")
            info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        }
    }

    @SubscribeEvent
    fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        for ((name, info) in sortItemsByDependencies()) {
            val item = registeredItems[name]?.get() ?: continue
            for ((tabRef, itemRef) in info.tabs) {
                val itemKey: ItemLike? = when (itemRef) {
                    is ItemReference.ItemKey -> itemRef.key
                    is ItemReference.ItemName -> registeredItems[itemRef.name]?.get()
                    null -> null
                }
                when (tabRef) {
                    is TabReference.TabKey -> {
                        if (event.tabKey == tabRef.key) {
                            if (itemKey != null) {
                                event.insertAfter(
                                    itemKey.asItem().defaultInstance,
                                    item.defaultInstance,
                                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                                )
                            } else {
                                event.accept(item)
                            }
                        }
                    }

                    is TabReference.TabName -> {
                        val resourceLocation =
                            ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], tabRef.name)
                        val resourceKey =
                            ResourceKey.create(Registries.CREATIVE_MODE_TAB, resourceLocation)
                        if (event.tabKey == resourceKey) {
                            if (itemKey != null) {
                                event.insertAfter(
                                    itemKey.asItem().defaultInstance,
                                    item.defaultInstance,
                                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                                )
                            } else {
                                event.accept(item)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(ProjectProps.MOD_ID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(ProjectProps.MOD_ID)
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
                DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectProps.MOD_ID)

        private val registeredItems = mutableMapOf<String, DeferredItem<out Item>>()
        private val registeredBlocks = mutableMapOf<String, DeferredBlock<Block>>()
        private val registeredTabs =
                mutableMapOf<String, DeferredHolder<CreativeModeTab, CreativeModeTab>>()

        init {
            Globals.loadAssets()

            // Register items
            for ((name, constructor) in Globals.items) {
                registeredItems[name] = ITEMS.registerItem(name, constructor)
            }

            // Register blocks
            for ((name, constructor) in Globals.blocks) {
                registeredBlocks[name] = BLOCKS.registerBlock(name, constructor)
                if (Globals.blockInfos[name]?.shouldCreateItem == false) {
                    continue
                }
                registeredItems[name] = ITEMS.registerSimpleBlockItem(registeredBlocks[name]!!)
            }

            // Register creative tabs
            for ((tabName, tabInfo) in Globals.creativeTabs) {
                registeredTabs[tabName] = CREATIVE_MODE_TABS.register(tabName) { _ ->
                    CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.${ProjectProps.MOD_ID}.$tabName"))
                        .icon {
                            when (tabInfo.iconItem) {
                                is ItemReference.ItemKey -> (tabInfo.iconItem as ItemReference.ItemKey).key.defaultInstance
                                is ItemReference.ItemName -> registeredItems[(tabInfo.iconItem as ItemReference.ItemName).name]?.get()?.defaultInstance
                                else -> Blocks.DIRT.asItem().defaultInstance
                            }
                        }
                        .build()
                }
            }
        }
    }
}
