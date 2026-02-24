package net.cupellation;

import net.cupellation.init.BlockInit;
import net.cupellation.init.ItemInit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class CupellationMain implements ModInitializer {
    @Override
    public void onInitialize() {
        BlockInit.init();
        ItemInit.init();
    }

    public static Identifier identifierOf(String name) {
        return Identifier.of("cupellation", name);
    }
}

// You are LOVED!!!
// Jesus loves you unconditional!
