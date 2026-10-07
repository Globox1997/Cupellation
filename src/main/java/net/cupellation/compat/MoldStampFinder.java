package net.cupellation.compat;

import net.cupellation.api.MoldType;
import net.cupellation.api.strategy.MoldResultStrategy;
import net.cupellation.api.strategy.SuffixMoldResultStrategy;
import net.cupellation.data.SmelterData;
import net.cupellation.init.ItemInit;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MoldStampFinder {

    public static final Map<String, String> METAL_NAME_OVERRIDES = Map.of("gold", "golden");

    private MoldStampFinder() {
    }

    public static List<Item> findStamps(MoldType moldType) {
        Set<Item> stamps = new LinkedHashSet<>();

        for (Item moldable : ItemInit.MOLDABLES) {
            Identifier moldableId = Registries.ITEM.getId(moldable);
            if (moldableId.getPath().endsWith("_" + moldType.suffix())) {
                stamps.add(moldable);
            }
        }

        MoldResultStrategy strategy = moldType.strategy() != null ? moldType.strategy() : new SuffixMoldResultStrategy();
        for (Identifier itemId : Registries.ITEM.getIds()) {
            if (strategy.matches(itemId, moldType.suffix())) {
                stamps.add(Registries.ITEM.get(itemId));
            }
        }

        for (Identifier metalTypeId : SmelterData.getAllMetals().keySet()) {
            Identifier candidate = suffixedItemId(metalTypeId, moldType.suffix());
            if (candidate != null) {
                stamps.add(Registries.ITEM.get(candidate));
            }
        }

        for (Identifier stampId : moldType.stampItemIds()) {
            if (Registries.ITEM.containsId(stampId)) {
                stamps.add(Registries.ITEM.get(stampId));
            }
        }

        return new ArrayList<>(stamps);
    }

    private static Identifier suffixedItemId(Identifier metalId, String suffix) {
        String raw = metalId.getPath();
        String override = METAL_NAME_OVERRIDES.get(raw);
        List<String> names = override != null ? List.of(override, raw) : List.of(raw);

        for (String name : names) {
            String itemPath = name + "_" + suffix;

            Identifier sameNamespace = Identifier.of(metalId.getNamespace(), itemPath);
            if (Registries.ITEM.containsId(sameNamespace)) {
                return sameNamespace;
            }
            Identifier vanilla = Identifier.ofVanilla(itemPath);
            if (Registries.ITEM.containsId(vanilla)) {
                return vanilla;
            }
        }
        return null;
    }
}
