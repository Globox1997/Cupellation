package net.cupellation.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "cupellation")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class CupellationConfig implements ConfigData {

//    @ConfigEntry.Gui.RequiresRestart
//    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
//    public int necromancer_spawn_weight = 1;
}