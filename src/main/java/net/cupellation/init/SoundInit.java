package net.cupellation.init;

import net.cupellation.CupellationMain;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class SoundInit {

    public static SoundEvent MOLTEN_EVENT = register("molten");
    public static SoundEvent CASTING_EVENT = register("casting");

    private static SoundEvent register(String id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(CupellationMain.identifierOf(id)));
    }

    public static void init() {
    }
}
