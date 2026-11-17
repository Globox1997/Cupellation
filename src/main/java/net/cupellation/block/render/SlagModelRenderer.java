package net.cupellation.block.render;

import net.cupellation.init.BlockInit;
import net.cupellation.misc.SlagHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlagBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SlagModelRenderer {

    public static void render(Identifier metalTypeId, int layers, float minXZ, float maxXZ, float bottomY, float layerHeight,
                              MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        int clamped = MathHelper.clamp(layers, 1, SlagHelper.MAX_LAYERS);
        BlockState state = clamped >= SlagHelper.MAX_LAYERS ? BlockInit.SLAG_BLOCK.getDefaultState() : BlockInit.SLAG.getDefaultState().with(SlagBlock.LAYERS, clamped);

        int color = SlagHelper.getTint(List.of(metalTypeId));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        float modelLayerHeight = 1f / SlagHelper.MAX_LAYERS;
        float size = maxXZ - minXZ;

        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        matrices.push();
        matrices.translate(minXZ, bottomY, minXZ);
        matrices.scale(size, layerHeight / modelLayerHeight, size);
        blockRenderManager.getModelRenderer().render(matrices.peek(), vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(state, false)), state,
                blockRenderManager.getModel(state), r, g, b, light, overlay);
        matrices.pop();
    }
}
