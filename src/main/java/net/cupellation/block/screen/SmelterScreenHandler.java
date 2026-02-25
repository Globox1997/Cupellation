package net.cupellation.block.screen;

import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.cupellation.network.packet.SmelterScreenPacket;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SmelterScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    private Identifier metalTypeId = null;

    private final BlockPos pos;

    private static final int PROP_COUNT = 15;

    private static final int PROP_MOLTEN_METAL_LOW = 0;
    private static final int PROP_MOLTEN_METAL_HIGH = 1;
    private static final int PROP_MAX_CAP_LOW = 2;
    private static final int PROP_MAX_CAP_HIGH = 3;
    private static final int PROP_IS_FORMED = 4;
    private static final int PROP_FUEL_TIME = 5;
    private static final int PROP_MAX_FUEL_TIME = 6;
    private static final int PROP_TEMPERATURE = 7;
    private static final int PROP_MAX_TEMPERATURE = 8;
    private static final int PROP_SMELT_PROGRESS_0 = 9;
    private static final int PROP_SMELT_PROGRESS_1 = 10;
    private static final int PROP_SMELT_PROGRESS_2 = 11;
    private static final int PROP_SMELT_TOTAL_0 = 12;
    private static final int PROP_SMELT_TOTAL_1 = 13;
    private static final int PROP_SMELT_TOTAL_2 = 14;


    public SmelterScreenHandler(int syncId, PlayerInventory playerInventory, SmelterScreenPacket packet) {
        this(syncId, playerInventory, new SimpleInventory(4), new ArrayPropertyDelegate(PROP_COUNT), packet.pos());
    }

    public SmelterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate, BlockPos blockPos) {
        super(BlockInit.SMELTER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.pos = blockPos;

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

                } else if (SmelterData.hasItem(originalStack.getItem())) {
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
        int low = propertyDelegate.get(PROP_MOLTEN_METAL_LOW) & 0xFFFF;
        int high = propertyDelegate.get(PROP_MOLTEN_METAL_HIGH) & 0xFFFF;
        return low | (high << 16);
    }

    public int getMaxCapacity() {
        int low = propertyDelegate.get(PROP_MAX_CAP_LOW) & 0xFFFF;
        int high = propertyDelegate.get(PROP_MAX_CAP_HIGH) & 0xFFFF;
        return low | (high << 16);
    }

    public boolean isFormed() {
        return propertyDelegate.get(PROP_IS_FORMED) == 1;
    }

    public int getFuelTime() {
        return propertyDelegate.get(PROP_FUEL_TIME);
    }

    public int getMaxFuelTime() {
        return propertyDelegate.get(PROP_MAX_FUEL_TIME);
    }

    public int getTemperature() {
        return propertyDelegate.get(PROP_TEMPERATURE);
    }

    public int getMaxTemperature() {
        return propertyDelegate.get(PROP_MAX_TEMPERATURE);
    }

    public int getSmeltProgress(int slot) {
        return propertyDelegate.get(PROP_SMELT_PROGRESS_0 + slot);
    }

    public int getSmeltTotal(int slot) {
        return propertyDelegate.get(PROP_SMELT_TOTAL_0 + slot);
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

    public int getMetalColor() {
        return SmelterData.getColor(metalTypeId);
    }

    public Identifier getMetalTexture() {
        return SmelterData.getTexture(metalTypeId);
    }

    public String getMetalName() {
        return SmelterData.getName(metalTypeId);
    }

    public void setMetalTypeId(Identifier id) {
        this.metalTypeId = id;
    }

    public Identifier getMetalTypeId() {
        return metalTypeId;
    }

    public void syncMetalType(World world) {
        if (world == null) {
            return;
        }
        if (world.getBlockEntity(this.pos) instanceof SmelterBlockEntity blockEntity) {
            setMetalTypeId(blockEntity.getMetalTypeId());
        }
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
    }

    private static class OreSlot extends Slot {
        public OreSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return SmelterData.hasItem(stack.getItem());
        }
    }
}