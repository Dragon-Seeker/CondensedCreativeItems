package io.wispforest.condensed_creative.mixins.client;

import io.wispforest.condensed_creative.entry.EntryContainer;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> {

    @ModifyVariable(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;", ordinal = 0, shift = At.Shift.BY, by = 2))
    private ItemStack changeDisplayedStackIfParent(ItemStack stack, GuiGraphics matrices, Slot slot){
        if(slot.container instanceof EntryContainer inv && inv.getEntryStack(slot.index) instanceof CondensedItemEntry entry && !entry.isChild){
            if (Minecraft.getInstance().level.getGameTime() - entry.lastTick > 40) {
                entry.getNextValue();
                entry.lastTick = Minecraft.getInstance().level.getGameTime();
            }

            return entry.getDisplayStack();
        }

        return stack;
    }
}
