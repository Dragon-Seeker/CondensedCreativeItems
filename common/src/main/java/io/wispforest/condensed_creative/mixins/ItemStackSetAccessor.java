package io.wispforest.condensed_creative.mixins;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStackLinkedSet.class)
public interface ItemStackSetAccessor {
    @Accessor("TYPE_AND_TAG") static Hash.Strategy<? super ItemStack> cc$getHashStrategy() { throw new UnsupportedOperationException(); }
}
