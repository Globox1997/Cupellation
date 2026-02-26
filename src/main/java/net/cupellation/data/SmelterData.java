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
    private static final Map<Identifier, FuelData> FUELS = new HashMap<>();

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

    public static void setFuels(Map<Identifier, FuelData> fuels) {
        FUELS.clear();
        FUELS.putAll(fuels);
    }

    public static boolean hasItem(Item item) {
        return getItemData(item) != null;
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

    public static int getCooledColor(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.cooledColor() : 0xFF4500;
    }

    public static Identifier getTexture(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.texture() : Identifier.of("cupellation", "fluid/molten_iron");
    }

    public static Identifier getIngotId(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.ingotId() : Identifier.of("minecraft", "iron_ingot");
    }

    public static Identifier getBlockId(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.blockId() : Identifier.of("minecraft", "iron_block");
    }

    public static String getName(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? Text.translatable(metal.name()).getString() : "Unknown";
    }

    public static MetalTypeData.Grade getGradeAt(Identifier metalTypeId, int temperature) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.getGradeAt(temperature) : MetalTypeData.Grade.LOW;
    }

    @Nullable
    public static FuelData getFuelData(Item item) {
        return FUELS.get(Registries.ITEM.getId(item));
    }

    public static int getFuelMaxTemp(Item item) {
        FuelData data = getFuelData(item);
        return data != null ? data.maxTemperature() : -1;
    }

    public static boolean isSmelterFuel(Item item) {
        return FUELS.containsKey(Registries.ITEM.getId(item));
    }

    public static Collection<SmelterItemData> allItems() {
        return Collections.unmodifiableCollection(ITEMS.values());
    }

    public static Collection<MetalTypeData> allMetals() {
        return Collections.unmodifiableCollection(METALS.values());
    }

    public static Collection<FuelData> allFuels() {
        return Collections.unmodifiableCollection(FUELS.values());
    }
}
