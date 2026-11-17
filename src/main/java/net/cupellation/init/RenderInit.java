package net.cupellation.init;

import net.cupellation.api.CupellationAPI;
import net.cupellation.api.SmelterType;
import net.cupellation.block.entity.SlagBlockEntity;
import net.cupellation.block.render.CastingBasinRenderer;
import net.cupellation.block.render.CastingTableRenderer;
import net.cupellation.block.render.SmelterBlockRenderer;
import net.cupellation.block.render.SmelterFaucetRenderer;
import net.cupellation.block.screen.SmelterScreen;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.SlagHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockEntityRendererFactories.register(BlockInit.SMELTER_ENTITY, SmelterBlockRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.CASTING_BASIN_ENTITY, CastingBasinRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.SMELTER_FAUCET_ENTITY, SmelterFaucetRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.CASTING_TABLE_ENTITY, CastingTableRenderer::new);

        for (SmelterType type : CupellationAPI.getSmelterTypes()) {
            BlockRenderLayerMap.INSTANCE.putBlock(type.smelter(), RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(type.faucet(), RenderLayer.getCutoutMipped());
            BlockRenderLayerMap.INSTANCE.putBlock(type.castingBasin(), RenderLayer.getCutout());
            BlockRenderLayerMap.INSTANCE.putBlock(type.castingTable(), RenderLayer.getCutout());
        }
        
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_GLASS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DEEPSLATE_BRICK_LEVER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.RED_NETHER_BRICK_GLASS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.RED_NETHER_BRICK_LEVER, RenderLayer.getCutout());

        HandledScreens.register(BlockInit.SMELTER_SCREEN_HANDLER, SmelterScreen::new);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null && world.getBlockEntity(pos) instanceof SlagBlockEntity slag) {
                return SlagHelper.getTint(slag.getLayers());
            }
            return SlagHelper.getTint(null);
        }, BlockInit.SLAG, BlockInit.SLAG_BLOCK);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xFF000000 | SlagHelper.getTint(stack.get(ItemInit.SLAG_METALS)), BlockInit.SLAG, BlockInit.SLAG_BLOCK);

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            List<Identifier> slagLayers = stack.get(ItemInit.SLAG_METALS);
            if (slagLayers != null) {
                for (Map.Entry<Identifier, Integer> entry : SlagHelper.countLayers(slagLayers).entrySet()) {
                    lines.add(Text.translatable("item.cupellation.tooltip.slag", SmelterData.getName(entry.getKey()), entry.getValue() * SlagHelper.MB_PER_LAYER).formatted(Formatting.GRAY));
                }
            }
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
