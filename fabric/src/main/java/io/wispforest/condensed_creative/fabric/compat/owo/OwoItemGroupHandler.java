package io.wispforest.condensed_creative.fabric.compat.owo;

import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.ItemGroupVariantHandler;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class OwoItemGroupHandler extends ItemGroupVariantHandler {

    @Override
    public boolean isVariant(ItemGroup group) {
        return group instanceof OwoItemGroup;
    }

    @Override
    public Collection<Integer> getSelectedTabs(ItemGroup group) {
        return (group instanceof OwoItemGroup owoItemGroup) ? owoItemGroup.selectedTabs() : IntSet.of(-1);
    }

    @Override
    public int getMaxTabs(ItemGroup group) {
        return (group instanceof OwoItemGroup owoItemGroup) ? owoItemGroup.tabs.size() : 1;
    }

    @Override
    public Identifier getIdentifier() {
        return CondensedCreative.createID("owo_item_group_handler");
    }
}
