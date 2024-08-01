package io.wispforest.condensed_creative.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.entry.Entry;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.util.CondensedInventory;
import me.shedaniel.math.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class SlotRenderUtils {

    private static final ResourceLocation PLUS_ICON = CondensedCreative.location("textures/gui/plus_logo.png");
    private static final ResourceLocation MINUS_ICON = CondensedCreative.location("textures/gui/minus_logo.png");

    public static void renderExtraIfEntry(AbstractContainerScreen screen, GuiGraphics context, Slot slot){
        if(!(screen instanceof CreativeModeInventoryScreen && slot.container instanceof CondensedInventory inv)) return;

        Entry entryStack = inv.getEntryStack(slot.getContainerSlot());

        if(!(entryStack instanceof CondensedItemEntry entry)) return;

        int minX = slot.x;
        int minY = slot.y;

        int maxX = minX + 16;
        int maxY = minY + 16;

        if(CondensedItemEntry.CHILD_VISIBILITY.get(entry.condensedID)) {
            Color backgroundColor = Color.ofTransparent(0x7F111111);//Color.ofRGBA(186, 186, 186, 255);

            if(CondensedCreative.getConfig().entryBackgroundColor) {
                var offset = CondensedCreative.getConfig().entryBorderColor ? 0 : 1;

                context.fill(minX - offset, minY - offset, maxX + offset, maxY + offset, backgroundColor.getColor());
            }

            if(CondensedCreative.getConfig().entryBorderColor) {
                RenderSystem.enableBlend();

                Color outlineColor = Color.ofTransparent(CondensedCreative.getConfig().condensedEntryBorderColor);//Color.ofRGBA(251, 255, 0, 128);

                if (!isSlotAbovePartOfCondensedEntry(slot, entry.condensedID)) {
                    context.fill(minX - 1, minY - 1, maxX + 1, maxY - 16, outlineColor.getColor());
                }

                if (!isSlotBelowPartOfCondensedEntry(slot, entry.condensedID)) {
                    context.fill(minX - 1, minY + 16, maxX + 1, maxY + 1, outlineColor.getColor());
                }

                if (!isSlotRightPartOfCondensedEntry(slot, entry.condensedID)) {
                    context.fill(minX + 16, minY - 1, maxX + 1, maxY + 1, outlineColor.getColor());
                }

                if (!isSlotLeftPartOfCondensedEntry(slot, entry.condensedID)) {
                    context.fill(minX - 1, minY - 1, maxX - 16, maxY + 1, outlineColor.getColor());
                }
            }

            RenderSystem.disableBlend();
        }

        if(!entry.isChild) {
            ResourceLocation id = !CondensedItemEntry.CHILD_VISIBILITY.get(entry.condensedID) ? PLUS_ICON : MINUS_ICON;

            context.blit(id, minX, minY, 160, 0, 0, 16, 16, 16, 16);
        }
    }

    private static boolean isSlotAbovePartOfCondensedEntry(Slot slot, ResourceLocation condensedID){
        int topSlotIndex = slot.getContainerSlot() - 9;

        return topSlotIndex >= 0 &&
                ((CondensedInventory) slot.container).getEntryStack(topSlotIndex) instanceof CondensedItemEntry condensedItemEntry &&
                condensedID == condensedItemEntry.condensedID;
    }

    private static boolean isSlotBelowPartOfCondensedEntry(Slot slot, ResourceLocation condensedID){
        int bottomSlotIndex = slot.getContainerSlot() + 9;

        return bottomSlotIndex < slot.container.getContainerSize() &&
                ((CondensedInventory) slot.container).getEntryStack(bottomSlotIndex) instanceof CondensedItemEntry condensedItemEntry &&
                condensedID == condensedItemEntry.condensedID;
    }

    private static boolean isSlotLeftPartOfCondensedEntry(Slot slot, ResourceLocation condensedID){
        if(((slot.index) % 9 == 0)) return false;

        int leftSlotIndex = slot.getContainerSlot() - 1;

        return leftSlotIndex < slot.container.getContainerSize() &&
                ((CondensedInventory) slot.container).getEntryStack(leftSlotIndex) instanceof CondensedItemEntry condensedItemEntry &&
                condensedID == condensedItemEntry.condensedID;
    }

    private static boolean isSlotRightPartOfCondensedEntry(Slot slot, ResourceLocation condensedID){
        if(((slot.index) % 9 == 8)) return false;

        int rightSlotIndex = slot.getContainerSlot() + 1;

        return rightSlotIndex < slot.container.getContainerSize() &&
                ((CondensedInventory) slot.container).getEntryStack(rightSlotIndex) instanceof CondensedItemEntry condensedItemEntry &&
                condensedID == condensedItemEntry.condensedID;
    }
}
