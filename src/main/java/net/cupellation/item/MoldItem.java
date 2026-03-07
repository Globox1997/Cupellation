package net.cupellation.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MoldItem extends Item {

    private static final Map<String, MoldItem> SUFFIX_TO_MOLD = new HashMap<>();
    private static final Map<Identifier, Map<String, Identifier>> RESOLVE_CACHE = new HashMap<>();
    private static final Map<String, String> METAL_NAME_OVERRIDES = Map.of(
            "gold", "golden"
    );

    @Nullable
    private final Identifier moldingMetalTypeId;
    private final int mb;
    private final String outputSuffix;
    private final Set<Identifier> blacklist;

    public MoldItem(@Nullable Identifier moldingMetalTypeId, int mb, String outputSuffix, Set<Identifier> blacklist, Settings settings) {
        super(settings);
        this.moldingMetalTypeId = moldingMetalTypeId;
        this.mb = mb;
        this.outputSuffix = outputSuffix;
        this.blacklist = blacklist;
        SUFFIX_TO_MOLD.put(outputSuffix, this);
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
        return Registries.ITEM.containsId(resolveResultId(metalTypeId));
    }

    @Nullable
    public Identifier resolveResultId(Identifier castingMetalId) {
        return RESOLVE_CACHE
                .computeIfAbsent(castingMetalId, k -> new HashMap<>())
                .computeIfAbsent(outputSuffix, k -> computeResultId(castingMetalId));
    }

    @Nullable
    private Identifier computeResultId(Identifier castingMetalId) {
        String metalName = METAL_NAME_OVERRIDES.getOrDefault(castingMetalId.getPath(), castingMetalId.getPath());
        String itemPath = metalName + "_" + outputSuffix;

        Identifier sameNamespace = Identifier.of(castingMetalId.getNamespace(), itemPath);
        if (Registries.ITEM.containsId(sameNamespace)) {
            return sameNamespace;
        }
        Identifier vanilla = Identifier.ofVanilla(itemPath);
        if (Registries.ITEM.containsId(vanilla)) {
            return vanilla;
        }
        for (Identifier id : Registries.ITEM.getIds()) {
            if (id.getPath().equals(itemPath)) {
                return id;
            }
        }

        return null;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    @Nullable
    public static MoldItem findMoldForItem(Identifier itemId) {
        for (Map.Entry<String, MoldItem> entry : SUFFIX_TO_MOLD.entrySet()) {
            if (itemId.getPath().endsWith("_" + entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}