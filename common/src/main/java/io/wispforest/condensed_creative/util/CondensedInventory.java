package io.wispforest.condensed_creative.util;

import io.wispforest.condensed_creative.entry.Entry;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CondensedInventory extends SimpleContainer {

    private final NonNullList<Entry> entryStacks;

    public CondensedInventory(int size) {
        super(size);

        this.entryStacks = NonNullList.withSize(size, Entry.EMPTY_ENTRY);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.entryStacks.size() ? this.entryStacks.get(slot).getEntryStack() : ItemStack.EMPTY;
    }

    public Entry getEntryStack(int slot) {
        return slot >= 0 && slot < this.entryStacks.size() ? this.entryStacks.get(slot) : Entry.EMPTY_ENTRY;
    }

    /**
     * Clears this util and return all the non-empty stacks in a list.
     */
    @Override
    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = (List)this.entryStacks.stream().filter(entry -> !entry.isEmpty()).map(Entry::getEntryStack).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(getItemStackList(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    public ItemStack removeItemType(Item item, int count) {
        ItemStack itemStack = new ItemStack(item, 0);

        for(int i = this.getContainerSize() - 1; i >= 0; --i) {
            ItemStack itemStack2 = this.getItem(i);
            if (itemStack2.getItem().equals(item)) {
                int j = count - itemStack.getCount();
                ItemStack itemStack3 = itemStack2.split(j);
                itemStack.grow(itemStack3.getCount());
                if (itemStack.getCount() == count) {
                    break;
                }
            }
        }

        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        boolean bl = false;

        for(ItemStack itemStack : getItemStackList()) {
            if (itemStack.isEmpty() || ItemStack.isSameItemSameTags(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                bl = true;
                break;
            }
        }

        return bl;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemStack = getItem(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.entryStacks.set(slot, Entry.EMPTY_ENTRY);
            return itemStack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.entryStacks.set(slot, Entry.of(stack));
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    public void setEntryStack(int slot, Entry entryStack) {
        this.entryStacks.set(slot, entryStack);

        ItemStack stack = entryStack.getEntryStack();
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemStack : getItemStackList()) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setChanged() {
        super.setChanged();

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.entryStacks.clear();
        this.clearContent();
    }

    @Override
    public void fillStackedContents(StackedContents finder) {
        for(ItemStack itemStack : getItemStackList()) {
            finder.accountStack(itemStack);
        }

    }

    public String toString() {
        return ((List)this.entryStacks.stream().filter(entry -> !entry.isEmpty()).map(Entry::toString).collect(Collectors.toList())).toString();
    }

    //TODO: WHY IS THIS HERE?
    public void fromTag(ListTag nbtList) {
        for(int i = 0; i < nbtList.size(); ++i) {
            ItemStack itemStack = ItemStack.of(nbtList.getCompound(i));
            if (!itemStack.isEmpty()) {
                this.addItem(itemStack);
            }
        }
    }

    private List<ItemStack> getItemStackList(){
        return this.entryStacks.stream().map(Entry::getEntryStack).toList();
    }
}
