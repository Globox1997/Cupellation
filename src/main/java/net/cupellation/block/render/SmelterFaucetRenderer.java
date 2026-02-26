package net.cupellation.block.render;

import net.cupellation.block.SmelterDrain;
import net.cupellation.block.SmelterFaucet;
import net.cupellation.block.entity.CastingBasinEntity;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.block.entity.SmelterFaucetEntity;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();

        int lightFull = 15 << 4 | 15 << 20;

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

        renderHorizontalStream(consumer, matrix, facing, cx, cy, cz, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

        float streamBottom = cy - basinDist - 0.5f;
        renderVerticalStream(consumer, matrix, cx, cz, cy, streamBottom, r, g, b, minU, maxU, minV, maxV, lightFull, overlay);

        matrices.pop();
    }

    private void renderHorizontalStream(VertexConsumer c, Matrix4f m, Direction facing, float cx, float cy, float cz, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        float startX = cx, startZ = cz;
        float endX = cx, endZ = cz;

        Direction inward = facing.getOpposite();
        if (inward == Direction.NORTH) startZ = 0f;
        else if (inward == Direction.SOUTH) startZ = 1f;
        else if (inward == Direction.WEST) startX = 0f;
        else if (inward == Direction.EAST) startX = 1f;

        float y0 = cy - STREAM_HALF;
        float y1 = cy + STREAM_HALF;

        if (facing.getAxis() == Direction.Axis.Z) {
            float xL = cx - STREAM_HALF;
            float xR = cx + STREAM_HALF;
            // Top
            quad(c, m, xL, y1, startZ, xR, y1, endZ, r, g, b, minU, maxU, minV, maxV, light, overlay, 'T');
            // Bottom
            quad(c, m, xL, y0, startZ, xR, y0, endZ, r, g, b, minU, maxU, minV, maxV, light, overlay, 'B');
            // Left (west)
            quad(c, m, xL, y0, startZ, xL, y1, endZ, r, g, b, minU, maxU, minV, maxV, light, overlay, 'W');
            // Right (east)
            quad(c, m, xR, y0, startZ, xR, y1, endZ, r, g, b, minU, maxU, minV, maxV, light, overlay, 'E');
        } else {
            float zL = cz - STREAM_HALF;
            float zR = cz + STREAM_HALF;
            quad(c, m, startX, y1, zL, endX, y1, zR, r, g, b, minU, maxU, minV, maxV, light, overlay, 'T');
            quad(c, m, startX, y0, zL, endX, y0, zR, r, g, b, minU, maxU, minV, maxV, light, overlay, 'B');
            quad(c, m, startX, y0, zL, endX, y1, zL, r, g, b, minU, maxU, minV, maxV, light, overlay, 'N');
            quad(c, m, startX, y0, zR, endX, y1, zR, r, g, b, minU, maxU, minV, maxV, light, overlay, 'S');
        }
    }

    private void renderVerticalStream(VertexConsumer c, Matrix4f m, float cx, float cz, float top, float bottom, float r, float g, float b, float minU, float maxU, float minV, float maxV, int light, int overlay) {
        float xL = cx - STREAM_HALF;
        float xR = cx + STREAM_HALF;
        float zL = cz - STREAM_HALF;
        float zR = cz + STREAM_HALF;

        // Nord
        quad(c, m, xL, bottom, zL, xR, top, zL, r, g, b, minU, maxU, minV, maxV, light, overlay, 'N');
        // Süd
        quad(c, m, xL, bottom, zR, xR, top, zR, r, g, b, minU, maxU, minV, maxV, light, overlay, 'S');
        // West
        quad(c, m, xL, bottom, zL, xL, top, zR, r, g, b, minU, maxU, minV, maxV, light, overlay, 'W');
        // Ost
        quad(c, m, xR, bottom, zL, xR, top, zR, r, g, b, minU, maxU, minV, maxV, light, overlay, 'E');
    }

    private void quad(VertexConsumer c, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b,
                      float minU, float maxU, float minV, float maxV, int light, int overlay, char face) {
        switch (face) {
            case 'T' -> { // Top: x0,z0 → x1,z1 auf y=y1
                vertex(c, m, x0, y1, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x0, y1, z1, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x1, y1, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x1, y1, z0, r, g, b, maxU, minV, light, overlay);
            }
            case 'B' -> { // Bottom
                vertex(c, m, x0, y0, z1, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x0, y0, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x1, y0, z0, r, g, b, maxU, minV, light, overlay);
                vertex(c, m, x1, y0, z1, r, g, b, maxU, maxV, light, overlay);
            }
            case 'N' -> { // North (z=z0, schaut von -Z)
                vertex(c, m, x1, y0, z0, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x0, y0, z0, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x0, y1, z0, r, g, b, minU, minV, light, overlay);
                vertex(c, m, x1, y1, z0, r, g, b, maxU, minV, light, overlay);
            }
            case 'S' -> { // South (z=z1)
                vertex(c, m, x0, y0, z1, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x1, y0, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x1, y1, z1, r, g, b, maxU, minV, light, overlay);
                vertex(c, m, x0, y1, z1, r, g, b, minU, minV, light, overlay);
            }
            case 'W' -> { // West (x=x0)
                vertex(c, m, x0, y0, z0, r, g, b, minU, maxV, light, overlay);
                vertex(c, m, x0, y0, z1, r, g, b, maxU, maxV, light, overlay);
                vertex(c, m, x0, y1, z1, r, g, b, maxU, minV, light, overlay);
                vertex(c, m, x0, y1, z0, r, g, b, minU, minV, light, overlay);
            }
            case 'E' -> { // East (x=x1)
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