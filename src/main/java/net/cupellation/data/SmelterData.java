package net.cupellation.data;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SmelterData {

    private static final Map<Identifier, SmelterItemData> ITEMS = new HashMap<>();
    private static final Map<Identifier, MetalTypeData> METALS = new HashMap<>();

    private SmelterData() {
    }

    public static void setItems(Map<Identifier, SmelterItemData> items) {
        ITEMS.clear();
        ITEMS.putAll(items);
    }

    public static void setMetals(Map<Identifier, MetalTypeData> metals) {
        METALS.clear();
        METALS.putAll(metals);
    }


    public static SmelterItemData getItemData(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return ITEMS.get(id);
    }

    public static MetalTypeData getMetalType(Identifier metalTypeId) {
        return METALS.get(metalTypeId);
    }

    public static Collection<SmelterItemData> allItems() {
        return Collections.unmodifiableCollection(ITEMS.values());
    }

    public static Collection<MetalTypeData> allMetals() {
        return Collections.unmodifiableCollection(METALS.values());
    }

    public static boolean hasItem(Item item) {
        return getItemData(item) != null;
    }
}
