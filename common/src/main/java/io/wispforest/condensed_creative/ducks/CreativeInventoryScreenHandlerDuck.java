package io.wispforest.condensed_creative.ducks;

import io.wispforest.condensed_creative.entry.Entry;
import java.util.Collection;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface CreativeInventoryScreenHandlerDuck {

    void markEntryListDirty();

    NonNullList<Entry> getFilteredEntryList();

    NonNullList<Entry> getDefaultEntryList();

    default boolean addToDefaultEntryList(ItemStack stack) {
        return this.getDefaultEntryList().add(Entry.of(stack));
    }

    default boolean addToDefaultEntryList(Collection<ItemStack> stacks) {
        return this.getDefaultEntryList().addAll(stacks.stream().map(Entry::of).toList());
    }
}
