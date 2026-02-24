package net.cupellation.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "cupellation")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class CupellationConfig implements ConfigData {

    public int smelterMaxWidth = 7;
    public int smelterMaxHeight = 8;
}