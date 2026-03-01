package net.cupellation.data;

import net.cupellation.misc.GradeRange;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record MetalTypeData(Identifier id, String name, int requiredTemp, int color, int cooledColor, Identifier texture, @Nullable Identifier ingotId, @Nullable Identifier blockId, int density,
                            List<AlloyIngredient> alloyFrom, @Nullable Identifier fluxItemId, @Nullable GradeRange lowGrade, @Nullable GradeRange midGrade, @Nullable GradeRange highGrade) {

    public record AlloyIngredient(Identifier metalId, int parts) {
    }

    public enum Grade {LOW, MID, HIGH}

    public boolean isAlloy() {
        return alloyFrom != null && !alloyFrom.isEmpty();
    }

    public Set<Identifier> alloyComponents() {
        if (!isAlloy()) {
            return Set.of();
        }
        return alloyFrom.stream().map(AlloyIngredient::metalId).collect(Collectors.toSet());
    }

    public int calcAlloyMultiplier(Map<Identifier, Integer> amounts) {
        if (!isAlloy()) {
            return -1;
        }
        int minMultiplier = Integer.MAX_VALUE;
        for (AlloyIngredient ingredient : alloyFrom) {
            Integer available = amounts.get(ingredient.metalId());
            if (available == null || available <= 0) {
                return -1;
            }
            int multiplier = available / ingredient.parts();
            if (multiplier <= 0) {
                return -1;
            }
            minMultiplier = Math.min(minMultiplier, multiplier);
        }
        return minMultiplier == Integer.MAX_VALUE ? -1 : minMultiplier;
    }

    public Grade getGradeAt(int temperature) {
        if (highGrade != null && highGrade.contains(temperature)) {
            return Grade.HIGH;
        }
        if (midGrade != null && midGrade.contains(temperature)) {
            return Grade.MID;
        }
        return Grade.LOW;
    }

    public int getMaxGradeTemperature() {
        if (highGrade != null) {
            return highGrade.max();
        }
        if (midGrade != null) {
            return midGrade.max();
        }
        if (lowGrade != null) {
            return lowGrade.max();
        }
        return 0;
    }

    public int getMinGradeTemperature() {
        if (lowGrade != null) {
            return lowGrade.min();
        }
        if (midGrade != null) {
            return midGrade.min();
        }
        if (highGrade != null) {
            return highGrade.min();
        }
        return 0;
    }

    public boolean hasGrades() {
        return lowGrade != null || midGrade != null || highGrade != null;
    }
}