package net.cupellation.init;

import net.cupellation.CupellationMain;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;

public class CompatInit {

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("earlystage")) {
            ResourceManagerHelper.registerBuiltinResourcePack(CupellationMain.identifierOf("earlystage_cupellation_compat"), FabricLoader.getInstance().getModContainer("cupellation").orElseThrow(),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        }
    }
}
