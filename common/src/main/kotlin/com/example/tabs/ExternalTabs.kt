package com.example.tabs

import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs

object ExternalTabs {
    fun grabTab(name: String): ResourceKey<CreativeModeTab> {
        val field = CreativeModeTabs::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(null) as ResourceKey<CreativeModeTab>
    }

    val BUILDING_BLOCKS = grabTab("BUILDING_BLOCKS")
    val COLORED_BLOCKS = grabTab("COLORED_BLOCKS")
    val NATURAL_BLOCKS = grabTab("NATURAL_BLOCKS")
    val FUNCTIONAL_BLOCKS = grabTab("FUNCTIONAL_BLOCKS")
    val REDSTONE_BLOCKS = grabTab("REDSTONE_BLOCKS")
    val HOTBAR = grabTab("HOTBAR")
    val SEARCH = grabTab("SEARCH")
    val TOOLS_AND_UTILITIES = grabTab("TOOLS_AND_UTILITIES")
    val COMBAT = grabTab("COMBAT")
    val FOOD_AND_DRINKS = grabTab("FOOD_AND_DRINKS")
    val INGREDIENTS = grabTab("INGREDIENTS")
    val SPAWN_EGGS = grabTab("SPAWN_EGGS")
    val OP_BLOCKS = grabTab("OP_BLOCKS")
    val INVENTORY = grabTab("INVENTORY")
}