package net.cupellation.api.strategy;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MappedResultMoldStrategy implements MoldResultStrategy {

    private final Map<Identifier, Identifier> mapping;

    public MappedResultMoldStrategy(Map<Identifier, Identifier> mapping) {
        this.mapping = mapping;
    }

    @Override
    @Nullable
    public Identifier resolveResultId(Identifier castingMetalId, String outputSuffix) {
        return mapping.get(castingMetalId);
    }

    @Override
    public boolean matches(Identifier itemId, String outputSuffix) {
        return mapping.containsValue(itemId);
    }
}