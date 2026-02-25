package net.cupellation.block.screen;

import net.cupellation.init.BlockInit;
import net.cupellation.misc.MoltenHelper;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SmelterScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    private static final int PROP_COUNT = 16;

    public SmelterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(4), new ArrayPropertyDelegate(PROP_COUNT));
    }

    public SmelterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(BlockInit.SMELTER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;

        checkSize(inventory, 4);
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 152, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
            }
        });

        this.addSlot(new OreSlot(inventory, 1, 17, 17));
        this.addSlot(new OreSlot(inventory, 2, 17, 35));
        this.addSlot(new OreSlot(inventory, 3, 17, 53));

        this.addProperties(propertyDelegate);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (slotIndex >= 0 && slotIndex < 4) {
                if (!this.insertItem(originalStack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (AbstractFurnaceBlockEntity.canUseAsFuel(originalStack)) {
                    if (!this.insertItem(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (MoltenHelper.isSmeltable(originalStack)) {
                    if (!this.insertItem(originalStack, 1, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player) && isFormed();
    }

    public int getMoltenMetal() {
        int low = propertyDelegate.get(0) & 0xFFFF;
        int high = propertyDelegate.get(1) & 0xFFFF;
        return low | (high << 16);
    }

    public int getMaxCapacity() {
        int low = propertyDelegate.get(2) & 0xFFFF;
        int high = propertyDelegate.get(3) & 0xFFFF;
        return low | (high << 16);
    }

    public int getMetalType() {
        return propertyDelegate.get(4);
    }

    public boolean isFormed() {
        return propertyDelegate.get(5) == 1;
    }

    public int getFuelTime() {
        return propertyDelegate.get(6);
    }

    public int getMaxFuelTime() {
        return propertyDelegate.get(7);
    }

    public int getTemperature() {
        return propertyDelegate.get(8);
    }

    public int getMaxTemperature() {
        return propertyDelegate.get(9);
    }

    public int getSmeltProgress(int slot) {
        return propertyDelegate.get(10 + slot);
    }

    public int getSmeltTotal(int slot) {
        return propertyDelegate.get(13 + slot);
    }

    public float getFillPercent() {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) getMoltenMetal() / cap : 0f;
    }

    public float getFuelPercent() {
        int max = getMaxFuelTime();
        return max > 0 ? (float) getFuelTime() / max : 0f;
    }

    public float getSmeltPercent(int slot) {
        int total = getSmeltTotal(slot);
        return total > 0 ? (float) getSmeltProgress(slot) / total : 0f;
    }

    public boolean isBurning() {
        return getFuelTime() > 0;
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static class OreSlot extends Slot {
        public OreSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return MoltenHelper.isSmeltable(stack);
        }
    }
}