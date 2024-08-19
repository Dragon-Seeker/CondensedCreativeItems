package io.wispforest.condensed_creative.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.ItemGroupVariantHandler;
import io.wispforest.condensed_creative.ducks.CreativeInventoryScreenDuck;
import io.wispforest.condensed_creative.ducks.CreativeInventoryScreenHandlerDuck;
import io.wispforest.condensed_creative.entry.Entry;
import io.wispforest.condensed_creative.entry.EntryContainer;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.entry.impl.ItemEntry;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import io.wispforest.condensed_creative.util.CondensedInventory;
import io.wispforest.condensed_creative.util.ItemGroupHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements CreativeInventoryScreenDuck {

    @Shadow @Final @Mutable static SimpleContainer CONTAINER;

    @Shadow private static CreativeModeTab selectedTab;

    @Shadow private float scrollOffs;

    @Shadow protected abstract void selectTab(CreativeModeTab group);

    //-------------

    @Unique private static final ResourceLocation refreshButtonIconUnfocused = CondensedCreative.location("refresh_button_unfocused");
    @Unique private static final ResourceLocation refreshButtonIconFocused = CondensedCreative.location("refresh_button_focused");

    @Unique private boolean validItemGroupForCondensedEntries = false;

    //-------------

    public CreativeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }

    @Override
    public void cc$refreshCurrentTab() {
        selectTab(selectedTab);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void setupInventory(CallbackInfo ci){
        CONTAINER = new CondensedInventory(CONTAINER.getContainerSize());
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlotListener(Lnet/minecraft/world/inventory/ContainerListener;)V", shift = At.Shift.BY, by = 2))
    private void addButtonRender(CallbackInfo ci){
        if(!CondensedCreative.getConfig().entryRefreshButton) return;

        var widget = new ImageButton(this.leftPos + 200, this.topPos + 140, 16, 16, new WidgetSprites(refreshButtonIconUnfocused, refreshButtonIconFocused),
                button -> {
                    CondensedEntryRegistry.refreshEntrypoints();
                },
                CommonComponents.EMPTY
        );

        widget.setTooltip(Tooltip.create(Component.nullToEmpty("Refresh Condensed Entries")));

        this.addRenderableWidget(widget);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Inject(method = "getTooltipFromContainerItem", at = @At("HEAD"), cancellable = true)
    private void testToReplaceTooltipText(ItemStack stack, CallbackInfoReturnable<List<Component>> cir){
        var slot = ((HandledScreenAccessor)this).cc$getHoveredSlot();

        if(slot != null && slot.container instanceof EntryContainer inv
                && ItemEntry.areStacksEqual(slot.getItem(), stack)
                && inv.getEntryStack(slot.index) instanceof CondensedItemEntry entry && !entry.isChild) {

            List<Component> tooltipData = new ArrayList<>();

            entry.getParentTooltipText(tooltipData, this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);

            cir.setReturnValue(tooltipData);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Inject(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;clear()V", ordinal = 0))
    private void setSelectedTab$clearEntryList(CreativeModeTab group, CallbackInfo ci){
        this.getHandlerDuck().getDefaultEntryList().clear();

        this.validItemGroupForCondensedEntries = false;
    }

    @WrapOperation(
            method = "selectTab",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;add(Ljava/lang/Object;)Z"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;add(Ljava/lang/Object;)Z", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;add(Ljava/lang/Object;)Z", ordinal = 1)
            )
    )
    private boolean setSelectedTab$addStackToEntryList(NonNullList<ItemStack> instance, Object o, Operation<Boolean> original) {
        this.getHandlerDuck().addToDefaultEntryList((ItemStack) o);

        original.call(instance, o);

        return true;
    }

    @Inject(method = "selectTab", at = {
                @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z",ordinal = 0, shift = At.Shift.BY, by = 2),
                @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z", ordinal = 1, shift = At.Shift.BY, by = 1)})
    private void setSelectedTab$addStacksToEntryList(CreativeModeTab group, CallbackInfo ci){
        this.menu.items.forEach(stack -> getHandlerDuck().addToDefaultEntryList(stack));

        if(group != BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.HOTBAR())) this.validItemGroupForCondensedEntries = true;
    }

    //-------------

    @Inject(method = "refreshSearchResults", at = @At("HEAD"))
    private void search$clearEntryList(CallbackInfo ci){
        this.getHandlerDuck().getDefaultEntryList().clear();
    }

    @Inject(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;scrollTo(F)V", shift = At.Shift.BY, by = -2))
    private void search$addStacksToEntryList(CallbackInfo ci){
        this.menu.items.forEach(stack -> this.getHandlerDuck().addToDefaultEntryList(stack));
    }

    @Inject(method = {"selectTab", "refreshSearchResults"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;scrollTo(F)V"))
    private void scrollLineCountDefault(CallbackInfo ci){
        this.getHandlerDuck().markEntryListDirty();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Inject(method = "selectTab", at = @At(value = "JUMP", opcode = Opcodes.IF_ACMPNE, ordinal = 2))
    private void filterEntriesAndAddCondensedEntries(CreativeModeTab group, CallbackInfo ci){
        if (!validItemGroupForCondensedEntries) return;

        var handler = ItemGroupVariantHandler.getHandler(group);

        var possibleTabs = (handler != null && handler.isVariant(group))
                ? handler.getSelectedTabs(group)
                : Set.of(0);

        var helpers = possibleTabs.stream()
                .map(tab -> ItemGroupHelper.of(group, tab))
                .toArray(ItemGroupHelper[]::new);

        var defaultList = this.getHandlerDuck().getDefaultEntryList();

        var indexToEntry = new Int2ObjectAVLTreeMap<CondensedItemEntry>(Collections.reverseOrder());

        var parentEntries = CondensedEntryRegistry.getEntryList(helpers);

        for (var condensedItemEntry : parentEntries) {
            int i = defaultList.indexOf(Entry.of(condensedItemEntry.getEntryStack()));

            condensedItemEntry.initializeChildren(Minecraft.getInstance().level.registryAccess(), defaultList);

            if(condensedItemEntry.childrenEntry.isEmpty()) continue;

            if (i >= 0 && i < defaultList.size()) {
                defaultList.add(i, condensedItemEntry);
            } else {
                defaultList.add(condensedItemEntry);
            }
        }

        for (var condensedItemEntry : parentEntries) {
            indexToEntry.put(defaultList.indexOf(condensedItemEntry), condensedItemEntry);
        }

        indexToEntry.forEach((i, condensedItemEntry) -> {
            var childrenStartIndex = i + 1;
            var children = List.copyOf(condensedItemEntry.childrenEntry);

            if (childrenStartIndex >= 0 && childrenStartIndex < defaultList.size()) {
                defaultList.addAll(childrenStartIndex, children);
            } else {
                defaultList.addAll(children);
            }
        });
    }

    //----------

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void checkIfCondensedEntryWithinSlot(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci){
        if(slot != null && slot.container instanceof EntryContainer inv && inv.getEntryStack(slotId) instanceof CondensedItemEntry entry && !entry.isChild) {
            entry.toggleVisibility();

            this.getHandlerDuck().markEntryListDirty();

            this.menu.scrollTo(this.scrollOffs);

            var maxRows = ((CreativeScreenHandlerAccessor) this.menu).calculateRowCount();

            if(this.scrollOffs > maxRows) {
                this.scrollOffs = maxRows;

                this.menu.scrollTo(this.scrollOffs);
            }

            ci.cancel();
        }
    }

    //----------

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"), index = 2)
    private int changePosForScrollBar(int y){
        int j = this.topPos + 18;

        float scrollPosition = this.scrollOffs/* * ((CreativeScreenHandlerAccessor) this.handler).callGetOverflowRows()*/; // Float.isFinite()

        if(!Float.isFinite(scrollPosition)) scrollPosition = 0;

        return Math.round(j + (95f * scrollPosition));
    }

    //----------

    @Unique
    public CreativeInventoryScreenHandlerDuck getHandlerDuck(){
        return (CreativeInventoryScreenHandlerDuck) this.menu;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
    public static abstract class CreativeInventoryScreenHandlerMixin implements CreativeInventoryScreenHandlerDuck {

        @Shadow public abstract boolean canScroll();

        @Shadow protected abstract int getRowIndexForScroll(float scroll);

        //----------------------------------------

        @Unique private boolean isEntryListDirty = true;

        @Unique private NonNullList<Entry> defaultEntryList = NonNullList.create();
        @Unique private NonNullList<Entry> filteredEntryList = NonNullList.create();

        @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;scrollTo(F)V"))
        private void scrollLineCount(CreativeModeInventoryScreen.ItemPickerMenu instance, float position, Operation<Void> original){
            this.markEntryListDirty();

            original.call(instance, position);
        }

        @WrapOperation(method = "canScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
        private int redirectListSize(NonNullList instance, Operation<Integer> original){
            return this.filteredEntryList.size();
        }

        /**
         * @author Blodhgarm
         * @reason adjusted to use filteredEntryList instead
         */
        @Overwrite
        public void scrollTo(float position){
            checkAndUpdateIfListDirt();

            //---------------------------------------------

            int positionOffset = this.getRowIndexForScroll(position);

            for(int k = 0; k < 5; ++k) {
                for(int l = 0; l < 9; ++l) {
                    int m = l + ((k + positionOffset) * 9);

                    if (m >= 0 && m < this.filteredEntryList.size()) {
                        ((EntryContainer) CreativeModeInventoryScreenAccessor.CONTAINER()).setEntryStack(l + k * 9, this.filteredEntryList.get(m));
                    } else {
                        CreativeModeInventoryScreenAccessor.CONTAINER().setItem(l + k * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        @Unique
        private void checkAndUpdateIfListDirt() {
            if(!this.isEntryListDirty) return;

            Predicate<Entry> removeNonVisibleEntries = Entry::isVisible;

            this.filteredEntryList.clear();
            this.filteredEntryList.addAll(this.getDefaultEntryList().stream().filter(removeNonVisibleEntries).toList());

            this.isEntryListDirty = false;
        }

        /**
         * @author Blodhgarm
         * @reason adjusted to use filteredEntryList instead and prevent negative values
         */
        @Overwrite
        public int calculateRowCount() {
            return !this.filteredEntryList.isEmpty() && this.canScroll() ? Mth.ceil((this.filteredEntryList.size() / 9F) - 5F) : 0;
        }

        //----------


        @Override
        public NonNullList<Entry> getFilteredEntryList() {
            return this.filteredEntryList;
        }

        @Override
        public NonNullList<Entry> getDefaultEntryList() {
            return this.defaultEntryList;
        }

        @Override
        public void markEntryListDirty() {
            this.isEntryListDirty = true;
        }
    }
}
