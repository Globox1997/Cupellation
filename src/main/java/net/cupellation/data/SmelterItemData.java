package net.cupellation.data;

import net.minecraft.util.Identifier;

public record SmelterItemData(Identifier itemId, Identifier metalTypeId, int smeltTime, int yield) {
}
