package net.cupellation.api;

import net.cupellation.api.strategy.MoldResultStrategy;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public record MoldType(String suffix, int mb, boolean extraOutput, Set<Identifier> blacklist, @Nullable Identifier moldingMetalTypeId, @Nullable MoldResultStrategy strategy,
                       boolean craftableAsClayMold, Set<Identifier> stampItemIds) {

    public MoldType(String suffix, int mb, boolean extraOutput, Set<Identifier> blacklist) {
        this(suffix, mb, extraOutput, blacklist, null, null, extraOutput, Set.of());
    }

    public static Builder builder(String suffix, int mb) {
        return new Builder(suffix, mb);
    }

    public static final class Builder {
        private final String suffix;
        private final int mb;
        private boolean extraOutput = false;
        private Set<Identifier> blacklist = Set.of();
        private Identifier moldingMetalTypeId;
        private MoldResultStrategy strategy;
        private boolean craftableAsClayMold = false;
        private Set<Identifier> stampItemIds = Set.of();

        private Builder(String suffix, int mb) {
            this.suffix = suffix;
            this.mb = mb;
        }

        public Builder extraOutput(boolean value) {
            this.extraOutput = value;
            this.craftableAsClayMold = value || this.craftableAsClayMold;
            return this;
        }

        public Builder blacklist(Set<Identifier> blacklist) {
            this.blacklist = blacklist;
            return this;
        }

        public Builder moldingMetalTypeId(Identifier id) {
            this.moldingMetalTypeId = id;
            return this;
        }

        public Builder strategy(MoldResultStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder craftableAsClayMold(Set<Identifier> stampItemIds) {
            this.craftableAsClayMold = true;
            this.stampItemIds = Set.copyOf(stampItemIds);
            return this;
        }

        public Builder craftableAsClayMold(Identifier stampItemId) {
            return craftableAsClayMold(Set.of(stampItemId));
        }

        public MoldType build() {
            if (extraOutput && !stampItemIds.isEmpty()) {
                throw new IllegalStateException("extraOutput and fixed stampItemIds are mutually exclusive for suffix: " + suffix);
            }
            return new MoldType(suffix, mb, extraOutput, blacklist, moldingMetalTypeId, strategy, craftableAsClayMold, stampItemIds);
        }
    }
}