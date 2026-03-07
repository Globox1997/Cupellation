package net.cupellation.block.render;

import net.cupellation.block.SmelterFaucet;
import net.cupellation.block.entity.CastingTableEntity;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.MoltenHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
            renderColor = MoltenHelper.lerpColor(moltenColor, cooledColor, coolProgress);
        } else {
            renderColor = moltenColor;
        }

        float r = ((renderColor >> 16) & 0xFF) / 255f;
        float g = ((renderColor >> 8) & 0xFF) / 255f;
        float b = (renderColor & 0xFF) / 255f;

        float fillPercent = (float) blockEntity.getMoltenAmount() / blockEntity.getCapacity();
        float topY = SURFACE_Y + FILL_RANGE * fillPercent;

        Sprite sprite = MoltenHelper.getFluidSprite(metalTypeId);
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

    private Direction getFaucetDirection(World world, BlockPos pos) {
        for (int i = 1; i <= 2; i++) {
            if (world.getBlockState(pos.up(i)).getBlock() instanceof SmelterFaucet) {
                return world.getBlockState(pos.up(i)).get(SmelterFaucet.FACING);
            }
        }
        return Direction.NORTH;
    }
}