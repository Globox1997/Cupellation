package net.cupellation.network.packet;

import net.cupellation.CupellationMain;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SmelterFluidSyncPacket(BlockPos pos, List<FluidEntry> entries) implements CustomPayload {

    public record FluidEntry(@Nullable Identifier metalTypeId, int metalAmount, int slagAmount) {
    }

    public static final CustomPayload.Id<SmelterFluidSyncPacket> PACKET_ID =
            new CustomPayload.Id<>(CupellationMain.identifierOf("smelter_fluid_sync"));

    public static final PacketCodec<RegistryByteBuf, SmelterFluidSyncPacket> CODEC = PacketCodec.of(
            (packet, buf) -> {
                buf.writeBlockPos(packet.pos());
                buf.writeInt(packet.entries().size());
                for (FluidEntry entry : packet.entries()) {
                    buf.writeBoolean(entry.metalTypeId() != null);
                    if (entry.metalTypeId() != null) {
                        buf.writeString(entry.metalTypeId().toString());
                    }
                    buf.writeInt(entry.metalAmount());
                    buf.writeInt(entry.slagAmount());
                }
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                int size = buf.readInt();
                List<FluidEntry> entries = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    boolean hasId = buf.readBoolean();
                    Identifier id = hasId ? Identifier.of(buf.readString()) : null;
                    int metal = buf.readInt();
                    int slag = buf.readInt();
                    entries.add(new FluidEntry(id, metal, slag));
                }
                return new SmelterFluidSyncPacket(pos, entries);
            }
    );

    public static SmelterFluidSyncPacket from(SmelterBlockEntity blockEntity) {
        List<FluidEntry> entries = new ArrayList<>();
        for (int i = 0; i < SmelterBlockEntity.MAX_METALS; i++) {
            entries.add(new FluidEntry(blockEntity.getMetalTypeId(i), blockEntity.getMoltenMetal(i), blockEntity.getSlag(i)));
        }
        return new SmelterFluidSyncPacket(blockEntity.getPos(), entries);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}