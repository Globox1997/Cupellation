package net.cupellation.data;

import net.cupellation.misc.GradeRange;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record MetalTypeData(Identifier id, String name, int requiredTemp, int color, int cooledColor, Identifier texture, Identifier ingotId, Identifier blockId, @Nullable GradeRange lowGrade,
                            @Nullable GradeRange midGrade, @Nullable GradeRange highGrade) {

    public enum Grade {LOW, MID, HIGH}

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
        if (highGrade != null) {
            return highGrade.min();
        }
        if (midGrade != null) {
            return midGrade.min();
        }
        if (lowGrade != null) {
            return lowGrade.min();
        }
        return 0;
    }

    public boolean hasGrades() {
        return lowGrade != null || midGrade != null || highGrade != null;
    }
}
