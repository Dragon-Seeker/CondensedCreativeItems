package io.wispforest.condensed_creative.registry;

import com.mojang.logging.LogUtils;
import io.wispforest.condensed_creative.LoaderSpecificUtils;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.util.ItemGroupHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Main Helper class to create CondensedItemEntries
 * 
 * Create {@link CondensedItemEntry} within your Client-Side Mod Initialization as this will only be seen for the User's side and not the server
 */
public final class CondensedEntryRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Register your A Entry's item group using {@link CondensedItemEntry.Builder#addToItemGroup(ItemGroup)}.
     *
     * <p>For owo ItemGroup with certain tabs use {@link CondensedItemEntry.Builder#addToItemGroup(ItemGroup, int)} to specify a certain tab index.</p>
     */
    public static final Map<ItemGroupHelper, List<CondensedItemEntry>> ENTRYPOINT_LOADED_ENTRIES = new HashMap<>();

    public static final Map<ItemGroupHelper, List<CondensedItemEntry>> RESOURCE_LOADED_ENTRIES = new HashMap<>();

    /**
     * Method to create A {@link CondensedItemEntry} using a Predicate
     * 
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemConvertible} being used to place the Entry within registered {@link ItemGroup}
     * @param predicte The {@link Predicate} used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder of(Identifier identifier, ItemConvertible itemConvertible, Predicate<Item> predicte){
        return of(identifier, itemConvertible.asItem().getDefaultStack(), predicte);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Predicate
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link ItemGroup}
     * @param predicte The predicate used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder of(Identifier identifier, ItemStack stack, Predicate<Item> predicte){
        return new CondensedItemEntry.Builder(identifier, stack, predicte, null);
    }

    //----------

    /**
     * Method to create A {@link CondensedItemEntry} using a Tag
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemConvertible} being used to place the Entry within registered {@link ItemGroup}
     * @param tagKey The {@link TagKey} used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static <T extends ItemConvertible> CondensedItemEntry.Builder fromTag(Identifier identifier, ItemConvertible itemConvertible, TagKey<T> tagKey){
        return fromTag(identifier, itemConvertible.asItem().getDefaultStack(), tagKey);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Tag
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link ItemGroup}
     * @param tagKey The {@link TagKey} used to find all the children items for the created {@link CondensedItemEntry}
     * @return The created {@link CondensedItemEntry}
     */
    public static <T extends ItemConvertible> CondensedItemEntry.Builder fromTag(Identifier identifier, ItemStack stack, TagKey<T> tagKey){
        return new CondensedItemEntry.Builder(identifier, stack, null, tagKey);
    }

    //----------

    /**
     * Use {@link #fromTag(Identifier, ItemConvertible, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromItemTag(Identifier identifier, ItemConvertible itemConvertible, TagKey<Item> itemTagKey){
        return fromItemTag(identifier, itemConvertible.asItem().getDefaultStack(), itemTagKey);
    }

    /**
     * Use {@link #fromTag(Identifier, ItemConvertible, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromItemTag(Identifier identifier, ItemStack stack, TagKey<Item> itemTagKey){
        return fromTag(identifier, stack, itemTagKey);
    }

    /**
     * Use {@link #fromTag(Identifier, ItemConvertible, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromBlockTag(Identifier identifier, ItemConvertible itemConvertible, TagKey<Block> blockTagKey){
        return fromBlockTag(identifier, itemConvertible.asItem().getDefaultStack(), blockTagKey);
    }

    /**
     * Use {@link #fromTag(Identifier, ItemConvertible, TagKey)}}
     */
    @Deprecated(forRemoval = true)
    public static CondensedItemEntry.Builder fromBlockTag(Identifier identifier, ItemStack stack, TagKey<Block> blockTagKey){
        return fromTag(identifier, stack, blockTagKey);
    }

    //----------

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemConvertible} being used to place the Entry within registered {@link ItemGroup}
     * @param collection The collection of {@link Item}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static <I extends ItemConvertible> CondensedItemEntry.Builder fromItems(Identifier identifier, ItemConvertible itemConvertible, Collection<I> collection){
        return fromItems(identifier, itemConvertible.asItem().getDefaultStack(), collection);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link ItemGroup}
     * @param collection The collection of {@link Item}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static <I extends ItemConvertible> CondensedItemEntry.Builder fromItems(Identifier identifier, ItemStack stack, Collection<I> collection){
        return fromItemStacks(identifier, stack, collection.stream()
                .map(ItemConvertible::asItem)
                .map(Item::getDefaultStack)
                .collect(Collectors.toList()));
    }

    //------------

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemConvertible} being used to place the Entry within registered {@link ItemGroup}
     * @param collection The collection of {@link ItemStack}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder fromItemStacks(Identifier identifier, ItemConvertible itemConvertible, Collection<ItemStack> collection){
        return fromItemStacks(identifier, itemConvertible.asItem().getDefaultStack(), collection);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using a Collection
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link ItemGroup}
     * @param collection The collection of {@link ItemStack}'s that will be used to build the children entries for the Entry
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder fromItemStacks(Identifier identifier, ItemStack stack, Collection<ItemStack> collection){
        return new CondensedItemEntry.Builder(identifier, stack, collection);
    }

    //-----------------------------------------------

    /**
     * Method to create A {@link CondensedItemEntry} using an ItemStack List Supplier
     *
     * @param identifier The Entries identifier
     * @param itemConvertible The {@link ItemConvertible} being used to place the Entry within registered {@link ItemGroup}
     * @param entryListSupplier A supplier for a collection of ItemStacks within a List
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder ofSupplier(Identifier identifier, ItemConvertible itemConvertible, Supplier<List<ItemStack>> entryListSupplier){
        return ofSupplier(identifier, itemConvertible.asItem().getDefaultStack(), entryListSupplier);
    }

    /**
     * Method to create A {@link CondensedItemEntry} using an ItemStack List Supplier
     *
     * @param identifier The Entries identifier
     * @param stack The {@link ItemStack} being used to place the Entry within the {@link ItemGroup}
     * @param entryListSupplier A supplier for a collection of ItemStacks within a List
     * @return The created {@link CondensedItemEntry}
     */
    public static CondensedItemEntry.Builder ofSupplier(Identifier identifier, ItemStack stack, Supplier<List<ItemStack>> entryListSupplier){
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
