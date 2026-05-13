package net.cupellation.api;

import java.util.ArrayList;
import java.util.List;

public final class CupellationAPI {

    private static final List<SmelterType> SMELTER_TYPES = new ArrayList<>();

    private CupellationAPI() {
    }

    public static void registerSmelterType(SmelterType type) {
        SMELTER_TYPES.add(type);
    }

    public static List<SmelterType> getSmelterTypes() {
        return List.copyOf(SMELTER_TYPES);
    }
}