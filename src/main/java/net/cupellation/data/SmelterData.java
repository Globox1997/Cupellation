package net.cupellation.data;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SmelterData {

    private static final Map<Identifier, SmelterItemData> ITEMS = new HashMap<>();
    private static final Map<Identifier, MetalTypeData> METALS = new HashMap<>();
    private static final Map<Identifier, FuelData> FUELS = new HashMap<>();
    private static final Map<Identifier, SmelterTypeData> TYPES = new HashMap<>();

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

    public static void setTypes(Map<Identifier, SmelterTypeData> types) {
        TYPES.clear();
        TYPES.putAll(types);
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

    @Nullable
    public static Identifier getIngotId(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.ingotId() : null;
    }

    @Nullable
    public static Identifier getBlockId(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.blockId() : null;
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
    public static SmelterTypeData getSmelterTypeForBlock(Identifier blockId) {
        for (SmelterTypeData type : TYPES.values()) {
            if (type.matchesBlock(blockId)) {
                return type;
            }
        }
        return null;
    }

    public static Collection<SmelterTypeData> getAllTypes() {
        return TYPES.values();
    }

    @Nullable
    public static MetalTypeData findAlloyFor(Set<Identifier> metalIds) {
        if (metalIds.isEmpty()) {
            return null;
        }
        Set<Identifier> rawMetals = metalIds.stream().filter(id -> {
            MetalTypeData metal = METALS.get(id);
            return metal != null && !metal.isAlloy();
        }).collect(java.util.stream.Collectors.toSet());

        if (rawMetals.isEmpty()) {
            return null;
        }
        for (MetalTypeData metal : METALS.values()) {
            if (metal.isAlloy() && metal.alloyComponents().equals(rawMetals)) {
                return metal;
            }
        }
        return null;
    }

    public static boolean canAddMetal(Set<Identifier> existing, Identifier newMetal) {
        if (existing.isEmpty()) {
            return true;
        }
        MetalTypeData newMetalData = METALS.get(newMetal);
        if (newMetalData == null) {
            return false;
        }
        if (newMetalData.isAlloy()) {
            return false;
        }
        Set<Identifier> rawExisting = existing.stream().filter(id -> {
            MetalTypeData m = METALS.get(id);
            return m != null && !m.isAlloy();
        }).collect(java.util.stream.Collectors.toSet());

        Set<Identifier> combined = new java.util.HashSet<>(rawExisting);
        combined.add(newMetal);
        return findAlloyFor(combined) != null;
    }

    public static int getDensity(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.density() : 1;
    }

    @Nullable
    public static Identifier getFluxItemId(Identifier metalTypeId) {
        MetalTypeData metal = METALS.get(metalTypeId);
        return metal != null ? metal.fluxItemId() : null;
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
