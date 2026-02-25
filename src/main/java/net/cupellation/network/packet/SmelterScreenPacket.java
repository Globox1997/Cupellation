package net.cupellation.network.packet;

import net.cupellation.CupellationMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SmelterScreenPacket(BlockPos pos) implements CustomPayload {

    public static final CustomPayload.Id<SmelterScreenPacket> PACKET_ID = new CustomPayload.Id<>(CupellationMain.identifierOf("smelter_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, SmelterScreenPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SmelterScreenPacket::pos, SmelterScreenPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
