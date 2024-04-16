package io.wispforest.condensed_creative.ducks;

import io.wispforest.condensed_creative.entry.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface CreativeInventoryScreenHandlerDuck {

    void markEntryListDirty();

    DefaultedList<Entry> getDefaultEntryList();

    default void addToDefaultEntryList(ItemStack stack) {
        this.getDefaultEntryList().add(Entry.of(stack));
    }
}
