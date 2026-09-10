package net.cupellation.api;

import net.cupellation.api.strategy.MappedResultMoldStrategy;
import net.cupellation.api.strategy.SuffixMoldResultStrategy;
import net.cupellation.init.BlockInit;
import net.cupellation.item.MoldItem;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CupellationAPI {

    private static final List<SmelterType> SMELTER_TYPES = new ArrayList<>();
    private static final List<MoldType> MOLD_TYPES = new ArrayList<>();
    private static final Map<MoldType, MoldItem> MOLD_ITEMS = new LinkedHashMap<>();

    private CupellationAPI() {
    }

    public static void registerSmelterType(SmelterType type) {
        SMELTER_TYPES.add(type);
    }

    public static List<SmelterType> getSmelterTypes() {
        return List.copyOf(SMELTER_TYPES);
    }

    public static void registerMoldType(MoldType moldType) {
        if (moldType.extraOutput() && !(moldType.strategy() == null || moldType.strategy() instanceof SuffixMoldResultStrategy)) {
            throw new IllegalArgumentException("extraOutput=true is only supported with the default suffix strategy: " + moldType.suffix());
        }
        MOLD_TYPES.add(moldType);
    }

    public static List<MoldType> getMoldTypes() {
        return List.copyOf(MOLD_TYPES);
    }

    public static void registerMoldItem(MoldType moldType, MoldItem item) {
        MOLD_ITEMS.put(moldType, item);
    }

    @Nullable
    public static MoldItem getMoldItem(MoldType moldType) {
        return MOLD_ITEMS.get(moldType);
    }

    public static Map<MoldType, MoldItem> getMoldItems() {
        return Map.copyOf(MOLD_ITEMS);
    }

    @Nullable
    public static MoldItem getMoldItemBySuffix(String suffix) {
        return MOLD_TYPES.stream().filter(mt -> mt.suffix().equals(suffix)).findFirst().map(MOLD_ITEMS::get).orElse(null);
    }

    public static void registerDefaultSmelterTypes() {
        registerSmelterType(new SmelterType(BlockInit.DEEPSLATE_BRICK_SMELTER, BlockInit.DEEPSLATE_BRICK_FAUCET, BlockInit.DEEPSLATE_BRICK_CASTING_BASIN, BlockInit.DEEPSLATE_BRICK_CASTING_TABLE));
        registerSmelterType(new SmelterType(BlockInit.RED_NETHER_BRICK_SMELTER, BlockInit.RED_NETHER_BRICK_FAUCET, BlockInit.RED_NETHER_BRICK_CASTING_BASIN, BlockInit.RED_NETHER_BRICK_CASTING_TABLE));
    }

    public static void registerDefaultMoldTypes() {
        registerMoldType(new MoldType("ingot", 144, false, Set.of()));
        registerMoldType(new MoldType("axe_head", 432, true, Set.of()));
        registerMoldType(new MoldType("hoe_head", 288, true, Set.of()));
        registerMoldType(new MoldType("pickaxe_head", 432, true, Set.of()));
        registerMoldType(new MoldType("shovel_head", 144, true, Set.of()));
        registerMoldType(new MoldType("sword_blade", 288, true, Set.of()));
        registerMoldType(new MoldType("helmet", 720, false, Set.of()));
        registerMoldType(new MoldType("chestplate", 1152, false, Set.of()));
        registerMoldType(new MoldType("leggings", 1008, false, Set.of()));
        registerMoldType(new MoldType("boots", 576, false, Set.of()));

        // Example for new custom diamond mold
        //        CupellationAPI.registerMoldType(
        //                MoldType.builder("diamond", 144)
        //                        .moldingMetalTypeId(Identifier.of("cupellation", "gold"))
        //                        .strategy(new MappedResultMoldStrategy(Map.of(Identifier.of("cupellation", "diamond"), Identifier.of("minecraft", "diamond"))))
        //                        .craftableAsClayMold(Set.of(Identifier.of("minecraft", "diamond")))
        //                        .build());
    }
}