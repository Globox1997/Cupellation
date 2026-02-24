package net.cupellation;

import net.cupellation.init.RenderInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CupellationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RenderInit.init();
    }
}
