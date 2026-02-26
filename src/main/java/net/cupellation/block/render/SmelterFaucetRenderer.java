package net.cupellation.block.render;

import net.cupellation.block.SmelterFaucet;
import net.cupellation.block.entity.CastingBasinEntity;
import net.cupellation.block.entity.SmelterFaucetEntity;
import net.cupellation.data.SmelterData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class SmelterFaucetRenderer implements BlockEntityRenderer<SmelterFaucetEntity> {

    private static final float STREAM_HALF = 1f / 16f;

    private static final float UV_FRAC = 2f / 16f;
    private static final float HORIZONTAL_FRAC = 8f / 16f;

    public SmelterFaucetRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(SmelterFaucetEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = blockEntity.getWorld();
        BlockPos faucetPos = blockEntity.getPos();
        if (world == null) {
            return;
        }
        BlockState faucetState = world.getBlockState(faucetPos);
        if (!(faucetState.getBlock() instanceof SmelterFaucet)) {
            return;
        }
        if (!faucetState.get(SmelterFaucet.OPEN)) {
            return;
        }
        Identifier metalTypeId = blockEntity.getMetalTypeId();
        if (metalTypeId == null) {
            return;
        }
        int color = SmelterData.getColor(metalTypeId);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Sprite sprite = getFluidSprite(metalTypeId);
        int lightFull = 15 << 4 | 15 << 20;

        float minU = sprite.getMinU();
        float maxU = minU + (sprite.getMaxU() - sprite.getMinU()) * UV_FRAC;
        float minV = sprite.getMinV();
        float maxV = minV + (sprite.getMaxV() - sprite.getMinV()) * UV_FRAC;

        float horizontalMaxU = minU + (sprite.getMaxU() - sprite.getMinU()) * HORIZONTAL_FRAC;
        float horizontalMaxV = minV + (sprite.getMaxV() - sprite.getMinV()) * HORIZONTAL_FRAC;

        Direction facing = faucetState.get(SmelterFaucet.FACING);

        int basinDist = getBasinDistance(world, faucetPos);
        if (basinDist < 0) {
            return;
        }
        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        float cx = 0.5f;
        float cy = 0.5f;
        float cz = 0.5f;

        renderHorizontalStream(consumer, matrix, facing, cx, cy, cz, r, g, b, minU, maxU, horizontalMaxU, minV, maxV, horizontalMaxV, lightFull, overlay);

        float streamTop = cy + STREAM_HALF;
        float streamBottom = streamTop - basinDist - (5f / 16f);

        float verticalMaxU = minU + (sprite.getMaxU() - sprite.getMinU()) * (5f / 16f + basinDist);
        float verticalMaxV = minV + (sprite.getMaxV() - sprite.getMinV()) * (5f / 16f + basinDist);

        renderVerticalStream(consumer, matrix, facing, cx, cz, streamTop, streamBottom, r, g, b, minU, maxU, verticalMaxU, minV, maxV, verticalMaxV, lightFull, overlay);

        matrices.pop();
    }


    private void renderHorizontalStream(VertexConsumer c, Matrix4f m, Direction facing, float cx, float cy, float cz, float r, float g, float b, float minU, float maxU, float horizontalMaxU, float minV, float maxV, float horizontalMaxV, int light, int overlay) {
        float y0 = cy - STREAM_HALF;
        float y1 = cy + STREAM_HALF;

        switch (facing) {
            case Direction.NORTH -> {
                float startZ = 1f;
                float endZ = cz;

                float xL = cx - STREAM_HALF;
                float xR = cx + STREAM_HALF;
                quad(c, m, xR, y0, endZ, xL, y0, startZ, r, g, b, minU, maxU, minV, horizontalMaxV, light, overlay, 'T');
                quad(c, m, xR, y1, endZ, xL, y1, startZ, r, g, b, minU, maxU, minV, horizontalMaxV, light, overlay, 'B');
                quad(c, m, xL, y0, endZ, xL, y1, startZ, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'W');
                quad(c, m, xR, y0, endZ, xR, y1, startZ, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'E');
            }
            case Direction.EAST -> {
                float startX = 0f;
                float endX = cx;

                float zL = cz - STREAM_HALF;
                float zR = cz + STREAM_HALF;

                quad(c, m, startX, y1, zL, endX, y1, zR, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'T');
                quad(c, m, startX, y0, zL, endX, y0, zR, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'B');
                quad(c, m, startX, y0, zL, endX, y1, zL, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'N');
                quad(c, m, startX, y0, zR, endX, y1, zR, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'S');
            }
            case Direction.SOUTH -> {
                float startZ = 0f;
                float endZ = cz;

                float xL = cx - STREAM_HALF;
                float xR = cx + STREAM_HALF;
                quad(c, m, xR, y1, endZ, xL, y1, startZ, r, g, b, minU, maxU, minV, horizontalMaxV, light, overlay, 'T');
                quad(c, m, xR, y0, endZ, xL, y0, startZ, r, g, b, minU, maxU, minV, horizontalMaxV, light, overlay, 'B');
                quad(c, m, xL, y1, endZ, xL, y0, startZ, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'W');
                quad(c, m, xR, y1, endZ, xR, y0, startZ, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'E');
            }
            case Direction.WEST -> {
                float startX = 1f;
                float endX = cx;

                float zL = cz - STREAM_HALF;
                float zR = cz + STREAM_HALF;

                quad(c, m, startX, y0, zL, endX, y0, zR, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'T');
                quad(c, m, startX, y1, zL, endX, y1, zR, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'B');
                quad(c, m, startX, y1, zL, endX, y0, zL, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'N');
                quad(c, m, startX, y1, zR, endX, y0, zR, r, g, b, minU, horizontalMaxU, minV, maxV, light, overlay, 'S');
            }
        }
    }

    private void renderVerticalStream(VertexConsumer c, Matrix4f m, Direction facing, float cx, float cz,
                                      float top, float bottom, float r, float g, float b, float minU, float maxU, float verticalMaxU, float minV, float maxV, float verticalMaxV, int light, int overlay) {
        float xL = cx - STREAM_HALF;
        float xR = cx + STREAM_HALF;
        float zL = cz - STREAM_HALF;
        float zR = cz + STREAM_HALF;

        switch (facing) {
            case Direction.NORTH -> {
                xR += 0.00001f;
                xL += 0.00001f;
                zR += STREAM_HALF;
                zL += STREAM_HALF;
            }
            case Direction.EAST -> {
                xR -= STREAM_HALF;
                xL -= STREAM_HALF;
                zR += 0.00001f;
                zL += 0.00001f;
            }
            case Direction.SOUTH -> {
                xR += 0.00001f;
                xL += 0.00001f;
                zR -= STREAM_HALF;
                zL -= STREAM_HALF;
            }
            case Direction.WEST -> {
                xR += STREAM_HALF;
                xL += STREAM_HALF;
                zR += 0.00001f;
                zL += 0.00001f;
            }
        }

        quad(c, m, xL, bottom, zL, xR, top, zL, r, g, b, minU, maxU, minV, verticalMaxV, light, overlay, 'N');
        quad(c, m, xL, bottom, zR, xR, top, zR, r, g, b, minU, maxU, minV, verticalMaxV, light, overlay, 'S');
        quad(c, m, xL, bottom, zL, xL, top, zR, r, g, b, minU, maxU, minV, verticalMaxV, light, overlay, 'W');
        quad(c, m, xR, bottom, zL, xR, top, zR, r, g, b, minU, maxU, minV, verticalMaxV, light, overlay, 'E');
    }

    private void quad(VertexConsumer c, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay, char face) {
        switch (face) {
            case 'T' -> {
                vertex(c, m, x0, y1, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x0, y1, z1, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x1, y1, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x1, y1, z0, r, g, b, maxU, minV, light, overlay);
            }
            case 'B' -> {
                vertex(c, m, x0, y0, z1, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x0, y0, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x1, y0, z0, r, g, b, maxU, minV, light, overlay);
                vertex(c, m, x1, y0, z1, r, g, b, maxU, maxV, light, overlay);
            }
            case 'N' -> {
                vertex(c, m, x1, y0, z0, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x0, y0, z0, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x0, y1, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x1, y1, z0, r, g, b, maxU, minV, light, overlay);
            }
            case 'S' -> {
                vertex(c, m, x0, y0, z1, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x1, y0, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x1, y1, z1, r, g, b, maxU, minV, light, overlay);
                vertex(c, m, x0, y1, z1, r, g, b, minU, minV, light, overlay);
            }
            case 'W' -> {
                vertex(c, m, x0, y0, z0, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x0, y0, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x0, y1, z1, r, g, b, maxU, minV, light, overlay);
                vertex(c, m, x0, y1, z0, r, g, b, minU, minV, light, overlay);
            }
            case 'E' -> {
                vertex(c, m, x1, y0, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x1, y0, z0, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x1, y1, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x1, y1, z1, r, g, b, maxU, minV, light, overlay);
            }
        }
    }

    private void vertex(VertexConsumer c, Matrix4f m, float x, float y, float z, float r, float g, float b, float u, float v, int light, int overlay) {
        c.vertex(m, x, y, z).color(r, g, b, 1f).texture(u, v).overlay(overlay).light(light).normal(0, 1, 0);
    }

    private int getBasinDistance(World world, BlockPos faucetPos) {
        for (int dy = 1; dy <= 2; dy++) {
            if (world.getBlockEntity(faucetPos.down(dy)) instanceof CastingBasinEntity) {
                return dy;
            }
        }
        return -1;
    }

    private Sprite getFluidSprite(Identifier metalTypeId) {
        Identifier textureId = SmelterData.getTexture(metalTypeId);
        return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(textureId);
    }
}