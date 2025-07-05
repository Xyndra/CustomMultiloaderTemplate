package com.example

import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import org.slf4j.LoggerFactory

object ExampleMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("assets/examplemod")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")

		Globals.loadAssets()
		for ((name, constructor) in Globals.items) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], name),
				constructor(Item.Properties().setId(
					ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ProjectProps["modId"], name)))))
		}
	}
}