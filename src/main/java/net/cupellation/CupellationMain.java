package net.cupellation;

import net.cupellation.init.*;
import net.cupellation.network.CupellationServerPacket;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class CupellationMain implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigInit.init();
        CompatInit.init();
        LoaderInit.init();
        EventInit.init();
        BlockInit.init();
        ItemInit.init();
        TagInit.init();
        SoundInit.init();
        CupellationServerPacket.init();
    }

    public static Identifier identifierOf(String name) {
        return Identifier.of("cupellation", name);
    }
}

// You are LOVED!!!
// Jesus loves you unconditional!
