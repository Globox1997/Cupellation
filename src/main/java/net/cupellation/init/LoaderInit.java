package net.cupellation.init;

import net.cupellation.data.SmelterLoader;
import net.cupellation.network.CupellationServerPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;

public class LoaderInit {

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SmelterLoader());

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                    ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayerList().get(i);

                    CupellationServerPacket.syncSmelterData(serverPlayerEntity);
                }
                SmelterLoader.LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else {
                SmelterLoader.LOGGER.error("Failed to reload on {}", Thread.currentThread());
            }
        });
    }
}
