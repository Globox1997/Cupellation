package net.cupellation.data;

import net.minecraft.util.Identifier;

import java.util.List;

public record SmelterItemData(Identifier itemId, Identifier metalTypeId, int smeltTime, int yield, boolean scaleWithDurability, List<Byproduct> byproducts) {

    public record Byproduct(Identifier metalTypeId, int yield) {
    }

    public int getTotalYield() {
        int total = yield;
        for (Byproduct byproduct : byproducts) {
            total += byproduct.yield();
        }
        return total;
    }
}
