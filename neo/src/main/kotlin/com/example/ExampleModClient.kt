package com.example

import com.example.utils.ProjectProps
import net.minecraft.client.gui.screens.Screen
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ProjectProps.MOD_ID, dist = [Dist.CLIENT])
class ExampleModClient(container: ModContainer) {
    init {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint<IConfigScreenFactory>(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { mod: ModContainer, parent: Screen? -> ConfigurationScreen(mod, parent) })
    }
}