package de.xyndra.examplemod.tabs

import de.xyndra.examplemod.CreativeTabInfo
import de.xyndra.examplemod.Globals
import de.xyndra.examplemod.ItemReference
import de.xyndra.examplemod.items.ExampleItem
import de.xyndra.examplemod.utils.LoadKt

@LoadKt
object ExampleTab {
    const val NAME = "example_tab"
    fun load() {
        println("Loading ExampleTab...")

        val tabInfo = CreativeTabInfo(
            name = NAME,
            iconItem = ItemReference.ItemName(ExampleItem.NAME)
        )

        Globals.creativeTabs[NAME] = tabInfo
    }
}
