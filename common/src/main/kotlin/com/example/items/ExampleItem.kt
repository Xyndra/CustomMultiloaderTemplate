package com.example.items

import com.example.Globals
import com.example.ItemInfo
import com.example.tabs.ExampleTab
import com.example.utils.LoadKt
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

@LoadKt
class ExampleItem(props: Properties) : Item(
    props
) {
    override fun useOn(ctx: UseOnContext): InteractionResult {
        println("ExampleItem used at position: ${ctx.clickedPos}")
        return InteractionResult.PASS
    }

    companion object {
        const val NAME = "example_item"

        fun load() {
            println("Loading ExampleItem...")
            Globals.items[NAME] = { props: Properties -> ExampleItem(props) }
            Globals.itemInfos[NAME] = ItemInfo(tabName = ExampleTab.NAME)
        }
    }
}