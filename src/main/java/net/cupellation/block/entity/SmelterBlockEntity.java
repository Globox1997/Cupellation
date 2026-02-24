package net.cupellation.block.entity;

import net.cupellation.block.SmelterBlock;
import net.cupellation.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class SmelterBlockEntity extends BlockEntity implements Inventory {
    private DefaultedList<ItemStack> inventory;

    private boolean isFormed = false;
    private BlockPos cornerMin = BlockPos.ORIGIN;

    private int moltenMetal = 0;
    private static final int MAX_CAPACITY = 16000;
    private int metalType = 0;

    private int smeltProgress = 0;
    private static final int SMELT_TIME = 200;

    private int validateCooldown = 0;
    private static final int VALIDATE_INTERVAL = 40;

    public SmelterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SMELTER_ENTITY, pos, state);
        this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    protected void readNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        inventory.clear();
        Inventories.readNbt(nbt, inventory, registryLookup);

        isFormed = nbt.getBoolean("isFormed");
        moltenMetal = nbt.getInt("moltenMetal");
        metalType = nbt.getInt("metalType");
        smeltProgress = nbt.getInt("smeltProgress");

        if (nbt.contains("cornerMinX")) {
            cornerMin = new BlockPos(
                    nbt.getInt("cornerMinX"),
                    nbt.getInt("cornerMinY"),
                    nbt.getInt("cornerMinZ")
            );
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);

        nbt.putBoolean("isFormed", isFormed);
        nbt.putInt("moltenMetal", moltenMetal);
        nbt.putInt("metalType", metalType);
        nbt.putInt("smeltProgress", smeltProgress);

        if (cornerMin != null) {
            nbt.putInt("cornerMinX", cornerMin.getX());
            nbt.putInt("cornerMinY", cornerMin.getY());
            nbt.putInt("cornerMinZ", cornerMin.getZ());
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, SmelterBlockEntity blockEntity) {
        blockEntity.clientTick();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, SmelterBlockEntity blockEntity) {
        blockEntity.serverTick();
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

            if (wasFormed && !isFormed) {
                onStructureDestroyed();
            }
            if (!wasFormed && isFormed) {
                onStructureFormed();
            }
        }

        if (!isFormed) return;

        if (!inventory.get(0).isEmpty() && moltenMetal < MAX_CAPACITY) {
            smeltProgress++;

            if (smeltProgress >= SMELT_TIME) {
                smeltItem();
            }
        } else {
            smeltProgress = 0;
        }
        if (moltenMetal > 0) {
            Direction facing = getCachedState().get(SmelterBlock.FACING);
            Direction right = facing.rotateYCounterclockwise();

            BlockPos p1 = cornerMin.offset(right, 1).offset(facing.getOpposite(), 1);
            BlockPos p2 = p1.offset(right, 2).offset(facing.getOpposite(), 2);

            float fluidHeight = getFillPercent() * 1.6f;

            Box fluidBox = new Box(Math.min(p1.getX(), p2.getX()), p1.getY(), Math.min(p1.getZ(), p2.getZ()),
                    Math.max(p1.getX(), p2.getX()) + 1, p1.getY() + fluidHeight, Math.max(p1.getZ(), p2.getZ()) + 1);

//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.minX, fluidBox.minY, fluidBox.minZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.minX, fluidBox.maxY, fluidBox.minZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.maxX, fluidBox.maxY, fluidBox.minZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.maxX, fluidBox.minY, fluidBox.minZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.maxX, fluidBox.minY, fluidBox.maxZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.maxX, fluidBox.maxY, fluidBox.maxZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.minX, fluidBox.minY, fluidBox.maxZ, 5, 0.0, 0.0, 0.0, 0.0);
//            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, fluidBox.minX, fluidBox.maxY, fluidBox.maxZ, 5, 0.0, 0.0, 0.0, 0.0);

            List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, fluidBox, LivingEntity::isAlive);
            for (LivingEntity entity : entities) {
                entity.damage(world.getDamageSources().lava(), 4.0f);
                entity.setOnFireFor(5);
            }
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, fluidBox.expand(0, 0.5, 0), entity -> true);
            for (ItemEntity item : items) {
                if (!item.isFireImmune()) {
                    item.discard();
                }
            }
        }
    }

    public boolean validateStructure() {
        World world = this.getWorld();
        if (world == null) return false;

        BlockState controllerState = world.getBlockState(this.pos);
        Direction facing = controllerState.get(SmelterBlock.FACING);
        Direction left = facing.rotateYClockwise();
        BlockPos corner = this.pos.offset(left, 2).offset(facing.getOpposite(), 0);

        this.cornerMin = corner;
        if (!checkBottom(world, corner, facing)) {
            return false;
        }
        return checkWalls(world, corner, facing);
    }

    private boolean checkBottom(World world, BlockPos corner, Direction facing) {
        Direction right = facing.rotateYCounterclockwise();

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {

                BlockPos check = corner.down().offset(right, x).offset(facing.getOpposite(), z);
                if (!world.getBlockState(check).isOf(Blocks.STONE_BRICKS)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkWalls(World world, BlockPos corner, Direction facing) {
        Direction right = facing.rotateYCounterclockwise();

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    boolean isWall = (x == 0 || x == 4 || z == 0 || z == 4);

                    BlockPos check = corner.offset(right, x).offset(facing.getOpposite(), z).up(y);

//                    if (isWall)
//                        ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.FLAME, check.getX() + 0.5D, check.getY() + 0.5D, check.getZ() + 0.5D, 1, 0.0, 0.0, 0.0, 0.0);


                    boolean isWallBlock = world.getBlockState(check).isOf(Blocks.STONE_BRICKS);

                    if (isWall && !isWallBlock) {
                        if (check.equals(this.pos)) {
                            continue;
                        }
//                        System.out.println("?? "+check);
//                        ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.FLAME, check.getX()+0.5D, check.getY()+0.5D,check.getZ()+0.5D, 5, 0.0, 0.0, 0.0, 0.0);

                        return false;
                    }
                    if (!isWall && !world.getBlockState(check).isAir()) {
//                        System.out.println("XX "+check);
//                        ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.BUBBLE, check.getX()+0.5D, check.getY()+0.5D,check.getZ()+0.5D, 5, 0.0, 0.0, 0.0, 0.0);

                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sendUpdate();
    }

    private void sendUpdate() {
        if (this.getWorld() != null) {
            BlockState state = this.getWorld().getBlockState(this.pos);
            this.getWorld().updateListeners(this.pos, state, state, 3);
        }
    }

    @Override
    public void clear() {
        this.inventory.clear();
        this.markDirty();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.getStack(0).isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(0);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, 1);
        this.markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.markDirty();
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(0, stack);
        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
        return this.createNbt(registryLookup);
    }

    private void onStructureFormed() {
        markDirty();
    }

    private void onStructureDestroyed() {
        smeltProgress = 0;
        markDirty();
    }

    private void smeltItem() {
        ItemStack input = inventory.get(0);
        if (input.isEmpty()) return;

        int yield = getMetalYield(input);
        int type = getMetalType(input);

        if (moltenMetal > 0 && metalType != type) {
            smeltProgress = 0;
            return;
        }

        if (moltenMetal + yield <= MAX_CAPACITY) {
            moltenMetal += yield;
            metalType = type;
            input.decrement(1);
            smeltProgress = 0;
            markDirty();
        }
    }

    private int getMetalYield(ItemStack stack) {
        if (stack.isOf(Items.IRON_ORE) || stack.isOf(Items.RAW_IRON)) return 144;
        if (stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.RAW_GOLD)) return 144;
        if (stack.isOf(Items.IRON_INGOT)) return 144;
        if (stack.isOf(Items.GOLD_INGOT)) return 144;
        return 0;
    }

    private int getMetalType(ItemStack stack) {
        if (stack.isOf(Items.IRON_ORE) || stack.isOf(Items.RAW_IRON) || stack.isOf(Items.IRON_INGOT)) return 1;
        if (stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.RAW_GOLD) || stack.isOf(Items.GOLD_INGOT)) return 2;
        return 0;
    }

    public boolean isFormed() {
        return isFormed;
    }

    public int getMoltenMetal() {
        return moltenMetal;
    }

    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public int getMetalType() {
        return metalType;
    }

    public float getFillPercent() {
        return (float) moltenMetal / MAX_CAPACITY;
    }

    public int getSmeltProgress() {
        return smeltProgress;
    }

    public BlockPos getCornerMin() {
        return cornerMin;
    }

}