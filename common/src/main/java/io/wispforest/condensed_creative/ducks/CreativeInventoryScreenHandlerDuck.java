package io.wispforest.condensed_creative.ducks;

import io.wispforest.condensed_creative.entry.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Collection;

public interface CreativeInventoryScreenHandlerDuck {

    void markEntryListDirty();

    DefaultedList<Entry> getDefaultEntryList();

    default boolean addToDefaultEntryList(ItemStack stack) {
        return this.getDefaultEntryList().add(Entry.of(stack));
    }

    default boolean addToDefaultEntryList(Collection<ItemStack> stacks) {
        return this.getDefaultEntryList().addAll(stacks.stream().map(Entry::of).toList());
    }
}
