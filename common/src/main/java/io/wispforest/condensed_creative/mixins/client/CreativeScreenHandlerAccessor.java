package io.wispforest.condensed_creative.mixins.client;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public interface CreativeScreenHandlerAccessor {
    @Invoker("calculateRowCount") int calculateRowCount();
}
