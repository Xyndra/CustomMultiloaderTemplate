package com.example

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer
import java.util.function.Supplier

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ProjectProps.MOD_ID)
class ExampleMod(modEventBus: IEventBus, modContainer: ModContainer) {
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    init {
        // Register the commonSetup method for mod loading
        modEventBus.addListener { event: FMLCommonSetupEvent ->
            this.commonSetup(
                event
            )
        }

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus)
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus)
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus)

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this)

        // Register the item to a creative tab
        modEventBus.addListener { event: BuildCreativeModeTabContentsEvent ->
            this.addCreative(
                event
            )
        }

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        info("HELLO FROM COMMON SETUP")

        if (Config.LOG_DIRT_BLOCK.asBoolean) {
            info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
        }

        info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.asInt)

        Config.ITEM_STRINGS.get().forEach(Consumer { item: String? -> info("ITEM >> {}", item) })
    }

    // Add the example block item to the building blocks tab
    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey === CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM)
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent?) {
        // Do something when the server starts
        info("HELLO from server starting")
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
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

    companion object {
        // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(ProjectProps.MOD_ID)

        // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(ProjectProps.MOD_ID)

        // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectProps.MOD_ID)

        // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
        val EXAMPLE_BLOCK: DeferredBlock<Block> = BLOCKS.registerSimpleBlock(
            "example_block", BlockBehaviour.Properties.of().mapColor(
                MapColor.STONE
            )
        )

        // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)

        private val registered_items = mutableMapOf<String, DeferredItem<Item>>()
        init {
            Globals.loadAssets()
            for ((name, constructor) in Globals.items) {
                // Register each item from the Globals.items map
                registered_items[name] = ITEMS.registerItem(name, constructor)
            }
        }

        // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
        val EXAMPLE_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register(
            "example_tab",
            Supplier {
                CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon {
                        // The icon for the CreativeModeTab, can be any item or block
                        registered_items.entries.firstOrNull()?.value?.get()?.defaultInstance
                            ?: Blocks.DIRT.asItem().defaultInstance
                    }
                    .displayItems { _: CreativeModeTab.ItemDisplayParameters?, output: CreativeModeTab.Output ->
                        for ((name, _) in Globals.items) {
                            // Add each item from the Globals.items map to the creative tab
                            val item = registered_items[name]!!.get()
                            output.accept(item.defaultInstance)
                        }
                    }.build()
            })
    }
}
