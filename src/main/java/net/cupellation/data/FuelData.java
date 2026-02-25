package net.cupellation.data;

import net.minecraft.util.Identifier;

public record FuelData(Identifier itemId, int maxTemperature, int burnTime) {
}