package net.cupellation.init;

import net.cupellation.block.render.CastingBasinRenderer;
import net.cupellation.block.render.CastingTableRenderer;
import net.cupellation.block.render.SmelterBlockRenderer;
import net.cupellation.block.render.SmelterFaucetRenderer;
import net.cupellation.block.screen.SmelterScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockEntityRendererFactories.register(BlockInit.SMELTER_ENTITY, SmelterBlockRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.CASTING_BASIN_ENTITY, CastingBasinRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.SMELTER_FAUCET_ENTITY, SmelterFaucetRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.CASTING_TABLE_ENTITY, CastingTableRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_SMELTER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_GLASS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_CASTING_BASIN, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_CASTING_TABLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_LEVER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_FAUCET, RenderLayer.getCutoutMipped());

        HandledScreens.register(BlockInit.SMELTER_SCREEN_HANDLER, SmelterScreen::new);

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            if (stack.contains(ItemInit.QUALITY_GRADE)) {
                Integer quality = stack.get(ItemInit.QUALITY_GRADE);

                if (quality != null) {
                    Formatting formatting = switch (quality) {
                        case 1 -> Formatting.YELLOW;
                        case 2 -> Formatting.GOLD;
                        case 3 -> Formatting.RED;
                        default -> Formatting.GOLD;
                    };
                    lines.add(Text.translatable("item.cupellation.tooltip.quality").append(Text.literal(" ")).append(Text.translatable("item.cupellation.tooltip.quality." + quality).formatted(formatting)));
                }
            }
        });
    }
}
