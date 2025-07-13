package de.xyndra.examplemod

import de.xyndra.examplemod.Globals.logger
import de.xyndra.examplemod.utils.ProjectProps
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
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
        for ((name, info) in Globals.itemInfos) {
            val item = registeredItems[name]?.get() ?: continue
            if (info.tab != null) {
                if (event.tabKey == info.tab) {
                    event.accept(item)
                }
            } else if (info.tabName != null) {
                val resourceLocation = ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], info.tabName!!)
                val resourceKey = ResourceKey.create(Registries.CREATIVE_MODE_TAB, resourceLocation)
                if (event.tabKey == resourceKey) {
                    event.accept(item)
                }
            } else {
                logger.warn("ItemInfo for item {} is missing tab or tabName", name)
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

            // Register blocks
            for ((name, constructor) in Globals.blocks) {
                registeredBlocks[name] = BLOCKS.registerBlock(name, constructor)
                if (Globals.blockInfos[name]?.shouldCreateItem == false) {
                    continue
                }
                registeredItems[name] = ITEMS.registerSimpleBlockItem(registeredBlocks[name]!!)
            }

            // Register items
            for ((name, constructor) in Globals.items) {
                registeredItems[name] = ITEMS.registerItem(name, constructor)
            }

            // Register creative tabs
            for ((tabName, tabInfo) in Globals.creativeTabs) {
                registeredTabs[tabName] = CREATIVE_MODE_TABS.register(tabName) { _ ->
                    CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.${ProjectProps.MOD_ID}.$tabName"))
                        .icon {
                            if (tabInfo.iconItem == null) {
                                Blocks.DIRT.asItem().defaultInstance
                            } else {
                                registeredItems[tabInfo.iconItem]?.get()?.defaultInstance
                                    ?: Blocks.DIRT.asItem().defaultInstance
                            }
                        }
                        .build()
                }
            }
        }
    }
}
