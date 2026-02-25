package net.cupellation.network;

import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.data.SmelterItemData;
import net.cupellation.network.packet.SmelterPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CupellationClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SmelterPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> {
                    java.util.Map<Identifier, SmelterItemData> itemMap = new java.util.HashMap<>();
                    payload.items().forEach(d -> itemMap.put(d.itemId(), d));

                    java.util.Map<Identifier, MetalTypeData> metalMap = new java.util.HashMap<>();
                    payload.metals().forEach(m -> metalMap.put(m.id(), m));

                    SmelterData.setItems(itemMap);
                    SmelterData.setMetals(metalMap);
                })
        );
    }
}
