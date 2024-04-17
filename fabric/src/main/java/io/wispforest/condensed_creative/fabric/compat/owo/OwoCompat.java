package io.wispforest.condensed_creative.fabric.compat.owo;

import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.ItemGroupVariantHandler;
import io.wispforest.condensed_creative.fabric.CondensedCreativeFabric;
import io.wispforest.condensed_creative.mixins.client.CreativeModeTabAccessor;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class OwoCompat {

    public static void init(){
        ItemGroupVariantHandler.register(new OwoItemGroupHandler());

        if (CondensedCreative.isDeveloperMode()) {
            CondensedCreativeFabric.createOwoItemGroup = () -> {
                OwoItemGroup owoItemGroup = OwoItemGroup.builder(CondensedCreative.createID("test"), () -> Icon.of(Blocks.BEDROCK.asItem().getDefaultInstance()))
                        .initializer(group -> {
                            Function<ResourceKey<CreativeModeTab>, CreativeModeTab> func = BuiltInRegistries.CREATIVE_MODE_TAB::get;

                            addTabToList(group.tabs, group, Icon.of(Blocks.BRICKS), "building_blocks", true, (enabledFeatures, entries) -> {
                                ((CreativeModeTabAccessor) func.apply(CreativeModeTabs.BUILDING_BLOCKS))
                                        .cc$getDisplayItemsGenerator()
                                        .accept(enabledFeatures, entries);
                            });
                            addTabToList(group.tabs, group, Icon.of(Blocks.PEONY), "colored_blocks", false, (enabledFeatures, entries) -> {
                                ((CreativeModeTabAccessor) func.apply(CreativeModeTabs.COLORED_BLOCKS))
                                        .cc$getDisplayItemsGenerator()
                                        .accept(enabledFeatures, entries);
                            });
                            addTabToList(group.tabs, group, Icon.of(Items.IRON_INGOT), "ingredients", false, (enabledFeatures, entries) -> {
                                ((CreativeModeTabAccessor) func.apply(CreativeModeTabs.INGREDIENTS))
                                        .cc$getDisplayItemsGenerator()
                                        .accept(enabledFeatures, entries);
                            });
                        }).build();

                owoItemGroup.initialize();

                return owoItemGroup;
            };
        }
    }

    public static void addTabToList(List<ItemGroupTab> tabs, OwoItemGroup group, Icon icon, String name, boolean primary, ItemGroupTab.ContentSupplier contentSupplier){
        tabs.add(new ItemGroupTab(
                icon,
                OwoItemGroup.ButtonDefinition.tooltipFor(group, "tab", name),
                contentSupplier,
                ItemGroupTab.DEFAULT_TEXTURE,
                primary
        ));
    }
}
