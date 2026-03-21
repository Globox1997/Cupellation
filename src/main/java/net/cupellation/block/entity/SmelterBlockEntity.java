package net.cupellation.block.entity;

import net.cupellation.block.SmelterBlock;
import net.cupellation.block.screen.SmelterScreenHandler;
import net.cupellation.data.*;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ConfigInit;
import net.cupellation.init.TagInit;
import net.cupellation.network.packet.SmelterFluidSyncPacket;
import net.cupellation.network.packet.SmelterScreenPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class SmelterBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory<SmelterScreenPacket> {

    private final DefaultedList<ItemStack> inventory;

    private boolean isFormed = false;
    private BlockPos cornerMin = BlockPos.ORIGIN;
    private int structureWidth = 3;
    private int structureDepth = 3;
    private int structureHeight = 2;

    private int validateCooldown = 0;
    private static final int VALIDATE_INTERVAL = 40;

    private static final int ALLOY_MB_PER_TICK = 144;

    private int lastFilledLayers = -1;

    public static final int MAX_METALS = 3;
    private final int[] metalAmounts = new int[MAX_METALS];
    private final int[] slagAmounts = new int[MAX_METALS];
    private final Identifier[] metalTypeIds = new Identifier[MAX_METALS];

    @Nullable
    private Identifier currentSmelterTypeId = null;

    private static final int ALLOY_TICK_INTERVAL = 20;
    private int alloyTickCooldown = 0;

    private boolean fluidDirty = false;

    private boolean redstonePowered = false;
    private int fuelTime = 0;
    private int maxFuelTime = 0;

    private int temperature = 0;
    private int maxTemperature = 0;
    private static final float TEMP_RISE_RATE = 1.5f;
    private static final float TEMP_DECAY_RATE = 0.5f;

    private static final int MINIMUM_TEMPERATURE = 400;
    private static final int ITEM_COOLING_TEMPERATURE = 20;

    private static final int FLUX_CONVERSION_RATE = 50;

    private final int[] smeltProgress = new int[3];
    private final int[] smeltTotal = new int[3];

    private static final int PROP_IS_FORMED = 0;
    private static final int PROP_FUEL_TIME = 1;
    private static final int PROP_MAX_FUEL_TIME = 2;
    private static final int PROP_TEMPERATURE = 3;
    private static final int PROP_MAX_TEMPERATURE = 4;
    private static final int PROP_SMELT_PROGRESS_0 = 5;
    private static final int PROP_SMELT_PROGRESS_1 = 6;
    private static final int PROP_SMELT_PROGRESS_2 = 7;
    private static final int PROP_SMELT_TOTAL_0 = 8;
    private static final int PROP_SMELT_TOTAL_1 = 9;
    private static final int PROP_SMELT_TOTAL_2 = 10;
    private static final int PROP_COUNT = 11;

    public final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            int cap = getMaxCapacity();
            return switch (index) {
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
        for (int i = 0; i < MAX_METALS; i++) {
            metalAmounts[i] = nbt.getInt("metalAmount" + i);
            slagAmounts[i] = nbt.getInt("slagAmount" + i);
            String idStr = nbt.getString("metalTypeId" + i);
            metalTypeIds[i] = idStr.isEmpty() ? null : Identifier.of(idStr);
        }

        fuelTime = nbt.getInt("fuelTime");
        maxFuelTime = nbt.getInt("maxFuelTime");
        temperature = nbt.getInt("temperature");
        maxTemperature = nbt.getInt("maxTemperature");
        redstonePowered = nbt.getBoolean("redstonePowered");

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

        String typeIdStr = nbt.getString("smelterTypeId");
        currentSmelterTypeId = typeIdStr.isEmpty() ? null : Identifier.of(typeIdStr);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);

        nbt.putBoolean("isFormed", isFormed);
        for (int i = 0; i < MAX_METALS; i++) {
            nbt.putInt("metalAmount" + i, metalAmounts[i]);
            nbt.putInt("slagAmount" + i, slagAmounts[i]);
            nbt.putString("metalTypeId" + i, metalTypeIds[i] != null ? metalTypeIds[i].toString() : "");
        }
        nbt.putInt("fuelTime", fuelTime);
        nbt.putInt("maxFuelTime", maxFuelTime);
        nbt.putInt("temperature", temperature);
        nbt.putInt("maxTemperature", maxTemperature);
        nbt.putBoolean("redstonePowered", redstonePowered);

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

        nbt.putString("smelterTypeId", currentSmelterTypeId != null ? currentSmelterTypeId.toString() : "");
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, SmelterBlockEntity blockEntity) {
        blockEntity.clientTick();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, SmelterBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    private void clientTick() {
        if (!isFormed || getTotalFluid() <= 0) {
            return;
        }
        if (world == null || !world.isRaining()) {
            return;
        }
        Direction facing = getCachedState().get(SmelterBlock.FACING);
        Direction right = facing.rotateYCounterclockwise();

        int innerW = structureWidth - 2;
        int innerD = structureDepth - 2;

        int rx = world.getRandom().nextInt(innerW);
        int rz = world.getRandom().nextInt(innerD);

        BlockPos innerPos = cornerMin.offset(right, 1 + rx).offset(facing.getOpposite(), 1 + rz).up(structureHeight);

        if (!world.isSkyVisible(innerPos)) {
            return;
        }
        if (world.getRandom().nextInt(10) == 0) {
            double px = innerPos.getX() + world.getRandom().nextDouble();
            double py = cornerMin.getY() + getTotalFillPercent() * maxFillHeight();
            double pz = innerPos.getZ() + world.getRandom().nextDouble();

            world.playSound(px, py, pz, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.3f, 0.8f + world.getRandom().nextFloat() * 0.4f, false);
        }
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

        if (!isFormed) {
            return;
        }
        if ((redstonePowered || hasAnyInput()) && getTotalFluid() < getMaxCapacity()) {
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

        tickAlloy();

        if (getTotalFluid() > 0) {
            tickFluidDamage();
        }

        int newFilledLayers = calcFilledLayers();
        if (newFilledLayers != lastFilledLayers) {
            lastFilledLayers = newFilledLayers;
            updateLightBlocks();
        }
        if (fluidDirty) {
            fluidDirty = false;
            if (world instanceof ServerWorld serverWorld) {
                SmelterFluidSyncPacket packet = SmelterFluidSyncPacket.from(this);
                serverWorld.getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
            }
        }
    }

    private void tickTemperature() {
        if (fuelTime > 0) {
            if (temperature < maxTemperature) {
                temperature = Math.min(maxTemperature, (int) (temperature + TEMP_RISE_RATE));
            } else if (temperature > maxTemperature) {
                temperature = Math.max(maxTemperature, (int) (temperature - TEMP_DECAY_RATE));
            }
        } else {
            if (temperature > 0) {
                temperature = Math.max(0, (int) (temperature - TEMP_DECAY_RATE));
            }
        }
    }

    private void tryConsumeFuel() {
        ItemStack fuel = inventory.get(0);
        if (fuel.isEmpty()) {
            return;
        }
        FuelData fuelData = SmelterData.getFuelData(fuel.getItem());
        if (fuelData == null) {
            return;
        }
        int burnTime = fuelData.burnTime();
        if (burnTime <= 0) {
            return;
        }
        maxFuelTime = burnTime;
        fuelTime = burnTime;
        maxTemperature = fuelData.maxTemperature();

        ItemStack remainder = fuel.getItem().getRecipeRemainder() != null ? new ItemStack(fuel.getItem().getRecipeRemainder()) : ItemStack.EMPTY;

        fuel.decrement(1);

        if (!remainder.isEmpty()) {
            if (fuel.isEmpty()) {
                inventory.set(0, remainder);
            } else {
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, remainder));
                }
            }
        }
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

        SmelterItemData data = SmelterData.getItemData(stack.getItem());
        if (data == null) {
            smeltProgress[slotIndex] = 0;
            smeltTotal[slotIndex] = 0;
            return;
        }

        Identifier itemMetalType = data.metalTypeId();

        Set<Identifier> present = getPresentMetals();
        if (!present.contains(itemMetalType)) {
            if (!SmelterData.canAddMetal(present, itemMetalType)) {
                smeltProgress[slotIndex] = 0;
                return;
            }
        }

        int requiredTemp = SmelterData.getRequiredTemp(itemMetalType);
        if (temperature < requiredTemp) {
            if (smeltProgress[slotIndex] > 0) {
                smeltProgress[slotIndex]--;
            }
            return;
        }

        if (currentSmelterTypeId != null) {
            SmelterTypeData smelterType = SmelterData.getAllTypes().stream().filter(type -> type.id().equals(currentSmelterTypeId)).findFirst().orElse(null);
            if (smelterType != null && !smelterType.allowsMetal(itemMetalType)) {
                smeltProgress[slotIndex] = 0;
                return;
            }
        }

        if (getTotalFluid() + data.yield() > getMaxCapacity()) {
            smeltProgress[slotIndex] = 0;
            return;
        }

        if (smeltTotal[slotIndex] == 0) {
            smeltTotal[slotIndex] = data.smeltTime();
        }

        smeltProgress[slotIndex]++;

        if (smeltProgress[slotIndex] >= smeltTotal[slotIndex]) {
            int totalYield = data.yield();
            int slagYield = Math.max(0, totalYield * ConfigInit.CONFIG.slagRatio / 100);
            int metalYield = totalYield - slagYield;

            int slot = getSlotForMetal(itemMetalType);
            if (slot == -1) {
                slot = getFirstFreeSlot();
                if (slot == -1) {
                    smeltProgress[slotIndex] = 0;
                    return;
                }
                metalTypeIds[slot] = itemMetalType;
            }

            metalAmounts[slot] += metalYield;
            slagAmounts[slot] += slagYield;

            stack.decrement(1);
            smeltProgress[slotIndex] = 0;
            smeltTotal[slotIndex] = 0;
            markFluidDirty();
        }
    }

    private void tickAlloy() {
        if (alloyTickCooldown > 0) {
            alloyTickCooldown--;
            return;
        }
        alloyTickCooldown = ALLOY_TICK_INTERVAL;

        Set<Identifier> present = getPresentMetals();

        if (present.size() < 2) {
            return;
        }
        MetalTypeData alloy = SmelterData.findAlloyFor(present);

        if (alloy == null) {
            return;
        }
        Map<Identifier, Integer> amounts = new HashMap<>();
        for (int i = 0; i < MAX_METALS; i++) {
            if (metalTypeIds[i] != null) {
                amounts.put(metalTypeIds[i], metalAmounts[i]);
            }
        }

        int multiplier = alloy.calcAlloyMultiplier(amounts);
        if (multiplier <= 0) {
            return;
        }
        int totalPartsPerUnit = 0;
        for (MetalTypeData.AlloyIngredient ingredient : alloy.alloyFrom()) {
            totalPartsPerUnit += ingredient.parts();
        }
        int maxMultiplier = Math.max(1, ALLOY_MB_PER_TICK / totalPartsPerUnit);
        multiplier = Math.min(multiplier, maxMultiplier);

        int totalConsumed = 0;
        for (MetalTypeData.AlloyIngredient ingredient : alloy.alloyFrom()) {
            totalConsumed += ingredient.parts() * multiplier;
        }

        int totalSlagConsumed = 0;
        for (MetalTypeData.AlloyIngredient ingredient : alloy.alloyFrom()) {
            int slot = getSlotForMetal(ingredient.metalId());
            if (slot == -1) {
                return;
            }
            int consumed = ingredient.parts() * multiplier;
            if (metalAmounts[slot] > 0) {
                totalSlagConsumed += (int) ((float) slagAmounts[slot] / metalAmounts[slot] * consumed);
            }
        }

        for (MetalTypeData.AlloyIngredient ingredient : alloy.alloyFrom()) {
            int slot = getSlotForMetal(ingredient.metalId());
            if (slot == -1) {
                return;
            }
            int consume = ingredient.parts() * multiplier;
            metalAmounts[slot] -= consume;
            if (metalAmounts[slot] + consume > 0) {
                int slagRemove = (int) ((float) consume / (metalAmounts[slot] + consume) * slagAmounts[slot]);
                slagAmounts[slot] = Math.max(0, slagAmounts[slot] - slagRemove);
            }
            if (metalAmounts[slot] <= 0 && slagAmounts[slot] <= 0) {
                metalTypeIds[slot] = null;
                metalAmounts[slot] = 0;
                slagAmounts[slot] = 0;
            }
        }

        int alloySlot = getSlotForMetal(alloy.id());
        if (alloySlot == -1) alloySlot = getFirstFreeSlot();
        if (alloySlot == -1) {
            compactSlots();
            markFluidDirty();
            return;
        }

        metalTypeIds[alloySlot] = alloy.id();
        metalAmounts[alloySlot] += totalConsumed;
        slagAmounts[alloySlot] += totalSlagConsumed;

        compactSlots();
        markFluidDirty();
    }

    private void tickFluidDamage() {
        Direction facing = getCachedState().get(SmelterBlock.FACING);
        Direction right = facing.rotateYCounterclockwise();

        BlockPos p1 = cornerMin.offset(right, 1).offset(facing.getOpposite(), 1);
        BlockPos p2 = cornerMin.offset(right, structureWidth - 2).offset(facing.getOpposite(), structureDepth - 2);

        float fluidHeight = getTotalFillPercent() * maxFillHeight();

        Box fluidBox = new Box(Math.min(p1.getX(), p2.getX()), p1.getY(), Math.min(p1.getZ(), p2.getZ()), Math.max(p1.getX(), p2.getX()) + 1, p1.getY() + fluidHeight, Math.max(p1.getZ(), p2.getZ()) + 1);

        World world = this.getWorld();
        assert world != null;

        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, fluidBox, LivingEntity::isAlive)) {
            entity.damage(world.getDamageSources().lava(), 4.0f);
            entity.setOnFireFor(5);
        }

        int temperatureDecrease = 0;
        int[] fluxPerSlot = new int[MAX_METALS];

        for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, fluidBox.expand(0, 0.5, 0), e -> true)) {
            boolean wasFlux = false;

            if (getTotalSlag() > 0) {
                Identifier itemId = Registries.ITEM.getId(item.getStack().getItem());
                for (int i = 0; i < MAX_METALS; i++) {
                    if (metalTypeIds[i] == null || slagAmounts[i] <= 0) continue;
                    Identifier fluxId = SmelterData.getFluxItemId(metalTypeIds[i]);
                    if (fluxId != null && fluxId.equals(itemId)) {
                        fluxPerSlot[i] += item.getStack().getCount();
                        item.discard();
                        wasFlux = true;
                        break;
                    }
                }
            }
            if (!wasFlux && !item.isFireImmune()) {
                ((ServerWorld) world).playSound(null, item.getX(), item.getY(), item.getZ(), SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 1.0f, 0.9F + world.getRandom().nextFloat() * 0.15F, this.getWorld().getRandom().nextLong());
                if (item.getStack().isIn(TagInit.COOLING_ITEMS)) {
                    temperatureDecrease += ITEM_COOLING_TEMPERATURE;
                }
                item.discard();
            }
        }

        for (int i = 0; i < MAX_METALS; i++) {
            if (fluxPerSlot[i] > 0 && slagAmounts[i] > 0) {
                int convert = Math.min(slagAmounts[i], fluxPerSlot[i] * FLUX_CONVERSION_RATE);
                slagAmounts[i] -= convert;
                metalAmounts[i] += convert;
                markFluidDirty();
                markFluidDirty();

                ((ServerWorld) world).playSound(null, cornerMin.getX() + structureWidth / 2.0, cornerMin.getY() + 0.5, cornerMin.getZ() + structureDepth / 2.0, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0f, 0.8f + world.getRandom().nextFloat() * 0.4f, world.getRandom().nextLong());
            }
        }

        if (this.temperature > MINIMUM_TEMPERATURE && temperatureDecrease > 0) {
            this.temperature -= temperatureDecrease;
            this.temperature = Math.max(this.temperature, MINIMUM_TEMPERATURE);
            markDirty();
        }
    }

    public boolean validateStructure() {
        World world = this.getWorld();
        if (world == null) {
            return false;
        }
        Direction facing = world.getBlockState(this.pos).get(SmelterBlock.FACING);
        Direction left = facing.rotateYClockwise();

        SmelterTypeData detectingType = null;
        if (!SmelterData.getAllTypes().isEmpty()) {
            Identifier blockId = Registries.BLOCK.getId(this.getCachedState().getBlock());
            detectingType = SmelterData.getSmelterTypeForBlock(blockId);
        }

        for (int w = ConfigInit.CONFIG.smelterMaxWidth; w >= 5; w -= 2) {
            for (int d = ConfigInit.CONFIG.smelterMaxWidth; d >= 5; d--) {
                for (int h = ConfigInit.CONFIG.smelterMaxHeight; h >= 1; h--) {
                    int halfWidth = (w - 1) / 2;
                    BlockPos corner = this.pos.offset(left, halfWidth);
                    if (tryValidate(world, corner, facing, w, d, h, detectingType)) {
                        this.cornerMin = corner;
                        this.structureWidth = w;
                        this.structureDepth = d;
                        this.structureHeight = h;
                        this.currentSmelterTypeId = detectingType != null ? detectingType.id() : null;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryValidate(World world, BlockPos corner, Direction facing, int w, int d, int h, @Nullable SmelterTypeData detectingType) {
        return tryCheckBottom(world, corner, facing, w, d, detectingType) && tryCheckWalls(world, corner, facing, w, d, h, detectingType);
    }

    private boolean tryCheckBottom(World world, BlockPos corner, Direction facing, int w, int d, @Nullable SmelterTypeData detectingType) {
        Direction right = facing.rotateYCounterclockwise();
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                BlockPos check = corner.down().offset(right, x).offset(facing.getOpposite(), z);
                if (!world.getBlockState(check).isIn(TagInit.SMELTER_BLOCKS)) {
                    return false;
                } else if (detectingType != null) {
                    if (!detectingType.matchesBlock(Registries.BLOCK.getId(world.getBlockState(check).getBlock()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean tryCheckWalls(World world, BlockPos corner, Direction facing, int w, int d, int h, @Nullable SmelterTypeData detectingType) {
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
                    if (isWall && !state.isIn(TagInit.SMELTER_BLOCKS)) {
                        return false;
                    }
                    if (!isWall && !state.isAir() && !state.isOf(Blocks.LIGHT)) {
                        return false;
                    }
                    if (isWall && detectingType != null) {
                        if (!detectingType.matchesBlock(Registries.BLOCK.getId(world.getBlockState(check).getBlock()))) {
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

    public void onStructureDestroyed() {
        clearLightBlocks();
        for (int i = 0; i < MAX_METALS; i++) {
            metalAmounts[i] = 0;
            slagAmounts[i] = 0;
            metalTypeIds[i] = null;
        }
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
            smeltProgress[slot - 1] = 0;
            smeltTotal[slot - 1] = 0;
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
        if (this.getWorld() != null) {
            BlockState state = this.getWorld().getBlockState(pos);
            this.getWorld().updateListeners(pos, state, state, 3);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
        return this.createNbt(registryLookup);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.cupellation.smelter");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SmelterScreenHandler(syncId, playerInventory, this, this.propertyDelegate, this.pos);
    }

    @Override
    public SmelterScreenPacket getScreenOpeningData(ServerPlayerEntity player) {
        return new SmelterScreenPacket(this.pos);
    }

    public boolean isFormed() {
        return isFormed;
    }

    public int getTotalMoltenMetal() {
        int total = 0;
        for (int amount : metalAmounts) total += amount;
        return total;
    }

    public int getTotalSlag() {
        int total = 0;
        for (int amount : slagAmounts) total += amount;
        return total;
    }

    public int getMoltenMetal(int slot) {
        return metalAmounts[slot];
    }

    public int getSlag(int slot) {
        return slagAmounts[slot];
    }

    public Identifier getMetalTypeId(int slot) {
        return metalTypeIds[slot];
    }

    public int getSlotForMetal(Identifier metalTypeId) {
        for (int i = 0; i < MAX_METALS; i++) {
            if (metalTypeId.equals(metalTypeIds[i])) return i;
        }
        return -1;
    }

    public int getFirstFreeSlot() {
        for (int i = 0; i < MAX_METALS; i++) {
            if (metalTypeIds[i] == null) return i;
        }
        return -1;
    }

    public Set<Identifier> getPresentMetals() {
        Set<Identifier> result = new HashSet<>();
        for (Identifier id : metalTypeIds) {
            if (id != null) result.add(id);
        }
        return result;
    }

    private void compactSlots() {
        int write = 0;
        for (int read = 0; read < MAX_METALS; read++) {
            if (metalTypeIds[read] != null) {
                metalTypeIds[write] = metalTypeIds[read];
                metalAmounts[write] = metalAmounts[read];
                slagAmounts[write] = slagAmounts[read];
                write++;
            }
        }
        for (int i = write; i < MAX_METALS; i++) {
            metalTypeIds[i] = null;
            metalAmounts[i] = 0;
            slagAmounts[i] = 0;
        }
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

    public boolean isBurning() {
        return fuelTime > 0;
    }

    public int getMaxCapacity() {
        int innerW = Math.max(1, structureWidth - 2);
        int innerD = Math.max(1, structureDepth - 2);
        return innerW * innerD * structureHeight * 1000;
    }

    public int getTotalFluid() {
        return getTotalMoltenMetal() + getTotalSlag();
    }

    public float getFillPercent() {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) getTotalMoltenMetal() / cap : 0f;
    }

    public float maxFillHeight() {
        return (structureHeight - 1) + 0.8f;
    }

    public float getTotalFillPercent() {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) getTotalFluid() / cap : 0f;
    }

    public float getSlagFillPercent() {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) getTotalSlag() / cap : 0f;
    }

    public float getMetalFillPercent(int slot) {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) metalAmounts[slot] / cap : 0f;
    }

    public float getSlagFillPercent(int slot) {
        int cap = getMaxCapacity();
        return cap > 0 ? (float) slagAmounts[slot] / cap : 0f;
    }

    private boolean hasAnyInput() {
        for (int i = 1; i <= 3; i++) {
            if (!inventory.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void setRedstonePowered(boolean powered) {
        this.redstonePowered = powered;
        markDirty();
    }

    public boolean isRedstonePowered() {
        return redstonePowered;
    }

    private void updateLightBlocks() {
        if (world == null || world.isClient()) {
            return;
        }
        Direction facing = getCachedState().get(SmelterBlock.FACING);
        Direction right = facing.rotateYCounterclockwise();

        int filledLayers = calcFilledLayers();
        int innerW = structureWidth - 2;
        int innerD = structureDepth - 2;

        for (int y = 0; y < structureHeight; y++) {
            for (int x = 0; x < innerW; x++) {
                for (int z = 0; z < innerD; z++) {
                    BlockPos lightPos = cornerMin.offset(right, 1 + x).offset(facing.getOpposite(), 1 + z).up(y);

                    BlockState current = world.getBlockState(lightPos);
                    boolean shouldHaveLight = y < filledLayers;

                    if (shouldHaveLight && (current.isAir() || current.isOf(Blocks.LIGHT))) {
                        world.setBlockState(lightPos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 15), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                    } else if (!shouldHaveLight && current.isOf(Blocks.LIGHT)) {
                        world.setBlockState(lightPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
    }

    private int calcFilledLayers() {
        if (getTotalFluid() <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(getTotalFillPercent() * maxFillHeight()));
    }

    private void clearLightBlocks() {
        if (world == null || world.isClient()) {
            return;
        }
        Direction facing = getCachedState().get(SmelterBlock.FACING);
        Direction right = facing.rotateYCounterclockwise();

        int innerW = Math.max(1, structureWidth - 2);
        int innerD = Math.max(1, structureDepth - 2);

        for (int y = 0; y < structureHeight; y++) {
            for (int x = 0; x < innerW; x++) {
                for (int z = 0; z < innerD; z++) {
                    BlockPos lightPos = cornerMin.offset(right, 1 + x).offset(facing.getOpposite(), 1 + z).up(y);

                    if (world.getBlockState(lightPos).isOf(Blocks.LIGHT)) {
                        world.setBlockState(lightPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
    }

    public void markFluidDirty() {
        fluidDirty = true;
        markDirty();
    }

    public void applyFluidSync(SmelterFluidSyncPacket packet) {
        for (int i = 0; i < MAX_METALS; i++) {
            SmelterFluidSyncPacket.FluidEntry entry = packet.entries().get(i);
            metalTypeIds[i] = entry.metalTypeId();
            metalAmounts[i] = entry.metalAmount();
            slagAmounts[i] = entry.slagAmount();
        }
    }

    public int getDensestMetalSlot() {
        int best = -1;
        int bestDensity = -1;
        for (int i = 0; i < MAX_METALS; i++) {
            if (metalTypeIds[i] != null && metalAmounts[i] > 0) {
                int density = SmelterData.getDensity(metalTypeIds[i]);
                if (density > bestDensity) {
                    bestDensity = density;
                    best = i;
                }
            }
        }
        return best;
    }

    @Nullable
    public Identifier getPourableMetalType() {
        int slot = getDensestMetalSlot();
        return slot >= 0 ? metalTypeIds[slot] : null;
    }

    public DrainResult drainMoltenMetal(int amount) {
        int slot = getDensestMetalSlot();
        if (slot == -1) {
            return new DrainResult(0, null);
        }
        Identifier drainedType = metalTypeIds[slot];
        int actual = Math.min(amount, metalAmounts[slot]);
        metalAmounts[slot] -= actual;

        if (metalAmounts[slot] <= 0 && slagAmounts[slot] <= 0) {
            metalTypeIds[slot] = null;
            metalAmounts[slot] = 0;
            compactSlots();
        }
        markFluidDirty();
        return new DrainResult(actual, drainedType);
    }

    public int[] getSlotsSortedByDensity() {
        return IntStream.range(0, MAX_METALS).filter(i -> metalTypeIds[i] != null && (metalAmounts[i] > 0 || slagAmounts[i] > 0)).boxed().sorted((a, b) -> {
            int da = SmelterData.getDensity(metalTypeIds[a]);
            int db = SmelterData.getDensity(metalTypeIds[b]);
            return Integer.compare(db, da);
        }).mapToInt(Integer::intValue).toArray();
    }

    public record DrainResult(int amount, @Nullable Identifier metalTypeId) {
    }
}