package io.wispforest.condensed_creative.util;

import io.wispforest.condensed_creative.compat.ItemGroupVariantHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public record ItemGroupHelper(CreativeModeTab group, int tab) {

    public static ItemGroupHelper of(ResourceKey<CreativeModeTab> groupKey, int tab) {
        CreativeModeTab group = BuiltInRegistries.CREATIVE_MODE_TAB.get(groupKey);

        if(group == null) throw new NullPointerException("A ItemGroup helper was attempted to be created with a RegistryKey that was not found within the ItemGroup Registry! [Key: " + groupKey.toString() + "]");

        return of(group, tab);
    }

    public static ItemGroupHelper of(CreativeModeTab group, int tab) {
        var handler = ItemGroupVariantHandler.getHandler(group);

        return new ItemGroupHelper(group, handler != null && handler.isVariant(group) ? Math.max(tab, 0) : 0);
    }
}
