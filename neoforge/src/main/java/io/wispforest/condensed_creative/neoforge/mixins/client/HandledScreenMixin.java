package io.wispforest.condensed_creative.neoforge.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.condensed_creative.client.SlotRenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> {

    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotContents(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Ljava/lang/String;)V"))
    private void renderExtraIfEntry(AbstractContainerScreen instance, GuiGraphics guiGraphics, ItemStack stack, Slot slot, String string, Operation<Void> original){
        original.call(instance, guiGraphics, stack, slot, string);
        SlotRenderUtils.renderExtraIfEntry((AbstractContainerScreen) (Object) this, guiGraphics, slot);
    }
}
