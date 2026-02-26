package net.cupellation.block.entity;

import net.cupellation.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SmelterFaucetEntity extends BlockEntity {

    private BlockPos linkedSmelterPos = null;

    private Identifier metalTypeId = null;

    public SmelterFaucetEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SMELTER_FAUCET_ENTITY, pos, state);
    }

    public void link(BlockPos smelterPos, Identifier metalTypeId) {
        this.linkedSmelterPos = smelterPos;
        this.metalTypeId = metalTypeId;
        markDirty();
    }

    public void unlink() {
        this.linkedSmelterPos = null;
        this.metalTypeId = null;
        markDirty();
    }

    public Identifier getMetalTypeId() {
        return metalTypeId;
    }

    public BlockPos getLinkedSmelterPos() {
        return linkedSmelterPos;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        metalTypeId = nbt.contains("metalTypeId") && !nbt.getString("metalTypeId").isEmpty() ? Identifier.of(nbt.getString("metalTypeId")) : null;
        linkedSmelterPos = nbt.contains("smelterX") ? new BlockPos(nbt.getInt("smelterX"), nbt.getInt("smelterY"), nbt.getInt("smelterZ")) : null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("metalTypeId", metalTypeId != null ? metalTypeId.toString() : "");
        if (linkedSmelterPos != null) {
            nbt.putInt("smelterX", linkedSmelterPos.getX());
            nbt.putInt("smelterY", linkedSmelterPos.getY());
            nbt.putInt("smelterZ", linkedSmelterPos.getZ());
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
