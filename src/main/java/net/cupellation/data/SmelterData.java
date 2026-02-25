package net.cupellation.data;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static SmelterItemData getItemData(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return ITEMS.get(id);
    }

    @Nullable
    public static MetalTypeData getMetalType(Identifier metalTypeId) {
        return METALS.get(metalTypeId);
    }

    public static int getRequiredTemp(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.requiredTemp() : 9999;
    }

    public static int getColor(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.color() : 0xFF4500;
    }

    public static Identifier getTexture(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.texture() : Identifier.of("cupellation", "fluid/molten");
    }

    public static String getName(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? Text.translatable(metal.name()).getString() : "Unknown";
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
