package net.cupellation.network.packet;

import net.cupellation.CupellationMain;
import net.cupellation.data.FuelData;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterItemData;
import net.cupellation.misc.GradeRange;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SmelterPacket(List<SmelterItemData> items, List<MetalTypeData> metals, List<FuelData> fuels) implements CustomPayload {

    public static final CustomPayload.Id<SmelterPacket> PACKET_ID = new CustomPayload.Id<>(CupellationMain.identifierOf("smelter_packet"));

    public static final PacketCodec<RegistryByteBuf, SmelterPacket> CODEC = PacketCodec.of(SmelterPacket::write, SmelterPacket::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

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
            buf.writeInt(metal.cooledColor());
            buf.writeIdentifier(metal.texture());
            buf.writeBoolean(metal.ingotId() != null);
            if (metal.ingotId() != null) {
                buf.writeIdentifier(metal.ingotId());
            }
            buf.writeBoolean(metal.blockId() != null);
            if (metal.blockId() != null) {
                buf.writeIdentifier(metal.blockId());
            }
            buf.writeInt(metal.density());

            List<MetalTypeData.AlloyIngredient> alloyFrom = metal.alloyFrom();
            buf.writeInt(alloyFrom != null ? alloyFrom.size() : 0);
            if (alloyFrom != null) {
                for (MetalTypeData.AlloyIngredient ingredient : alloyFrom) {
                    buf.writeIdentifier(ingredient.metalId());
                    buf.writeInt(ingredient.parts());
                }
            }

            buf.writeBoolean(metal.fluxItemId() != null);
            if (metal.fluxItemId() != null) {
                buf.writeIdentifier(metal.fluxItemId());
            }

            writeNullableGradeRange(buf, metal.lowGrade());
            writeNullableGradeRange(buf, metal.midGrade());
            writeNullableGradeRange(buf, metal.highGrade());
        }

        buf.writeInt(fuels.size());
        for (FuelData fuel : fuels) {
            buf.writeIdentifier(fuel.itemId());
            buf.writeInt(fuel.maxTemperature());
            buf.writeInt(fuel.burnTime());
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
            var id = buf.readIdentifier();
            var name = buf.readString();
            int requiredTemp = buf.readInt();
            int color = buf.readInt();
            int cooledColor = buf.readInt();
            var texture = buf.readIdentifier();
            boolean hasIngot = buf.readBoolean();
            var ingotId = hasIngot ? buf.readIdentifier() : null;
            boolean hasBlock = buf.readBoolean();
            var blockId = hasBlock ? buf.readIdentifier() : null;
            int density = buf.readInt();

            int alloyCount = buf.readInt();
            List<MetalTypeData.AlloyIngredient> alloyFrom = new ArrayList<>(alloyCount);
            for (int j = 0; j < alloyCount; j++) {
                alloyFrom.add(new MetalTypeData.AlloyIngredient(buf.readIdentifier(), buf.readInt()));
            }

            boolean hasFlux = buf.readBoolean();
            var fluxItemId = hasFlux ? buf.readIdentifier() : null;

            var lowGrade = readNullableGradeRange(buf);
            var midGrade = readNullableGradeRange(buf);
            var highGrade = readNullableGradeRange(buf);

            metals.add(new MetalTypeData(id, name, requiredTemp, color, cooledColor, texture, ingotId, blockId, density, alloyFrom, fluxItemId, lowGrade, midGrade, highGrade));
        }

        int fuelCount = buf.readInt();
        List<FuelData> fuels = new ArrayList<>(fuelCount);
        for (int i = 0; i < fuelCount; i++) {
            fuels.add(new FuelData(buf.readIdentifier(), buf.readInt(), buf.readInt()));
        }

        return new SmelterPacket(items, metals, fuels);
    }

    private static void writeNullableGradeRange(RegistryByteBuf buf, GradeRange range) {
        buf.writeBoolean(range != null);
        if (range != null) {
            buf.writeInt(range.min());
            buf.writeInt(range.max());
        }
    }

    @Nullable
    private static GradeRange readNullableGradeRange(RegistryByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) {
            return null;
        }
        return new GradeRange(buf.readInt(), buf.readInt());
    }
}