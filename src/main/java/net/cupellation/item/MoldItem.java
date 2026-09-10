package net.cupellation.item;

import net.cupellation.api.strategy.MoldResultStrategy;
import net.cupellation.api.strategy.SuffixMoldResultStrategy;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MoldItem extends Item {

    private static final List<MoldItem> REGISTERED_MOLDS = new ArrayList<>();

    @Nullable
    private final Identifier moldingMetalTypeId;
    private final int mb;
    private final String outputSuffix;
    private final Set<Identifier> blacklist;
    private final MoldResultStrategy strategy;

    public MoldItem(@Nullable Identifier moldingMetalTypeId, int mb, String outputSuffix, Set<Identifier> blacklist, @Nullable MoldResultStrategy strategy, Settings settings) {
        super(settings);
        this.moldingMetalTypeId = moldingMetalTypeId;
        this.mb = mb;
        this.outputSuffix = outputSuffix;
        this.blacklist = blacklist;
        this.strategy = strategy != null ? strategy : new SuffixMoldResultStrategy();
        if (moldingMetalTypeId != null) {
            REGISTERED_MOLDS.add(this);
        }
    }

    public MoldItem(@Nullable Identifier moldingMetalTypeId, int mb, String outputSuffix, Set<Identifier> blacklist, Settings settings) {
        this(moldingMetalTypeId, mb, outputSuffix, blacklist, null, settings);
    }

    @Nullable
    public Identifier getMoldingMetalTypeId() {
        return moldingMetalTypeId;
    }

    public int getMb() {
        return mb;
    }

    public boolean canMoldGetCastWith(Identifier metalTypeId) {
        if (moldingMetalTypeId == null) {
            return true;
        }
        return metalTypeId.equals(moldingMetalTypeId);
    }

    public boolean canCastWith(Identifier metalTypeId) {
        if (blacklist.contains(metalTypeId)) {
            return false;
        }

        Identifier resultId = resolveResultId(metalTypeId);
        return resultId != null && Registries.ITEM.containsId(resultId);
    }

    @Nullable
    public Identifier resolveResultId(Identifier castingMetalId) {
        return strategy.resolveResultId(castingMetalId, outputSuffix);
    }

    @Nullable
    public static MoldItem findMoldForItem(Identifier itemId) {
        for (MoldItem mold : REGISTERED_MOLDS) {
            if (mold.strategy.matches(itemId, mold.outputSuffix)) {
                return mold;
            }
        }
        return null;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public boolean isSingleUse() {
        return false;
    }
}