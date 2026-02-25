package net.cupellation.block.entity;

import net.cupellation.block.SmelterBlock;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ConfigInit;
import net.cupellation.misc.MoltenHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;

public class SmelterBlockEntity extends BlockEntity implements Inventory {

    private DefaultedList<ItemStack> inventory;

    private boolean isFormed = false;
    private BlockPos cornerMin = BlockPos.ORIGIN;
    private int structureWidth = 3;
    private int structureDepth = 3;
    private int structureHeight = 2;

    private int validateCooldown = 0;
    private static final int VALIDATE_INTERVAL = 40;

    private int moltenMetal = 0;
    private int metalType = 0;

    private int fuelTime = 0;
    private int maxFuelTime = 0;

    private int temperature = 0;
    private int maxTemperature = 0;
    private static final int TEMP_MAX_ABSOLUTE = 1600;
    private static final float TEMP_RISE_RATE = 1.5f;
    private static final float TEMP_DECAY_RATE = 0.5f;

    private final int[] smeltProgress = new int[3];
    private final int[] smeltTotal = new int[3];

    private static final int PROP_MOLTEN_METAL_LOW = 0;
    private static final int PROP_MOLTEN_METAL_HIGH = 1;
    private static final int PROP_MAX_CAP_LOW = 2;
    private static final int PROP_MAX_CAP_HIGH = 3;
    private static final int PROP_METAL_TYPE = 4;
    private static final int PROP_IS_FORMED = 5;
    private static final int PROP_FUEL_TIME = 6;
    private static final int PROP_MAX_FUEL_TIME = 7;
    private static final int PROP_TEMPERATURE = 8;
    private static final int PROP_MAX_TEMPERATURE = 9;
    private static final int PROP_SMELT_PROGRESS_0 = 10;
    private static final int PROP_SMELT_PROGRESS_1 = 11;
    private static final int PROP_SMELT_PROGRESS_2 = 12;
    private static final int PROP_SMELT_TOTAL_0 = 13;
    private static final int PROP_SMELT_TOTAL_1 = 14;
    private static final int PROP_SMELT_TOTAL_2 = 15;
    private static final int PROP_COUNT = 16;

    public final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            int cap = getMaxCapacity();
            return switch (index) {
                case PROP_MOLTEN_METAL_LOW -> moltenMetal & 0xFFFF;
                case PROP_MOLTEN_METAL_HIGH -> (moltenMetal >> 16) & 0xFFFF;
                case PROP_MAX_CAP_LOW -> cap & 0xFFFF;
                case PROP_MAX_CAP_HIGH -> (cap >> 16) & 0xFFFF;
                case PROP_METAL_TYPE -> metalType;
                case PROP_IS_FORMED -> isFormed ? 1 : 0;
                case PROP_FUEL_TIME -> fuelTime;
                case PROP_MAX_FUEL_TIME -> maxFuelTime;
                case PROP_TEMPERATURE -> temperature;
                case PROP_MAX_TEMPERATURE -> maxTemperature;
                case PROP_SMELT_PROGRESS_0 -> smeltProgress[0];
                case PROP_SMELT_PROGRESS_1 -> smeltProgress[1];
                case PROP_SMELT_PROGRESS_2 -> smeltProgress[2];
                case PROP_SMELT_TOTAL_0 -> smeltTotal[0];
                case PROP_SMELT_TOTAL_1 -> smeltTotal[1];
                case PROP_SMELT_TOTAL_2 -> smeltTotal[2];
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case PROP_MOLTEN_METAL_LOW -> moltenMetal = (moltenMetal & 0xFFFF0000) | (value & 0xFFFF);
                case PROP_MOLTEN_METAL_HIGH -> moltenMetal = (moltenMetal & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case PROP_METAL_TYPE -> metalType = value;
                case PROP_IS_FORMED -> isFormed = (value == 1);
                case PROP_FUEL_TIME -> fuelTime = value;
                case PROP_MAX_FUEL_TIME -> maxFuelTime = value;
                case PROP_TEMPERATURE -> temperature = value;
                case PROP_MAX_TEMPERATURE -> maxTemperature = value;
                case PROP_SMELT_PROGRESS_0 -> smeltProgress[0] = value;
                case PROP_SMELT_PROGRESS_1 -> smeltProgress[1] = value;
                case PROP_SMELT_PROGRESS_2 -> smeltProgress[2] = value;
                case PROP_SMELT_TOTAL_0 -> smeltTotal[0] = value;
                case PROP_SMELT_TOTAL_1 -> smeltTotal[1] = value;
                case PROP_SMELT_TOTAL_2 -> smeltTotal[2] = value;
            }
        }

        @Override
        public int size() {
            return PROP_COUNT;
        }
    };

    public SmelterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SMELTER_ENTITY, pos, state);
        this.inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
    }

    @Override
    protected void readNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        inventory.clear();
        Inventories.readNbt(nbt, inventory, registryLookup);

        isFormed = nbt.getBoolean("isFormed");
        moltenMetal = nbt.getInt("moltenMetal");
        metalType = nbt.getInt("metalType");
        fuelTime = nbt.getInt("fuelTime");
        maxFuelTime = nbt.getInt("maxFuelTime");
        temperature = nbt.getInt("temperature");
        maxTemperature = nbt.getInt("maxTemperature");

        for (int i = 0; i < 3; i++) {
            smeltProgress[i] = nbt.getInt("smeltProgress" + i);
            smeltTotal[i] = nbt.getInt("smeltTotal" + i);
        }

        if (nbt.contains("cornerMinX")) {
            cornerMin = new BlockPos(nbt.getInt("cornerMinX"), nbt.getInt("cornerMinY"), nbt.getInt("cornerMinZ"));
        }
        structureWidth = nbt.getInt("structureWidth");
        structureDepth = nbt.getInt("structureDepth");
        structureHeight = nbt.getInt("structureHeight");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);

        nbt.putBoolean("isFormed", isFormed);
        nbt.putInt("moltenMetal", moltenMetal);
        nbt.putInt("metalType", metalType);
        nbt.putInt("fuelTime", fuelTime);
        nbt.putInt("maxFuelTime", maxFuelTime);
        nbt.putInt("temperature", temperature);
        nbt.putInt("maxTemperature", maxTemperature);

        for (int i = 0; i < 3; i++) {
            nbt.putInt("smeltProgress" + i, smeltProgress[i]);
            nbt.putInt("smeltTotal" + i, smeltTotal[i]);
        }

        if (cornerMin != null) {
            nbt.putInt("cornerMinX", cornerMin.getX());
            nbt.putInt("cornerMinY", cornerMin.getY());
            nbt.putInt("cornerMinZ", cornerMin.getZ());
        }
        nbt.putInt("structureWidth", structureWidth);
        nbt.putInt("structureDepth", structureDepth);
        nbt.putInt("structureHeight", structureHeight);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, SmelterBlockEntity be) {
        be.clientTick();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, SmelterBlockEntity be) {
        be.serverTick();
    }

    private void clientTick() {

    }

    private void serverTick() {
        if (validateCooldown > 0) {
            validateCooldown--;
        } else {
            validateCooldown = VALIDATE_INTERVAL;
            boolean wasFormed = isFormed;
            isFormed = validateStructure();
            if (wasFormed && !isFormed) onStructureDestroyed();
            if (!wasFormed && isFormed) onStructureFormed();
        }

        if (!isFormed) {
            return;
        }
        boolean hasAnyInput = hasAnyInput();
        boolean tankFull = moltenMetal >= getMaxCapacity();

        if (hasAnyInput && !tankFull) {
            if (fuelTime <= 0) {
                tryConsumeFuel();
            }
        }

        tickTemperature();

        boolean burning = fuelTime > 0;
        if (burning) {
            fuelTime--;
        }
        if (getCachedState().get(SmelterBlock.LIT) != burning) {
            world.setBlockState(pos, getCachedState().with(SmelterBlock.LIT, burning));
        }

        for (int i = 0; i < 3; i++) {
            tickSmeltSlot(i);
        }

        if (moltenMetal > 0) {
            tickFluidDamage();
        }
    }

    private void tickTemperature() {
        if (fuelTime > 0) {
            if (temperature < maxTemperature) {
                temperature = Math.min(maxTemperature, (int) (temperature + TEMP_RISE_RATE));
            }
        } else {
            if (temperature > 0) {
                temperature = Math.max(0, (int) (temperature - TEMP_DECAY_RATE));
            }
        }
    }

    private void tryConsumeFuel() {
        ItemStack fuel = inventory.get(0);
        if (fuel.isEmpty() || !AbstractFurnaceBlockEntity.canUseAsFuel(fuel)) {
            return;
        }
        Map<?, Integer> fuelMap = AbstractFurnaceBlockEntity.createFuelTimeMap();
        Integer time = fuelMap.get(fuel.getItem());
        if (time == null || time <= 0) {
            return;
        }
        maxFuelTime = time;
        fuelTime = maxFuelTime;
        maxTemperature = MoltenHelper.getFuelMaxTemp(fuel);
        fuel.decrement(1);
        markDirty();
    }

    private void tickSmeltSlot(int slotIndex) {
        int invSlot = slotIndex + 1;
        ItemStack stack = inventory.get(invSlot);

        if (stack.isEmpty()) {
            smeltProgress[slotIndex] = 0;
            smeltTotal[slotIndex] = 0;
            return;
        }

        int type = MoltenHelper.getMetalType(stack);
        int yield = MoltenHelper.getMetalYield(stack);

        if (type == 0 || yield == 0) {
            smeltProgress[slotIndex] = 0;
            smeltTotal[slotIndex] = 0;
            return;
        }

        if (moltenMetal > 0 && metalType != type) {
            smeltProgress[slotIndex] = 0;
            return;
        }

        int requiredTemp = MoltenHelper.getRequiredTemp(type);
        if (temperature < requiredTemp) {
            if (smeltProgress[slotIndex] > 0) smeltProgress[slotIndex]--;
            return;
        }

        if (moltenMetal + yield > getMaxCapacity()) {
            smeltProgress[slotIndex] = 0;
            return;
        }

        if (smeltTotal[slotIndex] == 0) {
            smeltTotal[slotIndex] = MoltenHelper.getSmeltTime(stack);
        }

        smeltProgress[slotIndex]++;

        if (smeltProgress[slotIndex] >= smeltTotal[slotIndex]) {
            moltenMetal += yield;
            metalType = type;
            stack.decrement(1);
            smeltProgress[slotIndex] = 0;
            smeltTotal[slotIndex] = 0;
            markDirty();
        }
    }

    private void tickFluidDamage() {
        Direction facing = getCachedState().get(SmelterBlock.FACING);
        Direction right = facing.rotateYCounterclockwise();

        BlockPos p1 = cornerMin.offset(right, 1).offset(facing.getOpposite(), 1);
        BlockPos p2 = cornerMin.offset(right, structureWidth - 2).offset(facing.getOpposite(), structureDepth - 2);

        float fluidHeight = getFillPercent() * (structureHeight * 0.8f);

        Box fluidBox = new Box(Math.min(p1.getX(), p2.getX()), p1.getY(), Math.min(p1.getZ(), p2.getZ()),
                Math.max(p1.getX(), p2.getX()) + 1, p1.getY() + fluidHeight, Math.max(p1.getZ(), p2.getZ()) + 1);

        World world = this.getWorld();
        assert world != null;

        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, fluidBox, LivingEntity::isAlive)) {
            entity.damage(world.getDamageSources().lava(), 4.0f);
            entity.setOnFireFor(5);
        }
        for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, fluidBox.expand(0, 0.5, 0), e -> true)) {
            if (!item.isFireImmune()) item.discard();
        }
    }

    public boolean validateStructure() {
        World world = this.getWorld();
        if (world == null) return false;

        Direction facing = world.getBlockState(this.pos).get(SmelterBlock.FACING);
        Direction left = facing.rotateYClockwise();

        for (int w = ConfigInit.CONFIG.smelterMaxWidth; w >= 5; w -= 2) {
            for (int d = ConfigInit.CONFIG.smelterMaxWidth; d >= 5; d--) {
                for (int h = ConfigInit.CONFIG.smelterMaxHeight; h >= 1; h--) {
                    int halfWidth = (w - 1) / 2;
                    BlockPos corner = this.pos.offset(left, halfWidth);
                    if (tryValidate(world, corner, facing, w, d, h)) {
                        this.cornerMin = corner;
                        this.structureWidth = w;
                        this.structureDepth = d;
                        this.structureHeight = h;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryValidate(World world, BlockPos corner, Direction facing, int w, int d, int h) {
        return tryCheckBottom(world, corner, facing, w, d) && tryCheckWalls(world, corner, facing, w, d, h);
    }

    private boolean tryCheckBottom(World world, BlockPos corner, Direction facing, int w, int d) {
        Direction right = facing.rotateYCounterclockwise();
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                BlockPos check = corner.down().offset(right, x).offset(facing.getOpposite(), z);
                if (!world.getBlockState(check).isOf(Blocks.STONE_BRICKS)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean tryCheckWalls(World world, BlockPos corner, Direction facing, int w, int d, int h) {
        Direction right = facing.rotateYCounterclockwise();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                for (int z = 0; z < d; z++) {
                    BlockPos check = corner.offset(right, x).offset(facing.getOpposite(), z).up(y);
                    boolean isWall = (x == 0 || x == w - 1 || z == 0 || z == d - 1);

                    if (check.equals(this.pos)) {
                        continue;
                    }
                    BlockState state = world.getBlockState(check);
                    if (isWall) {
                        if (!state.isOf(Blocks.STONE_BRICKS)) {
                            return false;
                        }
                    } else {
                        if (!state.isAir()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void onStructureFormed() {
        markDirty();
    }

    private void onStructureDestroyed() {
        for (int i = 0; i < 3; i++) {
            smeltProgress[i] = 0;
            smeltTotal[i] = 0;
        }
        markDirty();
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        markDirty();
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (slot >= 1 && slot <= 3) {
            int i = slot - 1;
            smeltProgress[i] = 0;
            smeltTotal[i] = 0;
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sendUpdate();
    }

    private void sendUpdate() {
        if (getWorld() != null) {
            BlockState state = getWorld().getBlockState(pos);
            getWorld().updateListeners(pos, state, state, 3);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    public boolean isFormed() {
        return isFormed;
    }

    public int getMoltenMetal() {
        return moltenMetal;
    }

    public int getMetalType() {
        return metalType;
    }

    public int getFuelTime() {
        return fuelTime;
    }

    public int getMaxFuelTime() {
        return maxFuelTime;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public int getSmeltProgress(int i) {
        return smeltProgress[i];
    }

    public int getSmeltTotal(int i) {
        return smeltTotal[i];
    }

    public BlockPos getCornerMin() {
        return cornerMin;
    }

    public int getStructureWidth() {
        return structureWidth;
    }

    public int getStructureHeight() {
        return structureHeight;
    }

    public int getStructureDepth() {
        return structureDepth;
    }

    public int getMaxCapacity() {
        int innerW = Math.max(1, structureWidth - 2);
        int innerD = Math.max(1, structureDepth - 2);
        return innerW * innerD * structureHeight * 1000;
    }

    public float getFillPercent() {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) moltenMetal / cap : 0f;
    }

    public boolean isBurning() {
        return fuelTime > 0;
    }

    private boolean hasAnyInput() {
        for (int i = 1; i <= 3; i++) {
            if (!inventory.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}