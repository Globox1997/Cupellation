package net.cupellation.api;

import net.minecraft.util.Identifier;

import java.util.Set;

public record MoldType(String suffix, int mb, boolean extraOutput, Set<Identifier> blacklist) {
}
