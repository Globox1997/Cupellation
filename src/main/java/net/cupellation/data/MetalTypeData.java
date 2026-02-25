package net.cupellation.data;

import net.cupellation.misc.GradeRange;
import net.minecraft.util.Identifier;

public record MetalTypeData(Identifier id, String name, int requiredTemp, int color, Identifier texture, GradeRange lowGrade, GradeRange midGrade, GradeRange highGrade) {

    public enum Grade {LOW, MID, HIGH}

    public Grade getGradeAt(int temperature) {
        if (highGrade != null && highGrade.contains(temperature)) return Grade.HIGH;
        if (midGrade != null && midGrade.contains(temperature)) return Grade.MID;
        return Grade.LOW;
    }

    public boolean hasGrades() {
        return lowGrade != null || midGrade != null || highGrade != null;
    }
}
