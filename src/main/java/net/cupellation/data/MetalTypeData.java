package net.cupellation.data;

import net.minecraft.util.Identifier;

public record MetalTypeData(Identifier id, String name, int requiredTemp, int color, Identifier texture) {
}
