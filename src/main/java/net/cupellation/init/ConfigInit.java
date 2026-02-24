package net.cupellation.init;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.cupellation.config.CupellationConfig;

public class ConfigInit {
    public static CupellationConfig CONFIG = new CupellationConfig();

    public static void init() {
        AutoConfig.register(CupellationConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(CupellationConfig.class).getConfig();
    }

}
