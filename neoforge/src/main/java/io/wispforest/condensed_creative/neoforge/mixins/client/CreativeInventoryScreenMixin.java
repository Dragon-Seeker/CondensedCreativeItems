package io.wispforest.condensed_creative.neoforge.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.ItemGroupVariantHandler;
import io.wispforest.condensed_creative.ducks.CreativeInventoryScreenHandlerDuck;
import io.wispforest.condensed_creative.entry.Entry;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.entry.impl.ItemEntry;
import io.wispforest.condensed_creative.mixins.CreativeScreenHandlerAccessor;
import io.wispforest.condensed_creative.mixins.client.HandledScreenAccessor;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import io.wispforest.condensed_creative.util.CondensedInventory;
import io.wispforest.condensed_creative.util.ItemGroupHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {

    @Shadow @Final @Mutable public static SimpleInventory INVENTORY;

    @Shadow private static ItemGroup selectedTab;

    @Shadow private float scrollPosition;

    @Shadow protected abstract void setSelectedTab(ItemGroup group);

    //-------------

    @Unique private static final Identifier refreshButtonIconUnfocused = CondensedCreative.createID("refresh_button_unfocused");
    @Unique private static final Identifier refreshButtonIconFocused = CondensedCreative.createID("refresh_button_focused");

    @Unique private boolean validItemGroupForCondensedEntries = false;

    //-------------

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void setupInventory(CallbackInfo ci){
        INVENTORY = new CondensedInventory(INVENTORY.size());
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;addListener(Lnet/minecraft/screen/ScreenHandlerListener;)V", shift = At.Shift.BY, by = 2))
    private void addButtonRender(CallbackInfo ci){
        if(!CondensedCreative.getConfig().entryRefreshButton) return;

        var widget = new TexturedButtonWidget(this.x + 200, this.y + 140, 16, 16, new ButtonTextures(refreshButtonIconUnfocused, refreshButtonIconFocused),
                button -> {
                    CondensedEntryRegistry.refreshEntrypoints();
                    setSelectedTab(this.selectedTab);
                },
                ScreenTexts.EMPTY
        );

        widget.setTooltip(Tooltip.of(Text.of("Refresh Condensed Entries")));

        this.addDrawableChild(widget);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Inject(method = "getTooltipFromItem", at = @At("HEAD"), cancellable = true)
    private void testToReplaceTooltipText(ItemStack stack, CallbackInfoReturnable<List<Text>> cir){
        var slot = ((HandledScreenAccessor)this).cc$getFocusedSlot();

        if(slot != null && slot.inventory instanceof CondensedInventory inv
                && ItemEntry.areStacksEqual(slot.getStack(), stack)
                && inv.getEntryStack(slot.id) instanceof CondensedItemEntry entry && !entry.isChild) {

            List<Text> tooltipData = new ArrayList<>();

            entry.getParentTooltipText(tooltipData, this.client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);

            cir.setReturnValue(tooltipData);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Inject(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;clear()V", ordinal = 0))
    private void setSelectedTab$clearEntryList(ItemGroup group, CallbackInfo ci){
        this.getHandlerDuck().getDefaultEntryList().clear();

        this.validItemGroupForCondensedEntries = false;
    }

    @Redirect(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;add(Ljava/lang/Object;)Z"))
    private boolean setSelectedTab$addStackToEntryList(DefaultedList instance, Object o) {
        this.getHandlerDuck().addToDefaultEntryList((ItemStack) o);

        return true;
    }
//
//    @Inject(method = "setSelectedTab", at = {
//                @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BY, by = 2),
//                @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;add(Ljava/lang/Object;)Z", ordinal = 1, shift = At.Shift.BY, by = 2)})
//    private void setSelectedTab$addStackToEntryList(ItemGroup group, CallbackInfo ci){
//        this.getHandlerDuck().addToDefaultEntryList(this.handler.itemList.get(this.handler.itemList.size() - 1));
//    }

    @Inject(method = "setSelectedTab", at = {
                @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;addAll(Ljava/util/Collection;)Z",ordinal = 0, shift = At.Shift.BY, by = 2),
                @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;addAll(Ljava/util/Collection;)Z", ordinal = 1, shift = At.Shift.BY, by = 1)})
    private void setSelectedTab$addStacksToEntryList(ItemGroup group, CallbackInfo ci){
        this.handler.itemList.forEach(stack -> getHandlerDuck().addToDefaultEntryList(stack));

        if(group != Registries.ITEM_GROUP.get(ItemGroups.HOTBAR)) this.validItemGroupForCondensedEntries = true;
    }

    //-------------

    @Inject(method = "search", at = @At("HEAD"))
    private void search$clearEntryList(CallbackInfo ci){
        this.getHandlerDuck().getDefaultEntryList().clear();
    }

    @Inject(method = "search", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeScreenHandler;scrollItems(F)V", shift = At.Shift.BY, by = -2))
    private void search$addStacksToEntryList(CallbackInfo ci){
        this.handler.itemList.forEach(stack -> this.getHandlerDuck().addToDefaultEntryList(stack));
    }

    @Inject(method = {"setSelectedTab", "search"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeScreenHandler;scrollItems(F)V"))
    private void scrollLineCountDefault(CallbackInfo ci){
        this.getHandlerDuck().markEntryListDirty();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Inject(method = "setSelectedTab", at = @At(value = "JUMP", opcode = Opcodes.IF_ACMPNE, ordinal = 2))
    private void filterEntriesAndAddCondensedEntries(ItemGroup group, CallbackInfo ci){
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

            condensedItemEntry.initializeChildren(defaultList);

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

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    private void checkIfCondensedEntryWithinSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci){
        if(slot != null && slot.inventory instanceof CondensedInventory inv && inv.getEntryStack(slotId) instanceof CondensedItemEntry entry && !entry.isChild) {
            entry.toggleVisibility();

            this.getHandlerDuck().markEntryListDirty();

            this.handler.scrollItems(this.scrollPosition);

            var maxRows = ((CreativeScreenHandlerAccessor) this.handler).callGetOverflowRows();

            if(this.scrollPosition > maxRows) {
                this.scrollPosition = maxRows;

                this.handler.scrollItems(this.scrollPosition);
            }

            ci.cancel();
        }
    }

    //----------

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"), index = 2)
    private int changePosForScrollBar(int y){
        int j = this.y + 18;

        float scrollPosition = this.scrollPosition/* * ((CreativeScreenHandlerAccessor) this.handler).callGetOverflowRows()*/; // Float.isFinite()

        if(!Float.isFinite(scrollPosition)) scrollPosition = 0;

        return Math.round(j + (95f * scrollPosition));
    }

    //----------

    @Unique
    public CreativeInventoryScreenHandlerDuck getHandlerDuck(){
        return (CreativeInventoryScreenHandlerDuck) this.handler;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Mixin(CreativeInventoryScreen.CreativeScreenHandler.class)
    public static abstract class CreativeInventoryScreenHandlerMixin implements CreativeInventoryScreenHandlerDuck {

        @Shadow public abstract boolean shouldShowScrollbar();

        @Shadow protected abstract int getRow(float scroll);

        //----------------------------------------

        @Unique private boolean isEntryListDirty = true;

        @Unique private DefaultedList<Entry> defaultEntryList = DefaultedList.of();
        @Unique private DefaultedList<Entry> filteredEntryList = DefaultedList.of();

        @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeScreenHandler;scrollItems(F)V"))
        private void scrollLineCount(CreativeInventoryScreen.CreativeScreenHandler instance, float position, Operation<Void> original){
            this.markEntryListDirty();

            original.call(instance, position);
        }

        @Redirect(method = "shouldShowScrollbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;size()I"))
        private int redirectListSize(DefaultedList instance){
            return this.filteredEntryList.size();
        }

        @Inject(method = "scrollItems", at = @At(value = "HEAD"))
        private void setupFilterList(float position, CallbackInfo ci) {
            if(this.isEntryListDirty){
                Predicate<Entry> removeNonVisibleEntries = Entry::isVisible;

                this.filteredEntryList.clear();
                this.filteredEntryList.addAll(this.getDefaultEntryList().stream().filter(removeNonVisibleEntries).toList());

                this.isEntryListDirty = false;
            }
        }

        /**
         * @author Blodhgarm
         * @reason adjusted to use filteredEntryList instead
         */
        @Overwrite
        public void scrollItems(float position){
            if(this.isEntryListDirty){
                Predicate<Entry> removeNonVisibleEntries = Entry::isVisible;

                this.filteredEntryList.clear();
                this.filteredEntryList.addAll(this.getDefaultEntryList().stream().filter(removeNonVisibleEntries).toList());

                this.isEntryListDirty = false;
            }

            int positionOffset = this.getRow(position);

            //---------------------------------------------

            for(int k = 0; k < 5; ++k) {
                for(int l = 0; l < 9; ++l) {
                    int m = l + ((k + positionOffset) * 9);

                    if (m >= 0 && m < this.filteredEntryList.size()) {
                        ((CondensedInventory)CreativeInventoryScreen.INVENTORY).setEntryStack(l + k * 9, this.filteredEntryList.get(m));
                    } else {
                        ((CondensedInventory)CreativeInventoryScreen.INVENTORY).setStack(l + k * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        /**
         * @author Blodhgarm
         * @reason adjusted to use filteredEntryList instead and prevent negative values
         */
        @Overwrite
        public int getOverflowRows() {
            return !this.filteredEntryList.isEmpty() && this.shouldShowScrollbar() ? MathHelper.ceil((this.filteredEntryList.size() / 9F) - 5F) : 0;
        }

        //----------

        @Override
        public DefaultedList<Entry> getDefaultEntryList() {
            return this.defaultEntryList;
        }

        @Override
        public void markEntryListDirty() {
            this.isEntryListDirty = true;
        }
    }
}
