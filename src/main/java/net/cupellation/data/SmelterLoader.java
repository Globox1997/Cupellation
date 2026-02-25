package net.cupellation.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cupellation.CupellationMain;
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

        resourceManager.findResources("smelter/items", id -> id.getPath().endsWith(".json"))
                .forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                        SmelterItemData data = parseItem(json);
                        if (data != null) {
                            items.put(data.itemId(), data);
                            LOGGER.info("[Smelter] Loaded item: {}", data.itemId());
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
                        if (data != null) {
                            metals.put(data.id(), data);
                            LOGGER.info("[Smelter] Loaded metal type: {}", data.id());
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Smelter] Failed to load metal file {}: {}", id, e.toString());
                    }
                });

        SmelterData.setItems(items);
        SmelterData.setMetals(metals);

        LOGGER.info("[Smelter] Loaded {} item(s), {} metal type(s).", items.size(), metals.size());
    }

    private SmelterItemData parseItem(JsonObject json) {
        if (!json.has("item") || !json.has("metal_type") || !json.has("smelt_time") || !json.has("yield")) {
            LOGGER.warn("[Smelter] Item JSON missing required fields, skipping.");
            return null;
        }

        Identifier itemId = Identifier.of(json.get("item").getAsString());
        Identifier metalTypeId = Identifier.of(json.get("metal_type").getAsString());
        int smeltTime = json.get("smelt_time").getAsInt();
        int yield = json.get("yield").getAsInt();

        return new SmelterItemData(itemId, metalTypeId, smeltTime, yield);
    }

    private MetalTypeData parseMetal(JsonObject json) {
        if (!json.has("id") || !json.has("name") || !json.has("required_temp") || !json.has("color") || !json.has("texture")) {
            LOGGER.warn("[Smelter] Metal JSON missing required fields, skipping.");
            return null;
        }

        Identifier id = Identifier.of(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        int requiredTemp = json.get("required_temp").getAsInt();
        int color = parseColor(json.get("color").getAsString());
        Identifier texture = Identifier.of(json.get("texture").getAsString());

        return new MetalTypeData(id, name, requiredTemp, color, texture);
    }

    private int parseColor(String hex) {
        hex = hex.replace("#", "");
        return (int) Long.parseLong(hex, 16);
    }

}