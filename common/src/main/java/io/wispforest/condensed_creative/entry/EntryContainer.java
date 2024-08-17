package io.wispforest.condensed_creative.entry;

import net.minecraft.world.item.ItemStack;

public interface EntryContainer {

    Entry getEntryStack(int slot);

    void setEntryStack(int slot, Entry entryStack);
}
