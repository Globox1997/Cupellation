package net.cupellation.init;

import net.cupellation.CupellationMain;
import net.cupellation.block.SmelterBlock;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.block.screen.SmelterScreenHandler;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class BlockInit {

    public static final Block SMELTER = register("smelter", new SmelterBlock(AbstractBlock.Settings.copy(Blocks.FURNACE)));

    public static BlockEntityType<SmelterBlockEntity> SMELTER_ENTITY;

    public static final ScreenHandlerType<SmelterScreenHandler> SMELTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, CupellationMain.identifierOf("smelter"), new ScreenHandlerType<>(SmelterScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    private static Block register(String id, Block block) {
        return register(CupellationMain.identifierOf(id), block);
    }

    private static Block register(Identifier id, Block block) {
        Item item = Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        ItemGroupEvents.modifyEntriesEvent(ItemInit.CUPELLATION_ITEM_GROUP).register(entries -> entries.add(item));

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void init() {
        SMELTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, CupellationMain.identifierOf("smelter"),
                BlockEntityType.Builder.create(SmelterBlockEntity::new, SMELTER).build(null));
    }
}
