package io.wispforest.condensed_creative.registry;

import com.mojang.logging.LogUtils;
import io.wispforest.condensed_creative.LoaderSpecificUtils;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.util.ItemGroupHelper;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Main Helper class to create CondensedItemEntries
 * 
 * Create {@link CondensedItemEntry} within your Client-Side Mod Initialization as this will only be seen for the User's side and not the server
 */
public final class CondensedEntryRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Register your A Entry's item group using {@link CondensedItemEntry.Builder#addToItemGroup(CreativeModeTab)}.
     *
     * <p>For owo ItemGroup with certain tabs use {@link CondensedItemEntry.Builder#addToItemGroup(CreativeModeTab, int)} to specify a certain tab index.</p>
     */
    public static final Map<ItemGroupHelper, List<CondensedItemEntry>> ENTRYPOINT_LOADED_ENTRIES = new HashMap<>();

    public static final Map<ItemGroupHelper, List<CondensedItemEntry>> RESOURCE_LOADED_ENTRIES = new HashMap<>();

    /**
     * Method to create A {@link CondensedItemEntry} using a Predicate
     * 
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemLike} being used to place the Entry within registered {@link CreativeModeTab}
     * @param predicte The {@link Predicate} used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder of(ResourceLocation identifier, ItemLike itemConvertible, Predicate<Item> predicte){
        return of(identifier, itemConvertible.asItem().getDefaultInstance(), predicte);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Predicate
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link CreativeModeTab}
     * @param predicte The predicate used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder of(ResourceLocation identifier, ItemStack stack, Predicate<Item> predicte){
        return new CondensedItemEntry.Builder(identifier, stack, predicte, null);
    }

    //----------

    /**
     * Method to create A {@link CondensedItemEntry} using a Tag
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemLike} being used to place the Entry within registered {@link CreativeModeTab}
     * @param tagKey The {@link TagKey} used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static <T extends ItemLike> CondensedItemEntry.Builder fromTag(ResourceLocation identifier, ItemLike itemConvertible, TagKey<T> tagKey){
        return fromTag(identifier, itemConvertible.asItem().getDefaultInstance(), tagKey);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Tag
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link CreativeModeTab}
     * @param tagKey The {@link TagKey} used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static <T extends ItemLike> CondensedItemEntry.Builder fromTag(ResourceLocation identifier, ItemStack stack, TagKey<T> tagKey){
        return new CondensedItemEntry.Builder(identifier, stack, null, tagKey);
    }

    //----------

    /**
     * Use {@link #fromTag(ResourceLocation, ItemLike, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromItemTag(ResourceLocation identifier, ItemLike itemConvertible, TagKey<Item> itemTagKey){
        return fromItemTag(identifier, itemConvertible.asItem().getDefaultInstance(), itemTagKey);
    }

    /**
     * Use {@link #fromTag(ResourceLocation, ItemLike, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromItemTag(ResourceLocation identifier, ItemStack stack, TagKey<Item> itemTagKey){
        return fromTag(identifier, stack, itemTagKey);
    }

    /**
     * Use {@link #fromTag(ResourceLocation, ItemLike, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromBlockTag(ResourceLocation identifier, ItemLike itemConvertible, TagKey<Block> blockTagKey){
        return fromBlockTag(identifier, itemConvertible.asItem().getDefaultInstance(), blockTagKey);
    }

    /**
     * Use {@link #fromTag(ResourceLocation, ItemLike, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromBlockTag(ResourceLocation identifier, ItemStack stack, TagKey<Block> blockTagKey){
        return fromTag(identifier, stack, blockTagKey);
    }

    //----------

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemLike} being used to place the Entry within registered {@link CreativeModeTab}
     * @param collection The collection of {@link Item}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static <I extends ItemLike> CondensedItemEntry.Builder fromItems(ResourceLocation identifier, ItemLike itemConvertible, Collection<I> collection){
        return fromItems(identifier, itemConvertible.asItem().getDefaultInstance(), collection);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link CreativeModeTab}
     * @param collection The collection of {@link Item}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static <I extends ItemLike> CondensedItemEntry.Builder fromItems(ResourceLocation identifier, ItemStack stack, Collection<I> collection){
        return fromItemStacks(identifier, stack, collection.stream()
                .map(ItemLike::asItem)
                .map(Item::getDefaultInstance)
                .collect(Collectors.toList()));
    }

    //------------

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemLike} being used to place the Entry within registered {@link CreativeModeTab}
     * @param collection The collection of {@link ItemStack}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder fromItemStacks(ResourceLocation identifier, ItemLike itemConvertible, Collection<ItemStack> collection){
        return fromItemStacks(identifier, itemConvertible.asItem().getDefaultInstance(), collection);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link CreativeModeTab}
     * @param collection The collection of {@link ItemStack}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder fromItemStacks(ResourceLocation identifier, ItemStack stack, Collection<ItemStack> collection){
        return new CondensedItemEntry.Builder(identifier, stack, collection);
    }

    //-----------------------------------------------

    /**
     * Method to create A {@link CondensedItemEntry} using an ItemStack List Supplier
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemLike} being used to place the Entry within registered {@link CreativeModeTab}
     * @param entryListSupplier A supplier for a collection of ItemStacks within a List
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder ofSupplier(ResourceLocation identifier, ItemLike itemConvertible, Supplier<List<ItemStack>> entryListSupplier){
        return ofSupplier(identifier, itemConvertible.asItem().getDefaultInstance(), entryListSupplier);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using an ItemStack List Supplier
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link CreativeModeTab}
     * @param entryListSupplier A supplier for a collection of ItemStacks within a List
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder ofSupplier(ResourceLocation identifier, ItemStack stack, Supplier<List<ItemStack>> entryListSupplier){
        return new CondensedItemEntry.Builder(identifier, stack, entryListSupplier);
    }

    //-----------------------------------------------

    public static List<CondensedItemEntry> getEntryList(ItemGroupHelper... itemGroupHelpers){
        List<CondensedItemEntry> entries = new ArrayList<>();

        for (ItemGroupHelper itemGroupHelper : itemGroupHelpers) {
            if(ENTRYPOINT_LOADED_ENTRIES.containsKey(itemGroupHelper)){
                entries.addAll(ENTRYPOINT_LOADED_ENTRIES.get(itemGroupHelper));
            }

            if(RESOURCE_LOADED_ENTRIES.containsKey(itemGroupHelper)) {
                entries.addAll(RESOURCE_LOADED_ENTRIES.get(itemGroupHelper));
            }
        }

        return entries;
    }

    @ApiStatus.Internal
    @ApiStatus.Experimental
    public static void addCondensedEntryToRegistryMap(CondensedItemEntry condensedItemEntry, Map<ItemGroupHelper, List<CondensedItemEntry>> entriesMap){
        if(condensedItemEntry.getItemGroupInfo() != null) {
            if (entriesMap.containsKey(condensedItemEntry.getItemGroupInfo())) {
                entriesMap.get(condensedItemEntry.getItemGroupInfo()).add(condensedItemEntry);
            } else {
                ArrayList<CondensedItemEntry> list = new ArrayList<>();
                list.add(condensedItemEntry);

                entriesMap.put(condensedItemEntry.getItemGroupInfo(), list);
            }
        }
    }

    @ApiStatus.Internal
    @ApiStatus.Experimental
    public static void removeCondensedEntryToMainList(CondensedItemEntry condensedItemEntry){
        if(condensedItemEntry.getItemGroupInfo() != null) {
            if (ENTRYPOINT_LOADED_ENTRIES.containsKey(condensedItemEntry.getItemGroupInfo())) {
                ENTRYPOINT_LOADED_ENTRIES.get(condensedItemEntry.getItemGroupInfo()).remove(condensedItemEntry);
            }
        }
    }

    @ApiStatus.Internal
    public static boolean refreshEntrypoints(){
        int previousSize = 0;
        int currentSize = 0;

        for(Map.Entry<ItemGroupHelper, List<CondensedItemEntry>> entry : CondensedEntryRegistry.ENTRYPOINT_LOADED_ENTRIES.entrySet()){
            previousSize += entry.getValue().size();
        }

        ENTRYPOINT_LOADED_ENTRIES.clear();

        for(CondensedCreativeInitializer initializer : LoaderSpecificUtils.getEntryPoints()){
            initializer.registerCondensedEntries(true);
        }

        for(Map.Entry<ItemGroupHelper, List<CondensedItemEntry>> entry : CondensedEntryRegistry.ENTRYPOINT_LOADED_ENTRIES.entrySet()){
            currentSize += entry.getValue().size();
        }

        return previousSize != currentSize;
    }
}
