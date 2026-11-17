package net.cupellation.block.entity;

import net.cupellation.init.BlockInit;
import net.cupellation.init.ItemInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlagBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlagBlockEntity extends BlockEntity {

    private final List<Identifier> layers = new ArrayList<>();

    public SlagBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SLAG_ENTITY, pos, state);
    }

    public List<Identifier> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public void setLayers(List<Identifier> newLayers) {
        layers.clear();
        layers.addAll(newLayers);
        sync();
    }

    @Nullable
    public Identifier getTopMetal() {
        return layers.isEmpty() ? null : layers.get(layers.size() - 1);
    }

    private void sync() {
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        List<Identifier> metals = components.getOrDefault(ItemInit.SLAG_METALS, List.of());
        if (metals.isEmpty()) {
            return;
        }
        BlockState state = getCachedState();
        if (state.getBlock() instanceof SlagBlock) {
            int target = state.get(SlagBlock.LAYERS);
            for (Identifier metal : metals) {
                if (layers.size() >= target) {
                    break;
                }
                layers.add(metal);
            }
        } else {
            layers.clear();
            layers.addAll(metals);
        }
        sync();
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(ItemInit.SLAG_METALS, List.copyOf(layers));
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove("layers");
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        layers.clear();
        NbtList list = nbt.getList("layers", NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            Identifier id = Identifier.tryParse(list.getString(i));
            if (id != null) {
                layers.add(id);
            }
        }
        if (world != null && world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        NbtList list = new NbtList();
        for (Identifier layer : layers) {
            list.add(NbtString.of(layer.toString()));
        }
        nbt.put("layers", list);
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
