package net.cupellation.misc;

import net.cupellation.CupellationMain;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MoltenHelper {

    public static final Identifier SLAG_TEXTURE = CupellationMain.identifierOf("fluid/slag");
    public static final float SLAG_R = 0.55f;
    public static final float SLAG_G = 0.52f;
    public static final float SLAG_B = 0.50f;

    public static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (rr << 16) | (rg << 8) | rb;
    }

    public static int getPourColor(MetalTypeData metal, MetalTypeData.PourState pourState) {
        return switch (pourState) {
            case TOO_COLD -> metal.cooledColor();
            case LOW -> lerpColor(metal.color(), metal.cooledColor(), 0.35f);
            case MID -> metal.color();
            case HIGH -> lerpColor(metal.color(), 0xFFFFFF, 0.25f);
            case OVERHEATED -> lerpColor(metal.color(), 0xFFFFFF, 0.55f);
        };
    }

    public static Text getPourStateText(MetalTypeData.PourState pourState) {
        return switch (pourState) {
            case TOO_COLD -> Text.translatable("block.cupellation.faucet.too_cold").formatted(Formatting.AQUA);
            case LOW -> Text.translatable("item.cupellation.tooltip.quality.1").formatted(Formatting.YELLOW);
            case MID -> Text.translatable("item.cupellation.tooltip.quality.2").formatted(Formatting.GOLD);
            case HIGH -> Text.translatable("item.cupellation.tooltip.quality.3").formatted(Formatting.RED);
            case OVERHEATED -> Text.translatable("block.cupellation.faucet.overheating").formatted(Formatting.DARK_RED);
        };
    }

    public static Sprite getFluidSprite(Identifier metalTypeId) {
        return getSprite(SmelterData.getTexture(metalTypeId));
    }

    public static Sprite getSprite(Identifier textureId) {
        return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(textureId);
    }

    public static int getGrade(World world, BlockPos blockPos, Identifier metalTypeId) {
        if (world.getBlockEntity(blockPos) instanceof SmelterBlockEntity smelterBlockEntity && SmelterData.getMetalType(metalTypeId) instanceof MetalTypeData metalTypeData) {
            switch (metalTypeData.getGradeAt(smelterBlockEntity.getTemperature())) {
                case LOW -> {
                    return 1;
                }
                case MID -> {
                    return 2;
                }
                case HIGH -> {
                    return 3;
                }
            }
        }
        return 1;
    }

}
