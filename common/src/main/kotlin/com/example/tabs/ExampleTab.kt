package com.example.tabs

import com.example.CreativeTabInfo
import com.example.Globals
import com.example.items.ExampleItem
import com.example.utils.LoadKt

@LoadKt
object ExampleTab {
    const val NAME = "example_tab"
    fun load() {
        println("Loading ExampleTab...")

        val tabInfo = CreativeTabInfo(
            name = NAME,
            iconItem = ExampleItem.NAME
        )

        Globals.creativeTabs[NAME] = tabInfo
    }
}
