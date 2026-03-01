package net.cupellation.block.render;

import net.cupellation.block.SmelterBlock;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.MoltenHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import static net.cupellation.misc.MoltenHelper.*;

@Environment(EnvType.CLIENT)
public class SmelterBlockRenderer implements BlockEntityRenderer<SmelterBlockEntity> {

    private enum FaceDirection {TOP, TOP_INNER, BOTTOM, NORTH, SOUTH, WEST, EAST}

    public SmelterBlockRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(SmelterBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!blockEntity.isFormed()) {
            return;
        }
        if (blockEntity.getTotalFluid() <= 0) {
            return;
        }
        Direction facing = blockEntity.getCachedState().get(SmelterBlock.FACING);
        int lightFull = 15 << 4 | 15 << 20;

        float innerW = blockEntity.getStructureWidth() - 2f;
        float innerD = blockEntity.getStructureDepth() - 2f;
        float maxH = blockEntity.maxFillHeight();

        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, 0, -(blockEntity.getStructureDepth() - 0.5f));

        float x0 = 0.5f - (innerW / 2f);
        float x1 = x0 + innerW;
        float z0 = 1.0f;
        float z1 = z0 + innerD;
        float e = 0.001f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        int[] sortedSlots = blockEntity.getSlotsSortedByDensity();
        int cap = blockEntity.getMaxCapacity();

        float currentY = 0f;

// Erst alle Metalle rendern
        for (int slot : sortedSlots) {
            Identifier metalTypeId = blockEntity.getMetalTypeId(slot);
            int metalAmount = blockEntity.getMoltenMetal(slot);

            if (metalAmount > 0) {
                float metalH = ((float) metalAmount / cap) * maxH;
                float y0 = currentY;
                float y1 = currentY + metalH;

                Sprite sprite = MoltenHelper.getFluidSprite(metalTypeId);
                float minU = sprite.getMinU(), maxU = sprite.getMaxU();
                float minV = sprite.getMinV(), maxV = sprite.getMaxV();

                int color = SmelterData.getColor(metalTypeId);
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;

                renderTiledFace(consumer, matrix, x0, y0, z0, x1, y1, z1,
                        FaceDirection.TOP, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
                renderTiledFace(consumer, matrix, x0, y0, z0, x1, y1, z1,
                        FaceDirection.TOP_INNER, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

                if (slot == sortedSlots[0]) {
                    renderTiledFace(consumer, matrix, x0, y0, z0, x1, y1, z1,
                            FaceDirection.BOTTOM, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
                }

                renderTiledFace(consumer, matrix, x0, y0, z0 + e, x1, y1, z0 + e,
                        FaceDirection.NORTH, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
                renderTiledFace(consumer, matrix, x0, y0, z1 - e, x1, y1, z1 - e,
                        FaceDirection.SOUTH, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
                renderTiledFace(consumer, matrix, x0 + e, y0, z0, x0 + e, y1, z1,
                        FaceDirection.WEST, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
                renderTiledFace(consumer, matrix, x1 - e, y0, z0, x1 - e, y1, z1,
                        FaceDirection.EAST, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

                currentY = y1;
            }
        }

        int totalSlag = blockEntity.getTotalSlag();
        if (totalSlag > 0) {
            Sprite slagSprite = MoltenHelper.getFluidSprite(MoltenHelper.SLAG_TEXTURE);
            float slagMinU = slagSprite.getMinU(), slagMaxU = slagSprite.getMaxU();
            float slagMinV = slagSprite.getMinV(), slagMaxV = slagSprite.getMaxV();

            float slagH = ((float) totalSlag / cap) * maxH;
            float slagY0 = currentY;
            float slagY1 = Math.max(slagY0 + 0.05f, slagY0 + slagH);

            renderTiledFace(consumer, matrix, x0, slagY0, z0, x1, slagY1, z1,
                    FaceDirection.TOP, SLAG_R, SLAG_G, SLAG_B,
                    slagMinU, slagMaxU, slagMinV, slagMaxV, lightFull, overlay);
            renderTiledFace(consumer, matrix, x0, slagY0, z0, x1, slagY1, z1,
                    FaceDirection.TOP_INNER, SLAG_R, SLAG_G, SLAG_B,
                    slagMinU, slagMaxU, slagMinV, slagMaxV, lightFull, overlay);
            renderTiledFace(consumer, matrix, x0, slagY0, z0 + e, x1, slagY1, z0 + e,
                    FaceDirection.NORTH, SLAG_R, SLAG_G, SLAG_B,
                    slagMinU, slagMaxU, slagMinV, slagMaxV, lightFull, overlay);
            renderTiledFace(consumer, matrix, x0, slagY0, z1 - e, x1, slagY1, z1 - e,
                    FaceDirection.SOUTH, SLAG_R, SLAG_G, SLAG_B,
                    slagMinU, slagMaxU, slagMinV, slagMaxV, lightFull, overlay);
            renderTiledFace(consumer, matrix, x0 + e, slagY0, z0, x0 + e, slagY1, z1,
                    FaceDirection.WEST, SLAG_R, SLAG_G, SLAG_B,
                    slagMinU, slagMaxU, slagMinV, slagMaxV, lightFull, overlay);
            renderTiledFace(consumer, matrix, x1 - e, slagY0, z0, x1 - e, slagY1, z1,
                    FaceDirection.EAST, SLAG_R, SLAG_G, SLAG_B,
                    slagMinU, slagMaxU, slagMinV, slagMaxV, lightFull, overlay);
        }
        matrices.pop();
    }

    private void renderTiledFace(VertexConsumer consumer, Matrix4f matrix, float x0, float y0, float z0, float x1, float y1, float z1, FaceDirection face,
                                 float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        switch (face) {
            case TOP -> {
                for (float tx = 0; tx < (x1 - x0); tx++) {
                    for (float tz = 0; tz < (z1 - z0); tz++) {
                        float ax0 = x0 + tx, ax1 = Math.min(x0 + tx + 1, x1);
                        float az0 = z0 + tz, az1 = Math.min(z0 + tz + 1, z1);
                        float u1 = minU + (maxU - minU) * (ax1 - ax0);
                        float v1 = minV + (maxV - minV) * (az1 - az0);
                        vertex(consumer, matrix, ax0, y1, az0, r, g, b, minU, minV, light, overlay);
                        vertex(consumer, matrix, ax0, y1, az1, r, g, b, minU, v1, light, overlay);
                        vertex(consumer, matrix, ax1, y1, az1, r, g, b, u1, v1, light, overlay);
                        vertex(consumer, matrix, ax1, y1, az0, r, g, b, u1, minV, light, overlay);
                    }
                }
            }
            case TOP_INNER -> {
                float yInner = y1 - 0.002f;
                for (float tx = 0; tx < (x1 - x0); tx++) {
                    for (float tz = 0; tz < (z1 - z0); tz++) {
                        float ax0 = x0 + tx, ax1 = Math.min(x0 + tx + 1, x1);
                        float az0 = z0 + tz, az1 = Math.min(z0 + tz + 1, z1);
                        float u1 = minU + (maxU - minU) * (ax1 - ax0);
                        float v1 = minV + (maxV - minV) * (az1 - az0);
                        vertex(consumer, matrix, ax1, yInner, az0, r, g, b, u1, minV, light, overlay);
                        vertex(consumer, matrix, ax1, yInner, az1, r, g, b, u1, v1, light, overlay);
                        vertex(consumer, matrix, ax0, yInner, az1, r, g, b, minU, v1, light, overlay);
                        vertex(consumer, matrix, ax0, yInner, az0, r, g, b, minU, minV, light, overlay);
                    }
                }
            }
            case BOTTOM -> {
                for (float tx = 0; tx < (x1 - x0); tx++) {
                    for (float tz = 0; tz < (z1 - z0); tz++) {
                        float ax0 = x0 + tx, ax1 = Math.min(x0 + tx + 1, x1);
                        float az0 = z0 + tz, az1 = Math.min(z0 + tz + 1, z1);
                        float u1 = minU + (maxU - minU) * (ax1 - ax0);
                        float v1 = minV + (maxV - minV) * (az1 - az0);
                        vertex(consumer, matrix, ax0, y0, az1, r, g, b, minU, v1, light, overlay);
                        vertex(consumer, matrix, ax0, y0, az0, r, g, b, minU, minV, light, overlay);
                        vertex(consumer, matrix, ax1, y0, az0, r, g, b, u1, minV, light, overlay);
                        vertex(consumer, matrix, ax1, y0, az1, r, g, b, u1, v1, light, overlay);
                    }
                }
            }
            case NORTH -> {
                for (float tx = 0; tx < (x1 - x0); tx++) {
                    for (float ty = 0; ty < (y1 - y0); ty++) {
                        float ax0 = x0 + tx, ax1 = Math.min(x0 + tx + 1, x1);
                        float ay0 = y0 + ty, ay1 = Math.min(y0 + ty + 1, y1);
                        float u1 = minU + (maxU - minU) * (ax1 - ax0);
                        float v0 = maxV - (maxV - minV) * (ay1 - ay0);
                        vertex(consumer, matrix, ax1, ay0, z0, r, g, b, u1, maxV, light, overlay);
                        vertex(consumer, matrix, ax0, ay0, z0, r, g, b, minU, maxV, light, overlay);
                        vertex(consumer, matrix, ax0, ay1, z0, r, g, b, minU, v0, light, overlay);
                        vertex(consumer, matrix, ax1, ay1, z0, r, g, b, u1, v0, light, overlay);
                    }
                }
            }
            case SOUTH -> {
                for (float tx = 0; tx < (x1 - x0); tx++) {
                    for (float ty = 0; ty < (y1 - y0); ty++) {
                        float ax0 = x0 + tx, ax1 = Math.min(x0 + tx + 1, x1);
                        float ay0 = y0 + ty, ay1 = Math.min(y0 + ty + 1, y1);
                        float u1 = minU + (maxU - minU) * (ax1 - ax0);
                        float v0 = maxV - (maxV - minV) * (ay1 - ay0);
                        vertex(consumer, matrix, ax0, ay0, z1, r, g, b, minU, maxV, light, overlay);
                        vertex(consumer, matrix, ax1, ay0, z1, r, g, b, u1, maxV, light, overlay);
                        vertex(consumer, matrix, ax1, ay1, z1, r, g, b, u1, v0, light, overlay);
                        vertex(consumer, matrix, ax0, ay1, z1, r, g, b, minU, v0, light, overlay);
                    }
                }
            }
            case WEST -> {
                for (float tz = 0; tz < (z1 - z0); tz++) {
                    for (float ty = 0; ty < (y1 - y0); ty++) {
                        float az0 = z0 + tz, az1 = Math.min(z0 + tz + 1, z1);
                        float ay0 = y0 + ty, ay1 = Math.min(y0 + ty + 1, y1);
                        float u1 = minU + (maxU - minU) * (az1 - az0);
                        float v0 = maxV - (maxV - minV) * (ay1 - ay0);
                        vertex(consumer, matrix, x0, ay0, az0, r, g, b, minU, maxV, light, overlay);
                        vertex(consumer, matrix, x0, ay0, az1, r, g, b, u1, maxV, light, overlay);
                        vertex(consumer, matrix, x0, ay1, az1, r, g, b, u1, v0, light, overlay);
                        vertex(consumer, matrix, x0, ay1, az0, r, g, b, minU, v0, light, overlay);
                    }
                }
            }
            case EAST -> {
                for (float tz = 0; tz < (z1 - z0); tz++) {
                    for (float ty = 0; ty < (y1 - y0); ty++) {
                        float az0 = z0 + tz, az1 = Math.min(z0 + tz + 1, z1);
                        float ay0 = y0 + ty, ay1 = Math.min(y0 + ty + 1, y1);
                        float u1 = minU + (maxU - minU) * (az1 - az0);
                        float v0 = maxV - (maxV - minV) * (ay1 - ay0);
                        vertex(consumer, matrix, x1, ay0, az1, r, g, b, u1, maxV, light, overlay);
                        vertex(consumer, matrix, x1, ay0, az0, r, g, b, minU, maxV, light, overlay);
                        vertex(consumer, matrix, x1, ay1, az0, r, g, b, minU, v0, light, overlay);
                        vertex(consumer, matrix, x1, ay1, az1, r, g, b, u1, v0, light, overlay);
                    }
                }
            }
        }
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float u, float v, int light, int overlay) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, 1.0f).texture(u, v).overlay(overlay).light(light).normal(0, 1, 0);
    }
}