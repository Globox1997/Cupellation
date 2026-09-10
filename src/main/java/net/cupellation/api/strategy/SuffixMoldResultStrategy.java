package net.cupellation.api.strategy;

import net.cupellation.compat.MoldStampFinder;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SuffixMoldResultStrategy implements MoldResultStrategy {

    private final Map<Identifier, Map<String, Identifier>> cache = new HashMap<>();

    @Override
    public Identifier resolveResultId(Identifier castingMetalId, String outputSuffix) {
        return cache.computeIfAbsent(castingMetalId, k -> new HashMap<>())
                .computeIfAbsent(outputSuffix, k -> compute(castingMetalId, outputSuffix));
    }

    @Nullable
    private Identifier compute(Identifier castingMetalId, String outputSuffix) {
        String metalName = MoldStampFinder.METAL_NAME_OVERRIDES.getOrDefault(castingMetalId.getPath(), castingMetalId.getPath());
        String itemPath = metalName + "_" + outputSuffix;

        Identifier sameNamespace = Identifier.of(castingMetalId.getNamespace(), itemPath);
        if (Registries.ITEM.containsId(sameNamespace)) {
            return sameNamespace;
        }
        Identifier vanilla = Identifier.ofVanilla(itemPath);
        if (Registries.ITEM.containsId(vanilla)) {
            return vanilla;
        }
        for (Identifier id : Registries.ITEM.getIds()) {
            if (id.getPath().equals(itemPath)) {
                return id;
            }
        }
        return null;
    }

    @Override
    public boolean matches(Identifier itemId, String outputSuffix) {
        return itemId.getPath().endsWith("_" + outputSuffix);
    }
}