package io.wispforest.condensed_creative.mixins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStackLinkedSet.class)
public interface ItemStackSetAccessor {
    @Invoker("hashStackAndTag") static int cc$getHashCode(@Nullable ItemStack stack) { throw new UnsupportedOperationException(); }
}
