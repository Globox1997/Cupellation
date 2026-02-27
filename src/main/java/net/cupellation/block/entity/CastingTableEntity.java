package net.cupellation.block.entity;

import net.cupellation.block.SmelterFaucet;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ItemInit;
import net.cupellation.init.SoundInit;
import net.cupellation.misc.CastingEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CastingTableEntity extends BlockEntity implements CastingEntity {

    public static final int CAPACITY = 144;

    public static final int FILL_RATE = 5;

    public static final int COOL_TIME = 200;

    private int moltenAmount = 0;
    private Identifier metalTypeId = null;
    private int cooldownTicks = 0;

    private ItemStack result = ItemStack.EMPTY;
    private ItemStack mold = ItemStack.EMPTY;

    private BlockPos linkedSmelterPos = null;
    private boolean filling = false;

    @Nullable
    private SoundInstance casting = null;

    public CastingTableEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CASTING_TABLE_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CastingTableEntity blockEntity) {
        blockEntity.clientTick();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CastingTableEntity blockEntity) {
        blockEntity.serverTick(world);
    }

    private void clientTick() {
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.filling) {
            if (this.casting == null) {
                this.casting = new PositionedSoundInstance(SoundInit.CASTING_EVENT, SoundCategory.BLOCKS, 1.0f, 0.9F + world.getRandom().nextFloat() * 0.15F, world.getRandom(), pos.getX(), pos.getY(), pos.getZ());
                client.getSoundManager().play(this.casting);
            }
        } else if (this.casting != null && client.getSoundManager().isPlaying(this.casting)) {
            client.getSoundManager().stop(this.casting);
            this.casting = null;
        }
    }

    private void serverTick(World world) {
        if (filling) {
            if (result.isEmpty() && mold.isEmpty()) {
                stopFilling(world);
                return;
            }
            if (!result.isEmpty() && !mold.isEmpty()) {
                stopFilling(world);
                return;
            }
        }

        if (filling && linkedSmelterPos != null) {
            if (moltenAmount >= CAPACITY) {
                startCooling();
                stopFilling(world);
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(linkedSmelterPos);
            if (!(blockEntity instanceof SmelterBlockEntity smelter)) {
                stopFilling(world);
                if (moltenAmount > 0) {
                    startCooling();
                }
                return;
            }

            if (smelter.getMoltenMetal() <= 0) {
                stopFilling(world);
                if (moltenAmount > 0) {
                    startCooling();
                }
                return;
            }

            Identifier smelterMetal = smelter.getMetalTypeId();
            if (smelterMetal == null) {
                stopFilling(world);
                return;
            }
            if (metalTypeId == null) {
                metalTypeId = smelterMetal;
            } else if (!metalTypeId.equals(smelterMetal)) {
                stopFilling(world);
                return;
            }

            int toFill = Math.min(FILL_RATE, CAPACITY - moltenAmount);
            int available = smelter.getMoltenMetal();
            int actual = Math.min(toFill, available);

            smelter.drainMoltenMetal(actual);
            int smelterTemp = smelter.getTemperature();
            MetalTypeData metalData = SmelterData.getMetalType(metalTypeId);
            if (metalData != null && smelterTemp > metalData.getMaxGradeTemperature()) {
                int overheat = smelterTemp - metalData.getMaxGradeTemperature();
                int evaporate = Math.max(1, overheat / 10);
                actual = Math.max(1, actual - evaporate);

                ((ServerWorld) world).spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.2 + world.random.nextDouble() * 0.6, pos.getY() + 1.1,
                        pos.getZ() + 0.2 + world.random.nextDouble() * 0.6, 3, 0.1, 0.05, 0.1, 0.02);
                if (this.getWorld().getRandom().nextInt(20) == 0) {
                    ((ServerWorld) world).playSound(null, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 1.0f, 1.0f, this.getWorld().getRandom().nextLong());
                }
            }
            moltenAmount += actual;
            markDirty();
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            if (cooldownTicks == 0) {
                if (result.isEmpty() && !mold.isEmpty()) {
                    result = new ItemStack(Registries.ITEM.get(SmelterData.getIngotId(metalTypeId)));
                } else if (!result.isEmpty() && mold.isEmpty()) {
                    if (ItemInit.MOLDS.containsKey(metalTypeId)) {
                        mold = new ItemStack(ItemInit.MOLDS.get(metalTypeId));
                    }
                }
                moltenAmount = 0;
                metalTypeId = null;
                cooldownTicks = 0;
                markDirty();
            }
        }
    }

    @Override
    public boolean startFilling(BlockPos smelterPos, Identifier metalType) {
        if (result.isEmpty() && mold.isEmpty()) {
            return false;
        }
        if (!result.isEmpty() && !mold.isEmpty()) {
            return false;
        }
        if (cooldownTicks > 0) {
            return false;
        }
        if (moltenAmount >= CAPACITY) {
            return false;
        }
        if (metalTypeId != null && !metalTypeId.equals(metalType)) {
            return false;
        }
        if (mold.isEmpty() && !ItemInit.MOLDS.containsKey(metalTypeId)) {
            return false;
        }
        this.linkedSmelterPos = smelterPos;
        this.filling = true;
        markDirty();
        return true;
    }

    @Override
    public void stopFilling(World world) {
        filling = false;
        linkedSmelterPos = null;

        for (int dy = 1; dy <= 2; dy++) {
            BlockPos faucetPos = pos.up(dy);
            BlockState faucetState = world.getBlockState(faucetPos);
            if (faucetState.getBlock() instanceof SmelterFaucet && faucetState.get(SmelterFaucet.OPEN)) {
                world.setBlockState(faucetPos, faucetState.with(SmelterFaucet.OPEN, false));
                break;
            }
        }

        markDirty();
    }

    @Override
    public int castingDistance() {
        return 10;
    }

    private void startCooling() {
        filling = false;
        cooldownTicks = COOL_TIME;
        markDirty();
    }

    public boolean tryInsertResult(ItemStack stack) {
        if (!result.isEmpty() || filling) {
            return false;
        }
        if (moltenAmount > 0) {
            return false;
        }
        result = stack.copyWithCount(1);
        stack.decrement(1);
        markDirty();
        return true;
    }

    public boolean tryInsertMold(ItemStack stack) {
        if (!mold.isEmpty() || filling) {
            return false;
        }
        if (moltenAmount > 0) {
            return false;
        }
        mold = stack.copyWithCount(1);
        stack.decrement(1);
        markDirty();
        return true;
    }


    public ItemStack tryExtractMold() {
        if (filling || moltenAmount > 0 || mold.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extractingMold = mold.copy();
        mold = ItemStack.EMPTY;

        moltenAmount = 0;
        metalTypeId = null;
        cooldownTicks = 0;
        markDirty();
        return extractingMold;
    }


    public ItemStack tryExtractResult() {
        if (!result.isEmpty() && !filling || moltenAmount > 0) {
            ItemStack out = result.copy();
            result = ItemStack.EMPTY;
            markDirty();
            return out;
        }
        return ItemStack.EMPTY;
    }

    public int getMoltenAmount() {
        return moltenAmount;
    }

    public Identifier getMetalTypeId() {
        return metalTypeId;
    }

    public boolean isFilling() {
        return filling;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public float getFillPercent() {
        return (float) moltenAmount / CAPACITY;
    }

    public ItemStack getResult() {
        return result;
    }

    public boolean hasResult() {
        return !result.isEmpty();
    }

    public ItemStack getMold() {
        return mold;
    }

    public boolean hasMold() {
        return !mold.isEmpty();
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        moltenAmount = nbt.getInt("moltenAmount");
        cooldownTicks = nbt.getInt("cooldownTicks");
        filling = nbt.getBoolean("filling");

        if (nbt.contains("metalTypeId") && !nbt.getString("metalTypeId").isEmpty()) {
            metalTypeId = Identifier.of(nbt.getString("metalTypeId"));
        } else {
            metalTypeId = null;
        }

        if (nbt.contains("result", NbtElement.COMPOUND_TYPE)) {
            result = ItemStack.fromNbt(registryLookup, nbt.getCompound("result")).orElse(ItemStack.EMPTY);
        } else {
            result = ItemStack.EMPTY;
        }

        if (nbt.contains("mold", NbtElement.COMPOUND_TYPE)) {
            mold = ItemStack.fromNbt(registryLookup, nbt.getCompound("mold")).orElse(ItemStack.EMPTY);
        } else {
            mold = ItemStack.EMPTY;
        }

        if (nbt.contains("linkedSmelterX")) {
            linkedSmelterPos = new BlockPos(nbt.getInt("linkedSmelterX"), nbt.getInt("linkedSmelterY"), nbt.getInt("linkedSmelterZ"));
        } else {
            linkedSmelterPos = null;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("moltenAmount", moltenAmount);
        nbt.putInt("cooldownTicks", cooldownTicks);
        nbt.putBoolean("filling", filling);
        nbt.putString("metalTypeId", metalTypeId != null ? metalTypeId.toString() : "");

        if (!result.isEmpty()) {
            NbtCompound ingotNbt = new NbtCompound();
            nbt.put("result", result.encode(registryLookup, ingotNbt));
        }
        if (!mold.isEmpty()) {
            NbtCompound moldNbt = new NbtCompound();
            nbt.put("mold", mold.encode(registryLookup, moldNbt));
        }

        if (linkedSmelterPos != null) {
            nbt.putInt("linkedSmelterX", linkedSmelterPos.getX());
            nbt.putInt("linkedSmelterY", linkedSmelterPos.getY());
            nbt.putInt("linkedSmelterZ", linkedSmelterPos.getZ());
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createNbt(registryLookup);
    }
}