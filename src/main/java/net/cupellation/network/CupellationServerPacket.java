package net.cupellation.network;

import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.data.SmelterItemData;
import net.cupellation.network.packet.SmelterPacket;
import net.cupellation.network.packet.SmelterScreenPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class CupellationServerPacket {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(SmelterPacket.PACKET_ID, SmelterPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SmelterScreenPacket.PACKET_ID, SmelterScreenPacket.CODEC);
    }

    public static void syncSmelterData(ServerPlayerEntity player) {
        List<SmelterItemData> items = new ArrayList<>(SmelterData.allItems());
        List<MetalTypeData> metals = new ArrayList<>(SmelterData.allMetals());
        ServerPlayNetworking.send(player, new SmelterPacket(items, metals));
    }

}
