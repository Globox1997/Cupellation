package net.cupellation.block.render;

import net.cupellation.CupellationMain;
import net.cupellation.block.SmelterBlock;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class SmelterBlockRenderer implements BlockEntityRenderer<SmelterBlockEntity> {

    private static final Identifier MOLTEN_TEXTURE = CupellationMain.identifierOf("fluid/molten");

    public SmelterBlockRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(SmelterBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!blockEntity.isFormed() || blockEntity.getMoltenMetal() <= 0) return;

        Sprite sprite = getFluidSprite(blockEntity.getMetalType());
        Direction facing = blockEntity.getCachedState().get(SmelterBlock.FACING);

        int lightFull = 15 << 4 | 15 << 20;

        float innerW = blockEntity.getStructureWidth() - 2f;
        float innerD = blockEntity.getStructureDepth() - 2f;

        float fillHeight = blockEntity.getFillPercent() * blockEntity.getStructureHeight() * 0.8f + 0.05f;

        int color = getMetalColor(blockEntity.getMetalType());
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        matrices.push();

        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, 0, -(blockEntity.getStructureDepth() - 0.5f));
        float startX = 0.5f - (innerW / 2f);
        float startZ = 1.0f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();

        vertex(consumer, matrix, startX, fillHeight, startZ + innerD, r, g, b, minU, maxV, lightFull, overlay);
        vertex(consumer, matrix, startX + innerW, fillHeight, startZ + innerD, r, g, b, maxU, maxV, lightFull, overlay);
        vertex(consumer, matrix, startX + innerW, fillHeight, startZ, r, g, b, maxU, minV, lightFull, overlay);
        vertex(consumer, matrix, startX, fillHeight, startZ, r, g, b, minU, minV, lightFull, overlay);

        matrices.pop();
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float u, float v, int light, int overlay) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, 1.0f).texture(u, v).overlay(overlay).light(light).normal(0, 1, 0);
    }

    private Sprite getFluidSprite(int metalType) {
        return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(getTextureId(metalType));
    }

    private Identifier getTextureId(int metalType) {
        return switch (metalType) {
            case 1 -> MOLTEN_TEXTURE;
            case 2 -> MOLTEN_TEXTURE;
            default -> MOLTEN_TEXTURE;
        };
    }

    private int getMetalColor(int metalType) {
        return switch (metalType) {
//            case 1 -> 0xC8C8C8; // Eisen – silbrig
            case 1 -> 0xFF4500; // Eisen – silbrig
            case 2 -> 0xFFD700; // Gold – golden
            default -> 0xFF4500; // Fallback – orange-rot
        };
    }

}