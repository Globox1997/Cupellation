package net.cupellation.network.packet;

import net.cupellation.CupellationMain;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterItemData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record SmelterPacket(List<SmelterItemData> items, List<MetalTypeData> metals) implements CustomPayload {

    public static final CustomPayload.Id<SmelterPacket> PACKET_ID = new CustomPayload.Id<>(CupellationMain.identifierOf("smelter_packet"));

    public static final PacketCodec<RegistryByteBuf, SmelterPacket> CODEC = PacketCodec.of(SmelterPacket::write, SmelterPacket::read);

    private void write(RegistryByteBuf buf) {
        buf.writeInt(items.size());
        for (SmelterItemData data : items) {
            buf.writeIdentifier(data.itemId());
            buf.writeIdentifier(data.metalTypeId());
            buf.writeInt(data.smeltTime());
            buf.writeInt(data.yield());
        }

        buf.writeInt(metals.size());
        for (MetalTypeData metal : metals) {
            buf.writeIdentifier(metal.id());
            buf.writeString(metal.name());
            buf.writeInt(metal.requiredTemp());
            buf.writeInt(metal.color());
            buf.writeIdentifier(metal.texture());
        }
    }

    private static SmelterPacket read(RegistryByteBuf buf) {
        int itemCount = buf.readInt();
        List<SmelterItemData> items = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            items.add(new SmelterItemData(buf.readIdentifier(), buf.readIdentifier(), buf.readInt(), buf.readInt()));
        }

        int metalCount = buf.readInt();
        List<MetalTypeData> metals = new ArrayList<>(metalCount);
        for (int i = 0; i < metalCount; i++) {
            metals.add(new MetalTypeData(buf.readIdentifier(), buf.readString(), buf.readInt(), buf.readInt(), buf.readIdentifier()));
        }

        return new SmelterPacket(items, metals);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
