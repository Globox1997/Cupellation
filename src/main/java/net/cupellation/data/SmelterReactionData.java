package net.cupellation.data;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

public record SmelterReactionData(Identifier itemId, Identifier fromMetal, State fromState, Identifier toMetal, State toState, int amountPerItem, int minTemperature,
                                  @Nullable Set<Identifier> smelterTypes) {

    public enum State {
        METAL, SLAG;

        @Nullable
        public static State parse(String s) {
            try {
                return valueOf(s.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
