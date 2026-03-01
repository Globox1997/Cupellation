package net.cupellation.network;

import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.data.FuelData;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.data.SmelterItemData;
import net.cupellation.network.packet.SmelterFluidSyncPacket;
import net.cupellation.network.packet.SmelterPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CupellationClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SmelterPacket.PACKET_ID, (payload, context) -> context.client().execute(() -> {
                    Map<Identifier, SmelterItemData> itemMap = new HashMap<>();
                    payload.items().forEach(d -> itemMap.put(d.itemId(), d));

                    Map<Identifier, MetalTypeData> metalMap = new HashMap<>();
                    payload.metals().forEach(m -> metalMap.put(m.id(), m));

                    Map<Identifier, FuelData> fuelMap = new HashMap<>();
                    payload.fuels().forEach(m -> fuelMap.put(m.itemId(), m));

                    SmelterData.setItems(itemMap);
                    SmelterData.setMetals(metalMap);
                    SmelterData.setFuels(fuelMap);
                })
        );
        ClientPlayNetworking.registerGlobalReceiver(SmelterFluidSyncPacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientWorld world = context.client().world;
                if (world == null) return;
                if (world.getBlockEntity(payload.pos()) instanceof SmelterBlockEntity blockEntity) {
                    blockEntity.applyFluidSync(payload);
                }
            });
        });
    }
}
