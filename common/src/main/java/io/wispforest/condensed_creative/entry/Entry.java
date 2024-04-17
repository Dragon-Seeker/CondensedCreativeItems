package io.wispforest.condensed_creative.entry;

import io.wispforest.condensed_creative.entry.impl.ItemEntry;
import net.minecraft.world.item.ItemStack;

public interface Entry {

    Entry EMPTY_ENTRY = new ItemEntry(ItemStack.EMPTY);

    //---------

    static Entry of(ItemStack stack){
        return new ItemEntry(stack);
    }

    ItemStack getEntryStack();

    ItemStack getDisplayStack();

    void toggleVisibility();

    boolean isVisible();

    default boolean isEmpty(){
        return this == EMPTY_ENTRY;
    }

    static boolean compareEntries(Entry mainEntry, Entry comparingEntry){
        return mainEntry.hashCode() == comparingEntry.hashCode();
    }
}
