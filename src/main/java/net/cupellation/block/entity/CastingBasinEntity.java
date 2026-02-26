package net.cupellation.block.entity;

import net.cupellation.block.SmelterFaucet;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CastingBasinEntity extends BlockEntity {

    public static final int CAPACITY = 1296;

    public static final int FILL_RATE = 20;

    public static final int COOL_TIME = 200;

    private int moltenAmount = 0;
    private Identifier metalTypeId = null;
    private int cooldownTicks = 0;
    private boolean cooled = false;

    private BlockPos linkedSmelterPos = null;
    private boolean filling = false;


    public CastingBasinEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CASTING_BASIN_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CastingBasinEntity basin) {
        basin.serverTick(world);
    }

    private void serverTick(World world) {
        if (filling && linkedSmelterPos != null && !cooled) {


            if (moltenAmount >= CAPACITY) {
                startCooling();
                stopFilling(world);
                return;
            }

            BlockEntity be = world.getBlockEntity(linkedSmelterPos);
            if (!(be instanceof SmelterBlockEntity smelter)) {
                stopFilling(world);
                if (moltenAmount > 0) startCooling();
                return;
            }

            if (smelter.getMoltenMetal() <= 0) {
                stopFilling(world);
                if (moltenAmount > 0) startCooling();
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

    private void startCooling() {
        filling = false;
        cooldownTicks = COOL_TIME;
        markDirty();
    }

    public ItemStack tryExtract() {
        if (!cooled || moltenAmount < CAPACITY || metalTypeId == null) {
            return ItemStack.EMPTY;
        }
        MetalTypeData metal = SmelterData.getMetalType(metalTypeId);
        if (metal == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = getResultBlock(metal);
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        moltenAmount = 0;
        metalTypeId = null;
        cooled = false;
        cooldownTicks = 0;
        markDirty();

        return result;
    }

    private ItemStack getResultBlock(MetalTypeData metal) {
        return switch (metal.id().toString()) {
            case "cupellation:iron" -> new ItemStack(Items.IRON_BLOCK);
            case "cupellation:gold" -> new ItemStack(Items.GOLD_BLOCK);
            case "cupellation:copper" -> new ItemStack(Items.COPPER_BLOCK);
            default -> ItemStack.EMPTY;
        };
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
        return createNbt(registryLookup);
    }
}
