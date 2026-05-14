package net.cupellation.api;

import net.cupellation.init.BlockInit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CupellationAPI {

    private static final List<SmelterType> SMELTER_TYPES = new ArrayList<>();
    private static final List<MoldType> MOLD_TYPES = new ArrayList<>();

    private CupellationAPI() {
    }

    public static void registerSmelterType(SmelterType type) {
        SMELTER_TYPES.add(type);
    }

    public static List<SmelterType> getSmelterTypes() {
        return List.copyOf(SMELTER_TYPES);
    }

    public static void registerMoldType(MoldType moldType) {
        MOLD_TYPES.add(moldType);
    }

    public static List<MoldType> getMoldTypes() {
        return List.copyOf(MOLD_TYPES);
    }

    public static void registerDefaultSmelterTypes() {
        registerSmelterType(new SmelterType(BlockInit.DEEPSLATE_BRICK_SMELTER, BlockInit.DEEPSLATE_BRICK_FAUCET, BlockInit.DEEPSLATE_BRICK_CASTING_BASIN, BlockInit.DEEPSLATE_BRICK_CASTING_TABLE));
        registerSmelterType(new SmelterType(BlockInit.RED_NETHER_BRICK_SMELTER, BlockInit.RED_NETHER_BRICK_FAUCET, BlockInit.RED_NETHER_BRICK_CASTING_BASIN, BlockInit.RED_NETHER_BRICK_CASTING_TABLE));
    }

    public static void registerDefaultMoldTypes() {
        registerMoldType(new MoldType("axe_head", 432, true, Set.of()));
        registerMoldType(new MoldType("hoe_head", 288, true, Set.of()));
        registerMoldType(new MoldType("pickaxe_head", 432, true, Set.of()));
        registerMoldType(new MoldType("shovel_head", 144, true, Set.of()));
        registerMoldType(new MoldType("sword_blade", 288, true, Set.of()));
        registerMoldType(new MoldType("helmet", 720, false, Set.of()));
        registerMoldType(new MoldType("chestplate", 1152, false, Set.of()));
        registerMoldType(new MoldType("leggings", 1008, false, Set.of()));
        registerMoldType(new MoldType("boots", 576, false, Set.of()));
    }
}