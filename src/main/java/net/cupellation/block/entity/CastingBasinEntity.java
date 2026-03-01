package net.cupellation.block.entity;

import net.cupellation.block.SmelterFaucet;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ItemInit;
import net.cupellation.init.SoundInit;
import net.cupellation.misc.CastingEntity;
import net.cupellation.misc.MoltenHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

public class CastingBasinEntity extends BlockEntity implements CastingEntity {

    public static final int CAPACITY = 1296;

    public static final int FILL_RATE = 20;

    public static final int COOL_TIME = 200;

    private int moltenAmount = 0;
    private Identifier metalTypeId = null;
    private int cooldownTicks = 0;
    private boolean cooled = false;

    private int cachedGrade = 1;
    private BlockPos linkedSmelterPos = null;
    private boolean filling = false;

    @Nullable
    private SoundInstance casting = null;

    public CastingBasinEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CASTING_BASIN_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CastingBasinEntity blockEntity) {
        blockEntity.clientTick();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CastingBasinEntity blockEntity) {
        blockEntity.serverTick(world);
    }

    private void clientTick() {
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            if (this.cooldownTicks == 0) {
                this.cooled = true;
            }
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
        if (filling && linkedSmelterPos != null && !cooled) {
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

            if (smelter.getTotalMoltenMetal() <= 0) {
                stopFilling(world);
                if (moltenAmount > 0) {
                    startCooling();
                }
                return;
            }

            Identifier smelterMetal = smelter.getPourableMetalType();
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
            SmelterBlockEntity.DrainResult drain = smelter.drainMoltenMetal(toFill);
            int actual = drain.amount();

            if (actual <= 0) {
                stopFilling(world);
                if (moltenAmount > 0) startCooling();
                return;
            }

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
                cooled = true;
                markDirty();
            }
        }
    }

    @Override
    public boolean startFilling(BlockPos smelterPos, Identifier metalType) {
        if (cooled) {
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
        return 21;
    }

    private void startCooling() {
        filling = false;
        cooldownTicks = COOL_TIME;

        if (linkedSmelterPos != null && metalTypeId != null && this.getWorld() != null) {
            cachedGrade = MoltenHelper.getGrade(this.getWorld(), linkedSmelterPos, metalTypeId);
        }
        markDirty();
    }

    public ItemStack tryExtract() {
        if (!cooled || moltenAmount < CAPACITY || metalTypeId == null) {
            return ItemStack.EMPTY;
        }
        MetalTypeData metal = SmelterData.getMetalType(metalTypeId);
        if (metal == null || metal.blockId() == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(Registries.BLOCK.get(metal.blockId()).asItem());
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        result.set(ItemInit.QUALITY_GRADE, cachedGrade);
        moltenAmount = 0;
        metalTypeId = null;
        cooled = false;
        cooldownTicks = 0;
        cachedGrade = 1;
        markDirty();

        return result;
    }

    public int getMoltenAmount() {
        return moltenAmount;
    }

    public Identifier getMetalTypeId() {
        return metalTypeId;
    }

    public boolean isCooled() {
        return cooled;
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

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        moltenAmount = nbt.getInt("moltenAmount");
        cooldownTicks = nbt.getInt("cooldownTicks");
        cooled = nbt.getBoolean("cooled");
        filling = nbt.getBoolean("filling");
        cachedGrade = nbt.getInt("cachedGrade");

        if (nbt.contains("metalTypeId") && !nbt.getString("metalTypeId").isEmpty()) {
            metalTypeId = Identifier.of(nbt.getString("metalTypeId"));
        } else {
            metalTypeId = null;
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
        nbt.putBoolean("cooled", cooled);
        nbt.putBoolean("filling", filling);
        nbt.putInt("cachedGrade", cachedGrade);
        nbt.putString("metalTypeId", metalTypeId != null ? metalTypeId.toString() : "");

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
