package io.wispforest.condensed_creative.entry.impl;

import com.mojang.logging.LogUtils;
import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.entry.Entry;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import io.wispforest.condensed_creative.util.ItemGroupHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CondensedItemEntry extends ItemEntry {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Map<ResourceLocation, Boolean> CHILD_VISIBILITY = new HashMap<>();

    //-------------------------------------------

    public final ResourceLocation condensedID;

    @Nullable private Supplier<List<ItemStack>> childrenSupplier = null;

    @Nullable private Supplier<Component> condensedEntryTitleBuilder = null;

    private Component condensedEntryTitle;

    private boolean compareToItem = false;

    private ItemGroupHelper itemGroupInfo;

    private EntryOrder listOrder = EntryOrder.DEFAULT_ORDER;

    private boolean removeIfNotFound = false;

    private ItemEntry chosenIconStack = null;

    //-------------------------------------------

    private @Nullable Component descriptionText = null;

    private @Nullable Consumer<List<ItemStack>> entrySorting = null;

    private @Nullable TagKey<? extends ItemLike> tagKey = null;

    //-------------------------------------------

    public final boolean isChild;

    public final List<CondensedItemEntry> childrenEntry = new ArrayList<>();

    private ItemEntry currentlyDisplayedEntry;

    //-------------------------------------------

    private CondensedItemEntry(ResourceLocation identifier, ItemStack defaultStack, boolean isChild) {
        super(defaultStack);

        this.condensedID = identifier;
        this.isChild = isChild;

        this.condensedEntryTitle = Component.nullToEmpty(Arrays.stream(identifier.getPath().split("_")).map(WordUtils::capitalize).collect(Collectors.joining(" ")));

        CHILD_VISIBILITY.put(identifier, false);
    }

    @ApiStatus.Internal
    private static CondensedItemEntry createChild(ResourceLocation condensedID, ItemStack defaultItemstack) {
        return new CondensedItemEntry(condensedID, defaultItemstack, true);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------

    public static class Builder {

        public final CondensedItemEntry currentEntry;

        public Builder(ResourceLocation condensedID, ItemStack defaultItemstack, Supplier<List<ItemStack>> entryListSupplier) {
            this.currentEntry = new CondensedItemEntry(condensedID, defaultItemstack, false);

            this.currentEntry.childrenSupplier = entryListSupplier;
        }

        public Builder(ResourceLocation condensedID, ItemStack defaultItemstack, Collection<ItemStack> entries) {
            this.currentEntry = new CondensedItemEntry(condensedID, defaultItemstack, false);

            this.currentEntry.childrenSupplier = () -> new ArrayList<>(entries);
        }

        public <T extends ItemLike> Builder(ResourceLocation condensedID, ItemStack defaultItemstack, @Nullable Predicate<Item> entryPredicate, @Nullable TagKey<T> tagKey) {
            this.currentEntry = new CondensedItemEntry(condensedID, defaultItemstack, false);

            if(entryPredicate != null) {
                this.currentEntry.childrenSupplier = () -> {
                    List<ItemStack> stacks = new ArrayList<>();

                    BuiltInRegistries.ITEM.forEach(item1 -> {
                        if (entryPredicate.test(item1)) stacks.add(item1.getDefaultInstance());
                    });

                    return stacks;
                };
            } else if(tagKey != null){
                this.currentEntry.tagKey = tagKey;
            } else {
                LOGGER.warn("[CondensedEntryBuilder]: Both the Predicate and the TagKey for a Condensed Entry was found to be null meaning it will be empty on creation: [Id: {}]", condensedID);
            }
        }

        private Builder(CondensedItemEntry entry){
            this.currentEntry = new CondensedItemEntry(entry.condensedID, entry.getEntryStack(), false);

            this.currentEntry.condensedEntryTitle = entry.condensedEntryTitle;
            this.currentEntry.compareToItem = entry.compareToItem;

            this.currentEntry.descriptionText = entry.descriptionText;
            this.currentEntry.childrenSupplier = entry.childrenSupplier;
            this.currentEntry.tagKey = entry.tagKey;

            this.currentEntry.entrySorting = entry.entrySorting;
            this.currentEntry.listOrder = entry.listOrder;

            this.currentEntry.removeIfNotFound = entry.removeIfNotFound;
        }

        /**
         * Toggles the comparison mode to be item :: item rather than the stacks themselves
         */
        public Builder useItemComparison(boolean value){
            this.currentEntry.compareToItem = value;

            return this;
        }

        /**
         * Sets the tooltip title text to be based of the given {@link Component} Supplier
         */
        public Builder setTitleSupplier(Supplier<Component> condensedEntryTitle){
            this.currentEntry.condensedEntryTitleBuilder = condensedEntryTitle;

            return this;
        }

        /**
         * Sets the tooltip title text to be based of the given {@link Component}
         */
        public Builder setTitle(Component condensedEntryTitle){
            this.currentEntry.condensedEntryTitle = condensedEntryTitle;

            return this;
        }

        /**
         * Sets the tooltip title text to be based of the already given Tag if such has been set
         */
        public Builder setTitleFromTag(){
            if(this.currentEntry.tagKey != null){
                this.currentEntry.condensedEntryTitle = Component.nullToEmpty(Arrays.stream(this.currentEntry.tagKey.location().getPath().split("_")).map(WordUtils::capitalize).collect(Collectors.joining(" ")));
            }

            return this;
        }

        /**
         * Adds description onto the Entries tooltip
         */
        public Builder setDescription(Component description){
            this.currentEntry.descriptionText = description;

            return this;
        }

        /**
         * Used to filter the children list during creation
         */
        public Builder setEntrySorting(Consumer<List<ItemStack>> sortFunc){
            this.currentEntry.entrySorting = sortFunc;
            this.currentEntry.listOrder = EntryOrder.CUSTOM_ORDER;

            return this;
        }

        /**
         * Used Change the Children's Order (See {@link EntryOrder})
         */
        public Builder setEntryOrder(EntryOrder entryOrder){
            this.currentEntry.listOrder = entryOrder;

            return this;
        }

        /**
         * Toggle whether to filter out any Children Entries not found within the Entries Item Group
         */
        public Builder toggleStrictFiltering(boolean value){
            this.currentEntry.removeIfNotFound = value;

            return this;
        }

        /**
         * Used to add the {@link CondensedItemEntry} to a certain {@link CreativeModeTab}
         */
        public CondensedItemEntry addToItemGroup(CreativeModeTab itemGroup){
            return addToItemGroup(itemGroup, -1);
        }

        public CondensedItemEntry addToItemGroup(ResourceKey<CreativeModeTab> itemGroup){
            return addToItemGroup(itemGroup, -1);
        }

        /**
         * Used to add the {@link CondensedItemEntry} to a certain {@link CreativeModeTab} with tab index support if using a Supported ItemGroup Variant with tabs
         */
        public CondensedItemEntry addToItemGroup(CreativeModeTab itemGroup, int tabIndex){
            return addToItemGroup(ItemGroupHelper.of(itemGroup, tabIndex), true);
        }

        public CondensedItemEntry addToItemGroup(ResourceKey<CreativeModeTab> itemGroup, int tabIndex){
            return addToItemGroup(ItemGroupHelper.of(itemGroup, tabIndex), true);
        }

        public List<CondensedItemEntry> addToItemGroups(CreativeModeTab ...groups){
            return this.addToItemGroups(true, Arrays.stream(groups).map(group -> ItemGroupHelper.of(group, 0)).toArray(ItemGroupHelper[]::new));
        }

        @SafeVarargs
        public final List<CondensedItemEntry> addToItemGroups(ResourceKey<CreativeModeTab>... groups){
            return this.addToItemGroups(true, Arrays.stream(groups).map(group -> ItemGroupHelper.of(group, 0)).toArray(ItemGroupHelper[]::new));
        }

        public List<CondensedItemEntry> addToItemGroups(boolean addToMainEntriesMap, ItemGroupHelper... helpers){
            List<CondensedItemEntry> condensedItemEntries = new ArrayList<>();

            Builder builder = this;

            for(ItemGroupHelper helper : helpers){
                condensedItemEntries.add(builder.addToItemGroup(helper, addToMainEntriesMap));

                builder = builder.copy();
            }

            return condensedItemEntries;
        }

        //---------------------------------------------------------------------------

        @ApiStatus.Internal
        public CondensedItemEntry addToItemGroup(ItemGroupHelper helper, boolean addToMainEntriesMap){
            this.currentEntry.itemGroupInfo = helper;

            if(addToMainEntriesMap) {
                CondensedEntryRegistry.addCondensedEntryToRegistryMap(this.currentEntry, CondensedEntryRegistry.ENTRYPOINT_LOADED_ENTRIES);
            }

            return this.currentEntry;
        }

        public Builder copy(){
            return new Builder(this.currentEntry);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------

    @ApiStatus.Internal
    public void initializeChildren(List<Entry> itemGroupList){
        if(this.childrenEntry.isEmpty()) {
            List<ItemStack> childrenStacks = this.getChildren();

            List<ItemStack> replacedStacks = filterItemGroupStacks(itemGroupList, childrenStacks.stream().map(ItemEntry::hashcodeOfStack).toList(), true);

            if (removeIfNotFound) {
                List<ItemStack> childrenStacksFiltered = new ArrayList<>();

                for (int i = 0; i < childrenStacks.size(); i++) {
                    ItemStack childrenStack = childrenStacks.get(i);

                    boolean bl = replacedStacks
                            .stream()
                            .anyMatch(stack -> ItemEntry.hashcodeOfStack(childrenStack) == ItemEntry.hashcodeOfStack(stack));

                    if (bl) childrenStacksFiltered.add(childrenStack);
                }

                if(childrenStacks.size() > replacedStacks.size()) childrenStacks = childrenStacksFiltered;
            }

            if(!childrenStacks.isEmpty()) {
                this.listOrder.sortList(replacedStacks, childrenStacks, this.entrySorting != null ? this.entrySorting : (l) -> {});

                this.childrenEntry.addAll(
                        childrenStacks
                                .stream()
                                .map(stack -> createChild(this.condensedID, stack))
                                .toList()
                );
            }
        } else {
            filterItemGroupStacks(itemGroupList, this.childrenEntry.stream().map(CondensedItemEntry::getItemEntryHashCode).toList(), false);
        }

        if(!this.childrenEntry.isEmpty()) getNextValue();
    }

    public List<ItemStack> getChildren(){
        List<ItemStack> stacks = new ArrayList<>();

        if(childrenSupplier != null) return childrenSupplier.get();

        if(tagKey != null) {
            BuiltInRegistries.ITEM.forEach(item1 -> { if (withinEntriesTag(item1)) stacks.add(item1.getDefaultInstance()); });
        }

        return stacks;
    }

    private <T extends ItemLike> boolean withinEntriesTag(Item item) {
        if(tagKey == null) return false;

        if(tagKey.isFor(Registries.ITEM)){
            return item.builtInRegistryHolder().is((TagKey<Item>) tagKey);
        } else if(tagKey.isFor(Registries.BLOCK)) {
            return item instanceof BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().is((TagKey<Block>) tagKey);
        } else {
            LOGGER.warn("[CondensedEntryRegistry]: It seems that a Condensed Entry was somehow registered with Tag that isn't part of the Item or Block Registry: [Id: {}]", this.condensedID);
        }

        return false;
    }

    public List<ItemStack> filterItemGroupStacks(List<Entry> itemGroupList, List<Integer> hashValues, boolean returnStacks){
        List<ItemStack> replacedStacks = new ArrayList<>();

        itemGroupList.removeIf(entry -> {
            if(!(entry instanceof CondensedItemEntry) && hashValues.contains(entry.hashCode())){
                if(returnStacks) replacedStacks.add(entry.getEntryStack());

                return true;
            }

            return false;
        });

        return replacedStacks;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------

    public long lastTick = 0;

    public void getNextValue(){
        if(chosenIconStack == null) {
            int index = (CondensedCreative.getConfig().rotationPreview)
                    ? new Random().nextInt(0, childrenEntry.size())
                    : 0;

            if(index < 0 || index > childrenEntry.size()){
                currentlyDisplayedEntry = ItemEntry.EMPTY;

                LOGGER.error("[CondensedItemEntry]: It seems that a random number generated for the given CondensedEntry[{}] seems to have been out of the valid bounds!", this.condensedID);
            } else {
                currentlyDisplayedEntry = childrenEntry.get(index);
            }
        } else if(!currentlyDisplayedEntry.equals(chosenIconStack)){
            currentlyDisplayedEntry = chosenIconStack;
        }
    }

    public ItemGroupHelper getItemGroupInfo() {
        return itemGroupInfo;
    }

    @Override
    public ItemStack getDisplayStack() {
        return isChild ? this.getEntryStack() : this.currentlyDisplayedEntry.getEntryStack();
    }

    @Override
    public boolean isVisible() {
        return isChild ? CHILD_VISIBILITY.get(this.condensedID) : super.isVisible();
    }

    @Override
    public void toggleVisibility() {
        if(!isChild) CHILD_VISIBILITY.replace(this.condensedID, !CHILD_VISIBILITY.get(this.condensedID));
    }

    public void getParentTooltipText(List<Component> tooltipData, Player player, TooltipFlag context) {
        if(condensedEntryTitleBuilder != null){
            condensedEntryTitle = this.condensedEntryTitleBuilder.get();

            condensedEntryTitleBuilder = null;
        }

        tooltipData.add(condensedEntryTitle);

        if(descriptionText != null) {
            tooltipData.add(descriptionText);
        }

        if(tagKey != null && CondensedCreative.getConfig().tagPreviewForEntries){
            tooltipData.add(Component.nullToEmpty("Tag: #" + tagKey.location().toString()).copy().withStyle(ChatFormatting.GRAY));
        }

        if(CondensedCreative.getConfig().debugIdentifiersForEntries) {
            tooltipData.add(Component.nullToEmpty(""));

            tooltipData.add(Component.nullToEmpty("EntryID: " + condensedID.toString()).copy().withStyle(ChatFormatting.GRAY));
        }
    }

    @Nullable
    public TagKey<?> getTagKey(){
        return this.tagKey;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------

    public int getItemEntryHashCode(){
        return super.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();

        hash = hash * 31 + this.condensedID.hashCode();

        if(!this.isChild) hash = hash * 31 + this.childrenEntry.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this.compareToItem && obj instanceof Entry entry && !(entry instanceof CondensedItemEntry)){
            return this.getEntryStack().getItem() == entry.getEntryStack().getItem();
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return isChild ? super.toString() : ( "ParentEntry: {Id: " + this.condensedID + "}");
    }

    public enum EntryOrder implements SortingAction {
        DEFAULT_ORDER((itemGroupStacks, toBeChildren, customFilter) -> {}),
        ITEMGROUP_ORDER((itemGroupStacks, toBeChildren, customFilter) -> {
            List<ItemStack> newListOrder = new ArrayList<>();

            for(int i = 0; i < itemGroupStacks.size(); i++){
                var currentItemGroupStack = itemGroupStacks.get(i);
                var foundChildStack = new MutableObject<>(ItemStack.EMPTY);

                toBeChildren.removeIf(stack -> {
                    if(ItemEntry.hashcodeOfStack(currentItemGroupStack) == ItemEntry.hashcodeOfStack(stack)){
                        foundChildStack.setValue(stack);

                        return true;
                    }

                    return false;
                });

                if(foundChildStack.getValue() != ItemStack.EMPTY) newListOrder.add(foundChildStack.getValue());
            }

            if(!toBeChildren.isEmpty()) newListOrder.addAll(toBeChildren);

            toBeChildren.clear();

            toBeChildren.addAll(newListOrder);
        }),
        CUSTOM_ORDER((itemGroupStacks, toBeChildren, customFilter) -> customFilter.accept(toBeChildren));

        public SortingAction action;

        EntryOrder(SortingAction action){
            this.action = action;
        }

        @Override
        public void sortList(List<ItemStack> itemGroupStacks, List<ItemStack> toBeChildren, Consumer<List<ItemStack>> customFilter) {
            this.action.sortList(itemGroupStacks, toBeChildren, customFilter);
        }
    }

    public interface SortingAction {
        void sortList(List<ItemStack> itemGroupStacks, List<ItemStack> toBeChildren, Consumer<List<ItemStack>> customFilter);
    }
}
