package de.xyndra.examplemod.items

import de.xyndra.examplemod.Globals
import de.xyndra.examplemod.ItemInfo
import de.xyndra.examplemod.options.TestOptions
import de.xyndra.examplemod.tabs.ExampleTab
import de.xyndra.examplemod.utils.LoadKt
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

@LoadKt
class ExampleItem(props: Properties) : Item(
    props
) {
    override fun useOn(ctx: UseOnContext): InteractionResult {
        if (TestOptions.shouldPrint.value) {
            println("ExampleItem used at position: ${ctx.clickedPos}")
        }
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