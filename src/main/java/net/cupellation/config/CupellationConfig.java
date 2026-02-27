package net.cupellation.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "cupellation")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class CupellationConfig implements ConfigData {

    public int smelterMaxWidth = 7;
    public int smelterMaxHeight = 8;

    @Comment("Slag ratio in X%")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int slagRatio = 15;

    @Comment("Reduces durability by X%")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int lowGradeDurability = 50;

    @Comment("Increases durability by X%")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int midGradeDurability = 0;

    @Comment("Increases durability by X%")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int highGradeDurability = 30;
}