package net.cupellation.init;

import net.cupellation.CupellationMain;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TagInit {

    public static final TagKey<Item> COOLING_ITEMS = TagKey.of(RegistryKeys.ITEM, CupellationMain.identifierOf("cooling_items"));

    public static final TagKey<Item> GRADEABLE_ITEMS = TagKey.of(RegistryKeys.ITEM, CupellationMain.identifierOf("gradeable_items"));

    public static final TagKey<Item> CONSUMED_MOLD_TEMPLATES = TagKey.of(RegistryKeys.ITEM, CupellationMain.identifierOf("consumed_mold_templates"));

    public static final TagKey<Block> SMELTER_BLOCKS = TagKey.of(RegistryKeys.BLOCK, CupellationMain.identifierOf("smelter_blocks"));

    public static void init() {
    }
}
