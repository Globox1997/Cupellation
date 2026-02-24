package net.cupellation.init;

import net.cupellation.block.render.SmelterBlockRenderer;
import net.cupellation.block.screen.SmelterScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockEntityRendererFactories.register(BlockInit.SMELTER_ENTITY, SmelterBlockRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.SMELTER, RenderLayer.getCutout());

        HandledScreens.register(BlockInit.SMELTER_SCREEN_HANDLER, SmelterScreen::new);
    }
}
