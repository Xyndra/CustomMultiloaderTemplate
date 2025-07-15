package de.xyndra.examplemod.blocks

import de.xyndra.examplemod.Globals
import de.xyndra.examplemod.ItemInfo
import de.xyndra.examplemod.ItemReference
import de.xyndra.examplemod.TabReference
import de.xyndra.examplemod.items.ExampleItem
import de.xyndra.examplemod.mixin.CreativeModeTabsMixin
import de.xyndra.examplemod.options.TestOptions
import de.xyndra.examplemod.utils.LoadKt
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

@LoadKt
class ExampleBlock(properties: Properties) : Block(properties) {
    override fun useWithoutItem(
            blockState: BlockState,
            level: Level,
            blockPos: BlockPos,
            player: Player,
            blockHitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide) {
            println("ExampleBlock used at position: $blockPos by player: ${player.name.string}")
            println("Int Option values: " +
                    "${TestOptions.testInt1.value}, " +
                    "${TestOptions.testInt2.value}")
        }
        return InteractionResult.SUCCESS
    }

    companion object {
        const val NAME = "example_block"

        fun load() {
            println("Loading ExampleBlock...")
            Globals.blocks[NAME] = { props: Properties -> ExampleBlock(props) }
            Globals.itemInfos[NAME] = ItemInfo(tabs = listOf(
                TabReference.TabKey(CreativeModeTabsMixin.getBuildingBlocksTab())
                        to ItemReference.ItemKey(Items.OAK_BUTTON),
                TabReference.TabName("example_tab") to ItemReference.ItemName(ExampleItem.NAME)
            ))
        }
    }
}
