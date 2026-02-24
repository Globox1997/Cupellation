package net.cupellation.block.screen;

import net.cupellation.init.BlockInit;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SmelterScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public SmelterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(4), new ArrayPropertyDelegate(5));
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
                if (!this.insertItem(originalStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // TODO: CHECK IF FUEL OR SMELTABLE

                if (!this.insertItem(originalStack, 0, 1, false)) {
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

    public int getMetalType() {
        return propertyDelegate.get(2);
    }

    public int getSmeltProgress() {
        return propertyDelegate.get(3);
    }

    public boolean isFormed() {
        return propertyDelegate.get(4) == 1;
    }

    public float getFillPercent() {
        return (float) getMoltenMetal() / 16000;
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
            if (stack.isIn(ItemTags.COPPER_ORES) || stack.isIn(ItemTags.GOLD_ORES) || stack.isIn(ItemTags.IRON_ORES)) {
                return true;
            }
            return false;
        }
    }
}