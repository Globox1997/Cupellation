//package net.cupellation.block.render;
//
//import net.cupellation.block.entity.CastingTableEntity;
//import net.cupellation.data.SmelterData;
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.minecraft.block.Block;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.client.render.VertexConsumer;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.WorldRenderer;
//import net.minecraft.client.render.block.entity.BlockEntityRenderer;
//import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
//import net.minecraft.client.render.item.ItemRenderer;
//import net.minecraft.client.render.model.json.ModelTransformationMode;
//import net.minecraft.client.texture.Sprite;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.item.ItemStack;
//import net.minecraft.registry.Registries;
//import net.minecraft.screen.PlayerScreenHandler;
//import net.minecraft.util.Identifier;
//import org.jetbrains.annotations.Nullable;
//import org.joml.Matrix4f;
//
//@Environment(EnvType.CLIENT)
//public class CastingTableRenderer implements BlockEntityRenderer<CastingTableEntity> {
//
//    private static final float INNER_MIN = 2f / 16f;
//    private static final float INNER_MAX = 14f / 16f;
//    private static final float FLOOR_Y = 4f / 16f;
//    private static final float CEILING_Y = 14f / 16f;
//
//    private static final float FILL_RANGE = CEILING_Y - FLOOR_Y;
//
//    private final ItemRenderer itemRenderer;
//    @Nullable
//    private ItemStack stack = null;
//
//    public CastingTableRenderer(BlockEntityRendererFactory.Context ctx) {
//        this.itemRenderer = ctx.getItemRenderer();
//    }
//
//    @Override
//    public void render(CastingTableEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
//        if (blockEntity.getMoltenAmount() <= 0 && !blockEntity.isCooled()) {
//            stack = null;
//            return;
//        }
//        Identifier metalTypeId = blockEntity.getMetalTypeId();
//        if (metalTypeId == null) {
//            return;
//        }
//
//        if (blockEntity.isCooled()) {
//            renderResultBlock(blockEntity, matrices, vertexConsumers, light, overlay);
//            return;
//        }
//        int moltenColor = SmelterData.getColor(metalTypeId);
//        int renderColor = moltenColor;
//        int cooledColor = SmelterData.getCooledColor(metalTypeId);
//
//        if (blockEntity.getCooldownTicks() > 0) {
//            float coolProgress = 1f - ((float) blockEntity.getCooldownTicks() / CastingTableEntity.COOL_TIME);
//            renderColor = lerpColor(moltenColor, cooledColor, coolProgress);
//        }
//
//        float r = ((renderColor >> 16) & 0xFF) / 255f;
//        float g = ((renderColor >> 8) & 0xFF) / 255f;
//        float b = (renderColor & 0xFF) / 255f;
//
//        float fillPercent = (float) blockEntity.getMoltenAmount() / CastingTableEntity.CAPACITY;
//        float topY = FLOOR_Y + FILL_RANGE * fillPercent;
//
//        Sprite sprite = getFluidSprite(metalTypeId);
//        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
//        float minV = sprite.getMinV(), maxV = sprite.getMaxV();
//
//        int lightFull = 15 << 4 | 15 << 20;
//
//        matrices.push();
//        Matrix4f matrix = matrices.peek().getPositionMatrix();
//        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
//
//        float x0 = INNER_MIN, x1 = INNER_MAX;
//        float z0 = INNER_MIN, z1 = INNER_MAX;
//        float y0 = FLOOR_Y, y1 = topY;
//
//        float e = 0.001f;
//
//        renderQuad(consumer, matrix, x0, y1, z0, x1, y1, z1, true, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
//
//        if (fillPercent < 0.99f) {
//            renderQuadNorth(consumer, matrix, x0, y0, z0 + e, x1, y1, z0 + e, false, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
//            renderQuadFlipped(consumer, matrix, x0, y0, z1 - e, x1, y1, z1 - e, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
//            renderQuadWest(consumer, matrix, x0 + e, y0, z0, x0 + e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
//            renderQuadEast(consumer, matrix, x1 - e, y0, z0, x1 - e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
//        }
//
//        matrices.pop();
//    }
//
//    private void renderResultBlock(CastingTableEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
//        Identifier metalTypeId = blockEntity.getMetalTypeId();
//        if (metalTypeId == null) {
//            return;
//        }
//        if (stack == null) {
//            Block block = Registries.BLOCK.get(SmelterData.getBlockId(metalTypeId));
//            if (block.getDefaultState().isAir()) {
//                return;
//            }
//            stack = new ItemStack(block.asItem());
//            renderBlock(blockEntity, matrices, vertexConsumers, light, overlay);
//        } else {
//            renderBlock(blockEntity, matrices, vertexConsumers, light, overlay);
//        }
////        int color = COOLED_COLOR;
////        float r = ((color >> 16) & 0xFF) / 255f;
////        float g = ((color >> 8) & 0xFF) / 255f;
////        float b = (color & 0xFF) / 255f;
////
////        Sprite sprite = getFluidSprite(metalTypeId);
////        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
////        float minV = sprite.getMinV(), maxV = sprite.getMaxV();
////
////        int lightFull = 15 << 4 | 15 << 20;
////
////        matrices.push();
////        Matrix4f matrix = matrices.peek().getPositionMatrix();
////        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
////
////        float x0 = INNER_MIN, x1 = INNER_MAX;
////        float z0 = INNER_MIN, z1 = INNER_MAX;
////        float y0 = FLOOR_Y, y1 = CEILING_Y;
////        float e = 0.001f;
////
////        renderQuad(consumer, matrix, x0, y1, z0, x1, y1, z1, true, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
////        renderQuadNorth(consumer, matrix, x0, y0, z0 + e, x1, y1, z0 + e, false, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
////        renderQuadFlipped(consumer, matrix, x0, y0, z1 - e, x1, y1, z1 - e, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
////        renderQuadWest(consumer, matrix, x0 + e, y0, z0, x0 + e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
////        renderQuadEast(consumer, matrix, x1 - e, y0, z0, x1 - e, y1, z1, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);
////
////        matrices.pop();
//    }
//
//    private void renderBlock(CastingTableEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
//        matrices.push();
//        matrices.translate(0.5D, 0D, 0.5D);
//        matrices.scale(3.0f, 2.8f, 3.0f);
//        this.itemRenderer.renderItem(stack, ModelTransformationMode.GROUND,
//                WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up()), overlay, matrices, vertexConsumers, blockEntity.getWorld(),
//                (int) blockEntity.getPos().asLong());
//        matrices.pop();
//    }
//
//    private void renderQuad(VertexConsumer c, Matrix4f m, float x0, float y, float z0, float x1, float y1, float z1,
//                            boolean top, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
//        if (top) {
//            vertex(c, m, x0, y, z0, r, g, b, minU, minV, light, overlay);
//            vertex(c, m, x0, y, z1, r, g, b, minU, maxV, light, overlay);
//            vertex(c, m, x1, y, z1, r, g, b, maxU, maxV, light, overlay);
//            vertex(c, m, x1, y, z0, r, g, b, maxU, minV, light, overlay);
//        }
//    }
//
//    private void renderQuadNorth(VertexConsumer c, Matrix4f m, float x0, float y0, float z, float x1, float y1, float z1,
//                                 boolean unused, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
//        float spanX = x1 - x0;
//        float spanY = y1 - y0;
//        vertex(c, m, x1, y0, z, r, g, b, maxU, maxV, light, overlay);
//        vertex(c, m, x0, y0, z, r, g, b, minU, maxV, light, overlay);
//        vertex(c, m, x0, y1, z, r, g, b, minU, minV, light, overlay);
//        vertex(c, m, x1, y1, z, r, g, b, maxU, minV, light, overlay);
//    }
//
//    private void renderQuadFlipped(VertexConsumer c, Matrix4f m, float x0, float y0, float z, float x1, float y1, float z1,
//                                   float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
//        vertex(c, m, x0, y0, z, r, g, b, minU, maxV, light, overlay);
//        vertex(c, m, x1, y0, z, r, g, b, maxU, maxV, light, overlay);
//        vertex(c, m, x1, y1, z, r, g, b, maxU, minV, light, overlay);
//        vertex(c, m, x0, y1, z, r, g, b, minU, minV, light, overlay);
//    }
//
//    private void renderQuadWest(VertexConsumer c, Matrix4f m, float x, float y0, float z0, float x1, float y1, float z1,
//                                float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
//        vertex(c, m, x, y0, z0, r, g, b, minU, maxV, light, overlay);
//        vertex(c, m, x, y0, z1, r, g, b, maxU, maxV, light, overlay);
//        vertex(c, m, x, y1, z1, r, g, b, maxU, minV, light, overlay);
//        vertex(c, m, x, y1, z0, r, g, b, minU, minV, light, overlay);
//    }
//
//    private void renderQuadEast(VertexConsumer c, Matrix4f m, float x, float y0, float z0, float x1, float y1, float z1,
//                                float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
//        vertex(c, m, x, y0, z1, r, g, b, maxU, maxV, light, overlay);
//        vertex(c, m, x, y0, z0, r, g, b, minU, maxV, light, overlay);
//        vertex(c, m, x, y1, z0, r, g, b, minU, minV, light, overlay);
//        vertex(c, m, x, y1, z1, r, g, b, maxU, minV, light, overlay);
//    }
//
//    private void vertex(VertexConsumer c, Matrix4f m, float x, float y, float z, float r, float g, float b, float u, float v, int light, int overlay) {
//        c.vertex(m, x, y, z).color(r, g, b, 1f).texture(u, v).overlay(overlay).light(light).normal(0, 1, 0);
//    }
//
//    private Sprite getFluidSprite(Identifier metalTypeId) {
//        Identifier textureId = SmelterData.getTexture(metalTypeId);
//        return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(textureId);
//    }
//
//    private int lerpColor(int a, int b, float t) {
//        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
//        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
//        int rr = (int) (ar + (br - ar) * t);
//        int rg = (int) (ag + (bg - ag) * t);
//        int rb = (int) (ab + (bb - ab) * t);
//        return (rr << 16) | (rg << 8) | rb;
//    }
//}

package net.cupellation.block.render;

import net.cupellation.block.SmelterFaucet;
import net.cupellation.block.entity.CastingTableEntity;
import net.cupellation.data.SmelterData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CastingTableRenderer implements BlockEntityRenderer<CastingTableEntity> {

    private static final float SURFACE_Y = 15f / 16f + 0.0001f;
    private static final float FILL_RANGE = 0.99f - SURFACE_Y;

    private static final float INNER_MIN = 2f / 16f;
    private static final float INNER_MAX = 14f / 16f;

    private final ItemRenderer itemRenderer;

    public CastingTableRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(CastingTableEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (blockEntity.hasResult()) {
            renderResultItem(blockEntity, matrices, vertexConsumers, light, overlay);
        }
        if (blockEntity.hasMold()) {
            renderMoldItem(blockEntity, matrices, vertexConsumers, light, overlay);
        }
        if (blockEntity.getMoltenAmount() <= 0) {
            return;
        }
        Identifier metalTypeId = blockEntity.getMetalTypeId();
        if (metalTypeId == null) {
            return;
        }

        int moltenColor = SmelterData.getColor(metalTypeId);
        int cooledColor = SmelterData.getCooledColor(metalTypeId);
        int renderColor;

        if (blockEntity.getCooldownTicks() > 0) {
            float coolProgress = 1f - ((float) blockEntity.getCooldownTicks() / CastingTableEntity.COOL_TIME);
            renderColor = lerpColor(moltenColor, cooledColor, coolProgress);
        } else {
            renderColor = moltenColor;
        }

        float r = ((renderColor >> 16) & 0xFF) / 255f;
        float g = ((renderColor >> 8) & 0xFF) / 255f;
        float b = (renderColor & 0xFF) / 255f;

        float fillPercent = (float) blockEntity.getMoltenAmount() / CastingTableEntity.CAPACITY;
        float topY = SURFACE_Y + FILL_RANGE * fillPercent;

        Sprite sprite = getFluidSprite(metalTypeId);
        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();

        int lightFull = 15 << 4 | 15 << 20;

        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        renderTopQuad(consumer, matrix, INNER_MIN, topY, INNER_MIN, INNER_MAX, topY, INNER_MAX, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

        matrices.pop();
    }

    private void renderResultItem(CastingTableEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!blockEntity.hasResult()) {
            return;
        }
        renderItem(blockEntity, blockEntity.getResult(), matrices, vertexConsumers, light, overlay);
    }

    private void renderMoldItem(CastingTableEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!blockEntity.hasMold()) {
            return;
        }
        renderItem(blockEntity, blockEntity.getMold(), matrices, vertexConsumers, light, overlay);
    }

    private void renderItem(CastingTableEntity blockEntity, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5D, 0.972D, 0.5D);

        switch (getFaucetDirection(blockEntity.getWorld(), blockEntity.getPos())) {
            case Direction.NORTH -> {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            }
            case Direction.EAST -> {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            }
            case Direction.SOUTH -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
            case Direction.WEST -> {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));
            }
            default -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        }

        matrices.scale(0.75f, 0.75f, 0.75f);

        this.itemRenderer.renderItem(stack, ModelTransformationMode.GUI,
                WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up()), overlay, matrices, vertexConsumers, blockEntity.getWorld(),
                (int) blockEntity.getPos().asLong());
        matrices.pop();
    }

    private void renderTopQuad(VertexConsumer c, Matrix4f m, float x0, float y, float z0, float x1, float y1, float z1, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        vertex(c, m, x0, y, z0, r, g, b, minU, minV, light, overlay);
        vertex(c, m, x0, y, z1, r, g, b, minU, maxV, light, overlay);
        vertex(c, m, x1, y, z1, r, g, b, maxU, maxV, light, overlay);
        vertex(c, m, x1, y, z0, r, g, b, maxU, minV, light, overlay);
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

    private Direction getFaucetDirection(World world, BlockPos pos) {
        for (int i = 1; i <= 2; i++) {
            if (world.getBlockState(pos.up(i)).getBlock() instanceof SmelterFaucet) {
                return world.getBlockState(pos.up(i)).get(SmelterFaucet.FACING);
            }
        }
        return Direction.NORTH;
    }
}