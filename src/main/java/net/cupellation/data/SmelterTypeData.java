package net.cupellation.data;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;


public record SmelterTypeData(Identifier id, List<Identifier> blocks, Set<Identifier> allowedMetals) {

    public boolean allowsMetal(Identifier metalTypeId) {
        return allowedMetals == null || allowedMetals.contains(metalTypeId);
    }

    public boolean matchesBlock(Identifier blockId) {
        return blocks.contains(blockId);
    }
}