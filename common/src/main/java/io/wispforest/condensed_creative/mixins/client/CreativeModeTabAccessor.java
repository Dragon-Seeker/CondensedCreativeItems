package io.wispforest.condensed_creative.mixins.client;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Accessor("displayItemsGenerator") CreativeModeTab.DisplayItemsGenerator cc$getDisplayItemsGenerator();
}
