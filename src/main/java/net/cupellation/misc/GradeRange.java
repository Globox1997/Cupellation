package net.cupellation.misc;

public record GradeRange(int min, int max) {

    public boolean contains(int temperature) {
        return temperature >= min && temperature <= max;
    }
}
