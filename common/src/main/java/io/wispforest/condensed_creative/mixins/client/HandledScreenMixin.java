package io.wispforest.condensed_creative.mixins.client;

import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.util.CondensedInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {

    @ModifyVariable(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 0, shift = At.Shift.BY, by = 2))
    private ItemStack changeDisplayedStackIfParent(ItemStack stack, DrawContext matrices, Slot slot){
        if(slot.inventory instanceof CondensedInventory inv && inv.getEntryStack(slot.id) instanceof CondensedItemEntry entry && !entry.isChild){
            if (MinecraftClient.getInstance().world.getTime() - entry.lastTick > 40) {
                entry.getNextValue();
                entry.lastTick = MinecraftClient.getInstance().world.getTime();
            }

            return entry.getDisplayStack();
        }

        return stack;
    }
}
