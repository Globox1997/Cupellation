package net.cupellation.init;

import net.cupellation.CupellationMain;
import net.cupellation.block.*;
import net.cupellation.block.entity.CastingBasinEntity;
import net.cupellation.block.entity.CastingTableEntity;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.block.entity.SmelterFaucetEntity;
import net.cupellation.block.screen.SmelterScreenHandler;
import net.cupellation.network.packet.SmelterScreenPacket;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class BlockInit {

    public static final Block SMELTER = register("smelter", new SmelterBlock(AbstractBlock.Settings.copy(Blocks.FURNACE)));
    public static final Block DEEPSLATE_BRICK_GLASS = register("deepslate_brick_glass", new TransparentBlock(
            AbstractBlock.Settings.create().instrument(NoteBlockInstrument.HAT).strength(0.6F).sounds(BlockSoundGroup.DEEPSLATE).nonOpaque().allowsSpawning(Blocks::never).requiresTool()
                    .solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never)));
    public static final Block DEEPSLATE_BRICK_DRAIN = register(
            "deepslate_brick_drain",
            new SmelterDrain(AbstractBlock.Settings.create().mapColor(MapColor.DEEPSLATE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
                    .strength(3.0F, 6.0F).sounds(BlockSoundGroup.DEEPSLATE)));
    public static final Block DEEPSLATE_BRICK_FAUCET = register(
            "deepslate_brick_faucet",
            new SmelterFaucet(AbstractBlock.Settings.create().mapColor(MapColor.DEEPSLATE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
                    .strength(3.0F, 6.0F).sounds(BlockSoundGroup.DEEPSLATE).nonOpaque()));
    public static final Block DEEPSLATE_BRICK_CASTING_BASIN = register(
            "deepslate_brick_casting_basin",
            new CastingBasin(AbstractBlock.Settings.create().mapColor(MapColor.DEEPSLATE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
                    .strength(3.0F, 6.0F).sounds(BlockSoundGroup.DEEPSLATE)));
    public static final Block DEEPSLATE_BRICK_CASTING_TABLE = register(
            "deepslate_brick_casting_table",
            new CastingTable(AbstractBlock.Settings.create().mapColor(MapColor.DEEPSLATE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
                    .strength(3.0F, 6.0F).sounds(BlockSoundGroup.DEEPSLATE)));

    public static BlockEntityType<SmelterBlockEntity> SMELTER_ENTITY;
    public static BlockEntityType<CastingBasinEntity> CASTING_BASIN_ENTITY;
    public static BlockEntityType<SmelterFaucetEntity> SMELTER_FAUCET_ENTITY;
    public static BlockEntityType<CastingTableEntity> CASTING_TABLE_ENTITY;

    public static final ScreenHandlerType<SmelterScreenHandler> SMELTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, CupellationMain.identifierOf("smelter"),
                    new ExtendedScreenHandlerType<>(SmelterScreenHandler::new, SmelterScreenPacket.CODEC));

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
        CASTING_BASIN_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, CupellationMain.identifierOf("casting_basin"),
                BlockEntityType.Builder.create(CastingBasinEntity::new, DEEPSLATE_BRICK_CASTING_BASIN).build(null));
        SMELTER_FAUCET_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, CupellationMain.identifierOf("smelter_faucet"),
                BlockEntityType.Builder.create(SmelterFaucetEntity::new, DEEPSLATE_BRICK_FAUCET).build(null));
        CASTING_TABLE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, CupellationMain.identifierOf("casting_table"),
                BlockEntityType.Builder.create(CastingTableEntity::new, DEEPSLATE_BRICK_CASTING_TABLE).build(null));
    }
}
