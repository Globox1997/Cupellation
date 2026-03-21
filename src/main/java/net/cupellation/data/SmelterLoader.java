package net.cupellation.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cupellation.CupellationMain;
import net.cupellation.misc.GradeRange;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SmelterLoader implements SimpleSynchronousResourceReloadListener {

    public static final Logger LOGGER = LogManager.getLogger("Cupellation");

    @Override
    public Identifier getFabricId() {
        return CupellationMain.identifierOf("smelter_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        Map<Identifier, SmelterItemData> items = new HashMap<>();
        Map<Identifier, MetalTypeData> metals = new HashMap<>();
        Map<Identifier, FuelData> fuels = new HashMap<>();
        Map<Identifier, SmelterTypeData> types = new HashMap<>();

        resourceManager.findResources("smelter/items", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        List<SmelterItemData> resolved = parseItem(json, resourceManager);
                        if (resolved.isEmpty()) {
                            return;
                        }
                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();
                        for (SmelterItemData data : resolved) {
                            if (items.containsKey(data.itemId())) {
                                if (replace) items.put(data.itemId(), data);
                            } else {
                                items.put(data.itemId(), data);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load item file {}: {}", id, e.toString());
                    }
                });

        resourceManager.findResources("smelter/metals", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        MetalTypeData data = parseMetal(json);
                        if (data == null) {
                            return;
                        }
                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();

                        if (metals.containsKey(data.id())) {
                            if (replace) {
                                metals.put(data.id(), data);
                                // LOGGER.info("[Smelter] Replaced metal type: {} (from {})", data.id(), id);
                            } else {
                                // LOGGER.info("[Smelter] Skipped duplicate metal type: {} (from {}), use replace=true to override", data.id(), id);
                            }
                        } else {
                            metals.put(data.id(), data);
                            // LOGGER.info("[Smelter] Loaded metal type: {} (from {})", data.id(), id);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load metal file {}: {}", id, e.toString());
                    }
                });

        resourceManager.findResources("smelter/fuels", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        List<FuelData> resolved = parseFuel(json, resourceManager);
                        if (resolved.isEmpty()) {
                            return;
                        }
                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();
                        for (FuelData data : resolved) {
                            if (fuels.containsKey(data.itemId())) {
                                if (replace) {
                                    fuels.put(data.itemId(), data);
                                    // LOGGER.info("[Smelter] Replaced fuel: {} (from {})", data.itemId(), id);
                                } else {
                                    // LOGGER.info("[Smelter] Skipped duplicate fuel: {} (from {})", data.itemId(), id);
                                }
                            } else {
                                fuels.put(data.itemId(), data);
                                // LOGGER.info("[Smelter] Loaded fuel: {} (from {})", data.itemId(), id);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load fuel file {}: {}", id, e.toString());
                    }
                });

        resourceManager.findResources("smelter/types", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        SmelterTypeData data = parseSmelterType(json, resourceManager);
                        if (data == null) {
                            return;
                        }
                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();
                        if (types.containsKey(data.id())) {
                            if (replace) types.put(data.id(), data);
                        } else {
                            types.put(data.id(), data);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load type file {}: {}", id, e.toString());
                    }
                });
        SmelterData.setFuels(fuels);
        SmelterData.setItems(items);
        SmelterData.setMetals(metals);
        SmelterData.setTypes(types);

        LOGGER.info("[Smelter] Loaded {} item(s), {} metal type(s).", items.size(), metals.size());
    }

    private List<FuelData> parseFuel(JsonObject json, ResourceManager resourceManager) {
        if (!json.has("max_temperature")) {
            LOGGER.warn("[Smelter] Fuel JSON missing 'max_temperature', skipping.");
            return List.of();
        }
        if (!json.has("item")) {
            LOGGER.warn("[Smelter] Fuel JSON missing 'item', skipping.");
            return List.of();
        }

        int maxTemp = json.get("max_temperature").getAsInt();
        int burnTime = json.has("burn_time") ? json.get("burn_time").getAsInt() : -1;
        String entry = json.get("item").getAsString();

        if (entry.startsWith("#")) {
            List<Identifier> resolved = resolveItemTag(Identifier.of(entry.substring(1)), resourceManager);
            if (resolved.isEmpty()) {
                LOGGER.warn("[Smelter] Fuel tag {} could not be resolved or is empty.", entry);
                return List.of();
            }
            return resolved.stream().map(itemId -> new FuelData(itemId, maxTemp, burnTime)).toList();
        } else {
            Identifier itemId = Identifier.of(entry);
            if (!Registries.ITEM.containsId(itemId)) {
                LOGGER.warn("[Smelter] Fuel references unknown item: {}, skipping.", itemId);
                return List.of();
            }
            return List.of(new FuelData(itemId, maxTemp, burnTime));
        }
    }

    private List<SmelterItemData> parseItem(JsonObject json, ResourceManager resourceManager) {
        if (!json.has("item") || !json.has("metal_type") || !json.has("smelt_time") || !json.has("yield")) {
            LOGGER.warn("[Smelter] Item JSON missing required fields, skipping.");
            return List.of();
        }

        Identifier metalType = Identifier.of(json.get("metal_type").getAsString());
        int smeltTime = json.get("smelt_time").getAsInt();
        int yield = json.get("yield").getAsInt();
        String entry = json.get("item").getAsString();

        if (entry.startsWith("#")) {
            List<Identifier> resolved = resolveItemTag(Identifier.of(entry.substring(1)), resourceManager);
            if (resolved.isEmpty()) {
                LOGGER.warn("[Smelter] Item tag {} could not be resolved or is empty.", entry);
                return List.of();
            }
            return resolved.stream().map(itemId -> new SmelterItemData(itemId, metalType, smeltTime, yield)).toList();
        } else {
            Identifier itemId = Identifier.of(entry);
            if (!Registries.ITEM.containsId(itemId)) {
                LOGGER.warn("[Smelter] Item JSON references unknown item: {}, skipping.", entry);
                return List.of();
            }
            return List.of(new SmelterItemData(itemId, metalType, smeltTime, yield));
        }
    }

    private MetalTypeData parseMetal(JsonObject json) {
        if (!json.has("id") || !json.has("name") || !json.has("required_temp") || !json.has("color")
                || !json.has("cooled_color") || !json.has("texture")) {
            LOGGER.warn("[Smelter] Metal JSON missing required fields, skipping.");
            return null;
        }

        int density = json.has("density") ? json.get("density").getAsInt() : 1;

        List<MetalTypeData.AlloyIngredient> alloyFrom = new ArrayList<>();
        if (json.has("alloy_from")) {
            for (JsonElement jsonElement : json.getAsJsonArray("alloy_from")) {
                JsonObject entry = jsonElement.getAsJsonObject();
                alloyFrom.add(new MetalTypeData.AlloyIngredient(Identifier.of(entry.get("metal").getAsString()), entry.get("parts").getAsInt()));
            }
        }
        Identifier ingotId = null;
        if (json.has("ingot")) {
            ingotId = Identifier.of(json.get("ingot").getAsString());
            if (!Registries.ITEM.containsId(ingotId)) {
                LOGGER.warn("[Smelter] Metal JSON references unknown item: {}, skipping.", json.get("ingot").getAsString());
                ingotId = null;
            }
        }

        Identifier blockId = null;
        if (json.has("block")) {
            blockId = Identifier.of(json.get("block").getAsString());
            if (!Registries.ITEM.containsId(blockId)) {
                LOGGER.warn("[Smelter] Metal JSON references unknown block: {}, skipping.", json.get("block").getAsString());
                blockId = null;
            }
        }

        Identifier fluxItemId = null;
        if (json.has("flux_item")) {
            fluxItemId = Identifier.of(json.get("flux_item").getAsString());
            if (!Registries.ITEM.containsId(fluxItemId)) {
                LOGGER.warn("[Smelter] Metal JSON references unknown flux item: {}, skipping.", json.get("flux_item").getAsString());
                fluxItemId = null;
            }
        }

        GradeRange low = null, mid = null, high = null;
        if (json.has("grades")) {
            JsonObject grades = json.getAsJsonObject("grades");
            if (grades.has("low")) {
                low = parseGradeRange(grades.getAsJsonObject("low"));
            }
            if (grades.has("mid")) {
                mid = parseGradeRange(grades.getAsJsonObject("mid"));
            }
            if (grades.has("high")) {
                high = parseGradeRange(grades.getAsJsonObject("high"));
            }
        }

        return new MetalTypeData(Identifier.of(json.get("id").getAsString()), json.get("name").getAsString(), json.get("required_temp").getAsInt(),
                parseColor(json.get("color").getAsString()), parseColor(json.get("cooled_color").getAsString()), Identifier.of(json.get("texture").getAsString()),
                ingotId, blockId, density, alloyFrom, fluxItemId, low, mid, high);
    }

    private SmelterTypeData parseSmelterType(JsonObject json, ResourceManager resourceManager) {
        if (!json.has("id")) {
            LOGGER.warn("[Smelter] Type JSON missing 'id', skipping.");
            return null;
        }
        Identifier id = Identifier.of(json.get("id").getAsString());

        List<Identifier> blocks = new ArrayList<>();
        if (json.has("blocks")) {
            for (JsonElement el : json.getAsJsonArray("blocks")) {
                String entry = el.getAsString();
                if (entry.startsWith("#")) {
                    blocks.addAll(resolveBlockTag(Identifier.of(entry.substring(1)), resourceManager));
                } else {
                    Identifier bid = Identifier.of(entry);
                    if (!Registries.BLOCK.containsId(bid)) {
                        LOGGER.warn("[Smelter] Type {} references unknown block {}, skipping block.", id, bid);
                        continue;
                    }
                    blocks.add(bid);
                }
            }
        }
        if (blocks.isEmpty()) {
            LOGGER.warn("[Smelter] Type {} has no valid blocks, skipping.", id);
            return null;
        }

        Set<Identifier> allowedMetals = null;
        if (json.has("allowed_metals")) {
            allowedMetals = new HashSet<>();
            for (JsonElement el : json.getAsJsonArray("allowed_metals")) {
                allowedMetals.add(Identifier.of(el.getAsString()));
            }
        }

        return new SmelterTypeData(id, List.copyOf(blocks), allowedMetals);
    }

    private GradeRange parseGradeRange(JsonObject json) {
        return new GradeRange(json.get("min").getAsInt(), json.get("max").getAsInt());
    }

    private int parseColor(String hex) {
        return (int) Long.parseLong(hex.replace("#", ""), 16);
    }

    private List<Identifier> resolveItemTag(Identifier tagId, ResourceManager resourceManager) {
        return resolveItemTag(tagId, resourceManager, new HashSet<>());
    }


    private List<Identifier> resolveItemTag(Identifier tagId, ResourceManager resourceManager, Set<Identifier> visited) {
        if (!visited.add(tagId)) {
            LOGGER.warn("[Smelter] Circular tag reference detected: {}", tagId);
            return List.of();
        }

        Identifier fileId = Identifier.of(tagId.getNamespace(), "tags/item/" + tagId.getPath() + ".json");

        List<Identifier> result = new ArrayList<>();
        Collection<Resource> resources;

        try {
            resources = resourceManager.getAllResources(fileId);
        } catch (Exception e) {
            LOGGER.warn("[Smelter] Could not find tag file for {}: {}", tagId, e.toString());
            return List.of();
        }

        for (Resource resource : resources) {
            try (InputStream stream = resource.getInputStream()) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                if (json.has("replace") && json.get("replace").getAsBoolean()) {
                    result.clear();
                }

                if (!json.has("values")) {
                    continue;
                }
                JsonArray values = json.getAsJsonArray("values");
                for (JsonElement element : values) {
                    String entry = element.getAsString();

                    if (entry.startsWith("#")) {
                        Identifier nestedTag = Identifier.of(entry.substring(1));
                        result.addAll(resolveItemTag(nestedTag, resourceManager, visited));
                    } else {
                        Identifier itemId = Identifier.of(entry);
                        if (Registries.ITEM.containsId(itemId)) {
                            result.add(itemId);
                        } else {
                            LOGGER.warn("[Smelter] Tag {} references unknown item {}, skipping.", tagId, itemId);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[Smelter] Error reading tag file {} from {}: {}", fileId, resource, e.toString());
            }
        }

        return result;
    }

    private List<Identifier> resolveBlockTag(Identifier tagId, ResourceManager resourceManager) {
        return resolveBlockTag(tagId, resourceManager, new HashSet<>());
    }

    private List<Identifier> resolveBlockTag(Identifier tagId, ResourceManager resourceManager, Set<Identifier> visited) {
        if (!visited.add(tagId)) {
            LOGGER.warn("[Smelter] Circular block tag reference: {}", tagId);
            return List.of();
        }
        Identifier fileId = Identifier.of(tagId.getNamespace(), "tags/block/" + tagId.getPath() + ".json");
        List<Identifier> result = new ArrayList<>();
        Collection<Resource> resources;
        try {
            resources = resourceManager.getAllResources(fileId);
        } catch (Exception e) {
            LOGGER.warn("[Smelter] Could not find block tag {}: {}", tagId, e.toString());
            return List.of();
        }
        for (Resource resource : resources) {
            try (InputStream stream = resource.getInputStream()) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                if (json.has("replace") && json.get("replace").getAsBoolean()) result.clear();
                if (!json.has("values")) continue;
                for (JsonElement el : json.getAsJsonArray("values")) {
                    String entry = el.getAsString();
                    if (entry.startsWith("#")) {
                        result.addAll(resolveBlockTag(Identifier.of(entry.substring(1)), resourceManager, visited));
                    } else {
                        Identifier bid = Identifier.of(entry);
                        if (Registries.BLOCK.containsId(bid)) {
                            result.add(bid);
                        } else {
                            LOGGER.warn("[Smelter] Block tag {} references unknown block {}", tagId, bid);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[Smelter] Error reading block tag file {}: {}", fileId, e.toString());
            }
        }
        return result;
    }
}