package com.example.items

import com.example.Globals
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
        fun load() {
            println("Loading ExampleItem...")
            Globals.items["example_item"] = { props: Properties -> ExampleItem(props) }
        }
    }
}