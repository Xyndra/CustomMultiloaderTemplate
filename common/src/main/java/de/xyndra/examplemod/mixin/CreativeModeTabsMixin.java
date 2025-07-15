package de.xyndra.examplemod.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsMixin {
    @Accessor("BUILDING_BLOCKS")
    static ResourceKey<CreativeModeTab> getBuildingBlocksTab() {
        throw new AssertionError("Mixin failed to apply");
    }
}
