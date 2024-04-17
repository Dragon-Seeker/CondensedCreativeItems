package io.wispforest.condensed_creative.neoforge.mixins.client;

import io.wispforest.condensed_creative.client.SlotRenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderSlotContents(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;IILjava/lang/String;)V", shift = At.Shift.BY, by = 1))
    private void renderExtraIfEntry(DrawContext context, Slot slot, CallbackInfo ci){
        SlotRenderUtils.renderExtraIfEntry((HandledScreen) (Object) this, context, slot);
    }
}
