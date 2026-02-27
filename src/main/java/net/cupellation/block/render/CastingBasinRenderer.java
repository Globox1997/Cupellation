package net.cupellation.block.render;

import net.cupellation.block.entity.CastingBasinEntity;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.MoltenHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CastingBasinRenderer implements BlockEntityRenderer<CastingBasinEntity> {

    private static final float INNER_MIN = 2f / 16f;
    private static final float INNER_MAX = 14f / 16f;
    private static final float FLOOR_Y = 4f / 16f;
    private static final float CEILING_Y = 14f / 16f;

    private static final float FILL_RANGE = CEILING_Y - FLOOR_Y;

    private final ItemRenderer itemRenderer;
    @Nullable
    private ItemStack stack = null;

    public CastingBasinRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(CastingBasinEntity basin, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (basin.getMoltenAmount() <= 0 && !basin.isCooled()) {
            stack = null;
            return;
        }
        Identifier metalTypeId = basin.getMetalTypeId();
        if (metalTypeId == null) {
            return;
        }

        if (basin.isCooled()) {
            renderResultBlock(basin, matrices, vertexConsumers, light, overlay);
            return;
        }
        int moltenColor = SmelterData.getColor(metalTypeId);
        int renderColor = moltenColor;
        int cooledColor = SmelterData.getCooledColor(metalTypeId);

        if (basin.getCooldownTicks() > 0) {
            float coolProgress = 1f - ((float) basin.getCooldownTicks() / CastingBasinEntity.COOL_TIME);
            renderColor = MoltenHelper.lerpColor(moltenColor, cooledColor, coolProgress);
        }

        float r = ((renderColor >> 16) & 0xFF) / 255f;
        float g = ((renderColor >> 8) & 0xFF) / 255f;
        float b = (renderColor & 0xFF) / 255f;

        float fillPercent = (float) basin.getMoltenAmount() / CastingBasinEntity.CAPACITY;
        float topY = FLOOR_Y + FILL_RANGE * fillPercent;

        Sprite sprite = MoltenHelper.getFluidSprite(metalTypeId);
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

        renderQuad(consumer, matrix, x0, y1, z0, x1, y1, z1, true, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

        if (fillPercent < 0.99f) {
            renderQuadNorth(consumer, matrix, x0, y0, z0 + e, x1, y1, z0 + e, false, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
            renderQuadFlipped(consumer, matrix, x0, y0, z1 - e, x1, y1, z1 - e, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
            renderQuadWest(consumer, matrix, x0 + e, y0, z0, x0 + e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
            renderQuadEast(consumer, matrix, x1 - e, y0, z0, x1 - e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
        }

        matrices.pop();
    }

    private void renderResultBlock(CastingBasinEntity basin, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Identifier metalTypeId = basin.getMetalTypeId();
        if (metalTypeId == null) {
            return;
        }
        if (stack == null) {
            Block block = Registries.BLOCK.get(SmelterData.getBlockId(metalTypeId));
            if (block.getDefaultState().isAir()) {
                return;
            }
            stack = new ItemStack(block.asItem());
            renderBlock(basin, matrices, vertexConsumers, light, overlay);
        } else {
            renderBlock(basin, matrices, vertexConsumers, light, overlay);
        }
    }

    private void renderBlock(CastingBasinEntity basin, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5D, 0D, 0.5D);
        matrices.scale(3.0f, 2.8f, 3.0f);
        this.itemRenderer.renderItem(stack, ModelTransformationMode.GROUND,
                WorldRenderer.getLightmapCoordinates(basin.getWorld(), basin.getPos().up()), overlay, matrices, vertexConsumers, basin.getWorld(),
                (int) basin.getPos().asLong());
        matrices.pop();
    }

    private void renderQuad(VertexConsumer c, Matrix4f m, float x0, float y, float z0, float x1, float y1, float z1,
                            boolean top, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        if (top) {
            vertex(c, m, x0, y, z0, r, g, b, minU, minV, light, overlay);
            vertex(c, m, x0, y, z1, r, g, b, minU, maxV, light, overlay);
            vertex(c, m, x1, y, z1, r, g, b, maxU, maxV, light, overlay);
            vertex(c, m, x1, y, z0, r, g, b, maxU, minV, light, overlay);
        }
    }

    private void renderQuadNorth(VertexConsumer c, Matrix4f m, float x0, float y0, float z, float x1, float y1, float z1,
                                 boolean unused, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        float spanX = x1 - x0;
        float spanY = y1 - y0;
        vertex(c, m, x1, y0, z, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x0, y0, z, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x0, y1, z, r, g, b, minU, minV, light, overlay);
        vertex(c, m, x1, y1, z, r, g, b, maxU, minV, light, overlay);
    }

    private void renderQuadFlipped(VertexConsumer c, Matrix4f m, float x0, float y0, float z, float x1, float y1, float z1,
                                   float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        vertex(c, m, x0, y0, z, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x1, y0, z, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x1, y1, z, r, g, b, maxU, minV, light, overlay);
        vertex(c, m, x0, y1, z, r, g, b, minU, minV, light, overlay);
    }

    private void renderQuadWest(VertexConsumer c, Matrix4f m, float x, float y0, float z0, float x1, float y1, float z1,
                                float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        vertex(c, m, x, y0, z0, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x, y0, z1, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x, y1, z1, r, g, b, maxU, minV, light, overlay);
        vertex(c, m, x, y1, z0, r, g, b, minU, minV, light, overlay);
    }

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

}