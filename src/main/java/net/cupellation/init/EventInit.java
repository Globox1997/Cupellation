package net.cupellation.init;

import net.cupellation.network.CupellationServerPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventInit {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CupellationServerPacket.syncSmelterData(handler.getPlayer());
        });
    }
}
