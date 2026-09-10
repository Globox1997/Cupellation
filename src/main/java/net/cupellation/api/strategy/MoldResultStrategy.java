package net.cupellation.api.strategy;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface MoldResultStrategy {

    @Nullable
    Identifier resolveResultId(Identifier castingMetalId, String outputSuffix);

    boolean matches(Identifier itemId, String outputSuffix);
}