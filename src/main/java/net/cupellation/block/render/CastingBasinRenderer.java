package net.cupellation.block.render;

import net.cupellation.block.entity.CastingBasinEntity;
import net.cupellation.data.SmelterData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CastingBasinRenderer implements BlockEntityRenderer<CastingBasinEntity> {

    // Basin-Innenmaße (Cauldron-ähnlich, leicht eingerückt von den Wänden)
    private static final float INNER_MIN = 2f / 16f;   // 2px vom Rand
    private static final float INNER_MAX = 14f / 16f;  // 14px
    private static final float FLOOR_Y = 4f / 16f;   // Boden des Bassins
    private static final float CEILING_Y = 14f / 16f;  // Maximale Füllhöhe

    // Vollständige Füllhöhe (Innenhöhe des Bassins)
    private static final float FILL_RANGE = CEILING_Y - FLOOR_Y;

    // Abkühlfarbe: von Molten-Farbe zu diesem dunklen Grau
    private static final int COOLED_COLOR = 0x3A3A3A;

    private final ItemRenderer itemRenderer;

    public CastingBasinRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(CastingBasinEntity basin, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (basin.getMoltenAmount() <= 0 && !basin.isCooled()) {
            return;
        }
        Identifier metalTypeId = basin.getMetalTypeId();
        if (metalTypeId == null) {
            return;
        }
        int moltenColor = SmelterData.getColor(metalTypeId);
        int renderColor = moltenColor;

        if (basin.isCooled()) {
            renderResultBlock(basin, matrices, vertexConsumers, light, overlay);
            return;
        }

        if (basin.getCooldownTicks() > 0) {
            float coolProgress = 1f - ((float) basin.getCooldownTicks() / CastingBasinEntity.COOL_TIME);
            renderColor = lerpColor(moltenColor, COOLED_COLOR, coolProgress);
        }

        float r = ((renderColor >> 16) & 0xFF) / 255f;
        float g = ((renderColor >> 8) & 0xFF) / 255f;
        float b = (renderColor & 0xFF) / 255f;

        float fillPercent = (float) basin.getMoltenAmount() / CastingBasinEntity.CAPACITY;
        float topY = FLOOR_Y + FILL_RANGE * fillPercent;

        Sprite sprite = getFluidSprite(metalTypeId);
        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();

        int lightFull = 15 << 4 | 15 << 20;

        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        float x0 = INNER_MIN, x1 = INNER_MAX;
        float z0 = INNER_MIN, z1 = INNER_MAX;
        float y0 = FLOOR_Y, y1 = topY;

        float e = 0.001f;

        // Top-Face (Flüssigkeitsoberfläche)
        renderQuad(consumer, matrix, x0, y1, z0, x1, y1, z1, true, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

        if (fillPercent < 0.99f) {
            // Nord
            renderQuadNorth(consumer, matrix, x0, y0, z0 + e, x1, y1, z0 + e, false, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
            // Süd
            renderQuadFlipped(consumer, matrix, x0, y0, z1 - e, x1, y1, z1 - e, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
            // West
            renderQuadWest(consumer, matrix, x0 + e, y0, z0, x0 + e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
            // Ost
            renderQuadEast(consumer, matrix, x1 - e, y0, z0, x1 - e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
        }

        matrices.pop();
    }

    private void renderResultBlock(CastingBasinEntity basin, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // Wir rendern den Block als Item (flat) oder als Block-Model
        // Hier als einfache gefärbte Fläche auf CEILING_Y Höhe
        // Du kannst das später zu einem echten Block-Model erweitern

        Identifier metalTypeId = basin.getMetalTypeId();
        if (metalTypeId == null) return;

        int color = COOLED_COLOR;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Sprite sprite = getFluidSprite(metalTypeId);
        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();

        int lightFull = 15 << 4 | 15 << 20;

        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        // Voller Block im Inneren des Bassins
        float x0 = INNER_MIN, x1 = INNER_MAX;
        float z0 = INNER_MIN, z1 = INNER_MAX;
        float y0 = FLOOR_Y, y1 = CEILING_Y;
        float e = 0.001f;

        renderQuad(consumer, matrix, x0, y1, z0, x1, y1, z1, true, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
        renderQuadNorth(consumer, matrix, x0, y0, z0 + e, x1, y1, z0 + e, false, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
        renderQuadFlipped(consumer, matrix, x0, y0, z1 - e, x1, y1, z1 - e, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
        renderQuadWest(consumer, matrix, x0 + e, y0, z0, x0 + e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
        renderQuadEast(consumer, matrix, x1 - e, y0, z0, x1 - e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

        matrices.pop();
    }

    // -------------------------------------------------------------------------
    // Quad-Helfer (TOP, NORTH, SOUTH, WEST, EAST)
    // -------------------------------------------------------------------------

    /** Top face – schaut von oben */
    private void renderQuad(VertexConsumer c, Matrix4f m, float x0, float y, float z0, float x1, float y1, float z1,
                            boolean top, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        if (top) {
            vertex(c, m, x0, y, z0, r, g, b, minU, minV, light, overlay);
            vertex(c, m, x0, y, z1, r, g, b, minU, maxV, light, overlay);
            vertex(c, m, x1, y, z1, r, g, b, maxU, maxV, light, overlay);
            vertex(c, m, x1, y, z0, r, g, b, maxU, minV, light, overlay);
        }
    }

    /** North face (z = fixed, schaut von Norden rein) */
    private void renderQuadNorth(VertexConsumer c, Matrix4f m, float x0, float y0, float z, float x1, float y1, float z1,
                                 boolean unused, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        float spanX = x1 - x0;
        float spanY = y1 - y0;
        vertex(c, m, x1, y0, z, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x0, y0, z, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x0, y1, z, r, g, b, minU, minV, light, overlay);
        vertex(c, m, x1, y1, z, r, g, b, maxU, minV, light, overlay);
    }

    /** South face */
    private void renderQuadFlipped(VertexConsumer c, Matrix4f m,
                                   float x0, float y0, float z, float x1, float y1, float z1,
                                   float r, float g, float b,
                                   float minU, float maxU, float minV, float maxV,
                                   int light, int overlay) {
        vertex(c, m, x0, y0, z, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x1, y0, z, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x1, y1, z, r, g, b, maxU, minV, light, overlay);
        vertex(c, m, x0, y1, z, r, g, b, minU, minV, light, overlay);
    }

    /** West face (x = fixed) */
    private void renderQuadWest(VertexConsumer c, Matrix4f m, float x, float y0, float z0, float x1, float y1, float z1,
                                float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        vertex(c, m, x, y0, z0, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x, y0, z1, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x, y1, z1, r, g, b, maxU, minV, light, overlay);
        vertex(c, m, x, y1, z0, r, g, b, minU, minV, light, overlay);
    }

    /** East face */
    private void renderQuadEast(VertexConsumer c, Matrix4f m, float x, float y0, float z0, float x1, float y1, float z1,
                                float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        vertex(c, m, x, y0, z1, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x, y0, z0, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x, y1, z0, r, g, b, minU, minV, light, overlay);
        vertex(c, m, x, y1, z1, r, g, b, maxU, minV, light, overlay);
    }

    private void vertex(VertexConsumer c, Matrix4f m, float x, float y, float z, float r, float g, float b, float u, float v, int light, int overlay) {
        c.vertex(m, x, y, z).color(r, g, b, 1f).texture(u, v).overlay(overlay).light(light).normal(0, 1, 0);
    }

    private Sprite getFluidSprite(Identifier metalTypeId) {
        Identifier textureId = SmelterData.getTexture(metalTypeId);
        return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(textureId);
    }

    private int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (rr << 16) | (rg << 8) | rb;
    }
}