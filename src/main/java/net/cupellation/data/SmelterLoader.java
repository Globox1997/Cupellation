package net.cupellation.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cupellation.CupellationMain;
import net.cupellation.misc.GradeRange;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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

        resourceManager.findResources("smelter/fuels", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        FuelData data = parseFuel(json);
                        if (data == null) return;

                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();

                        if (fuels.containsKey(data.itemId())) {
                            if (replace) {
                                fuels.put(data.itemId(), data);
                                LOGGER.info("[Smelter] Replaced fuel: {} (from {})", data.itemId(), id);
                            } else {
                                LOGGER.info("[Smelter] Skipped duplicate fuel: {} (from {})", data.itemId(), id);
                            }
                        } else {
                            fuels.put(data.itemId(), data);
                            LOGGER.info("[Smelter] Loaded fuel: {} (from {})", data.itemId(), id);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load fuel file {}: {}", id, e.toString());
                    }
                });


        resourceManager.findResources("smelter/items", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        SmelterItemData data = parseItem(json);
                        if (data == null) return;

                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();

                        if (items.containsKey(data.itemId())) {
                            if (replace) {
                                items.put(data.itemId(), data);
                                LOGGER.info("[Smelter] Replaced item: {} (from {})", data.itemId(), id);
                            } else {
                                LOGGER.info("[Smelter] Skipped duplicate item: {} (from {}), use replace=true to override", data.itemId(), id);
                            }
                        } else {
                            items.put(data.itemId(), data);
                            LOGGER.info("[Smelter] Loaded item: {} (from {})", data.itemId(), id);
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
                        if (data == null) return;

                        boolean replace = json.has("replace") && json.get("replace").getAsBoolean();

                        if (metals.containsKey(data.id())) {
                            if (replace) {
                                metals.put(data.id(), data);
                                LOGGER.info("[Smelter] Replaced metal type: {} (from {})", data.id(), id);
                            } else {
                                LOGGER.info("[Smelter] Skipped duplicate metal type: {} (from {}), use replace=true to override", data.id(), id);
                            }
                        } else {
                            metals.put(data.id(), data);
                            LOGGER.info("[Smelter] Loaded metal type: {} (from {})", data.id(), id);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load metal file {}: {}", id, e.toString());
                    }
                });

        SmelterData.setFuels(fuels);
        SmelterData.setItems(items);
        SmelterData.setMetals(metals);

        LOGGER.info("[Smelter] Loaded {} item(s), {} metal type(s).", items.size(), metals.size());
    }

    private FuelData parseFuel(JsonObject json) {
        if (!json.has("item") || !json.has("max_temperature")) {
            LOGGER.warn("[Smelter] Fuel JSON missing required fields, skipping.");
            return null;
        }
        return new FuelData(Identifier.of(json.get("item").getAsString()), json.get("max_temperature").getAsInt());
    }

    private SmelterItemData parseItem(JsonObject json) {
        if (!json.has("item") || !json.has("metal_type") || !json.has("smelt_time") || !json.has("yield")) {
            LOGGER.warn("[Smelter] Item JSON missing required fields, skipping.");
            return null;
        }
        return new SmelterItemData(Identifier.of(json.get("item").getAsString()),
                Identifier.of(json.get("metal_type").getAsString()), json.get("smelt_time").getAsInt(), json.get("yield").getAsInt());
    }

    private MetalTypeData parseMetal(JsonObject json) {
        if (!json.has("id") || !json.has("name") || !json.has("required_temp") || !json.has("color") || !json.has("texture")) {
            LOGGER.warn("[Smelter] Metal JSON missing required fields, skipping.");
            return null;
        }

        GradeRange low = null, mid = null, high = null;
        if (json.has("grades")) {
            JsonObject grades = json.getAsJsonObject("grades");
            if (grades.has("low")) low = parseGradeRange(grades.getAsJsonObject("low"));
            if (grades.has("mid")) mid = parseGradeRange(grades.getAsJsonObject("mid"));
            if (grades.has("high")) high = parseGradeRange(grades.getAsJsonObject("high"));
        }

        return new MetalTypeData(Identifier.of(json.get("id").getAsString()), json.get("name").getAsString(), json.get("required_temp").getAsInt(),
                parseColor(json.get("color").getAsString()), Identifier.of(json.get("texture").getAsString()), low, mid, high);
    }

    private GradeRange parseGradeRange(JsonObject json) {
        return new GradeRange(json.get("min").getAsInt(), json.get("max").getAsInt());
    }

    private int parseColor(String hex) {
        return (int) Long.parseLong(hex.replace("#", ""), 16);
    }
}