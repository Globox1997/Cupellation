package net.cupellation.block.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cupellation.CupellationMain;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.data.FuelData;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.MoltenHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SmelterScreen extends HandledScreen<SmelterScreenHandler> {

    private static final Identifier TEXTURE = CupellationMain.identifierOf("textures/gui/smelter.png");
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.ofVanilla("container/furnace/burn_progress");

    private static final int SLAG_COLOR = 0x8C857F;

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private static final int FLUID_X = 81, FLUID_Y = 19, FLUID_W = 48, FLUID_H = 48;

    private static final int TEMP_BAR_X = 78, TEMP_BAR_Y = 19;
    private static final int TEMP_BAR_W = 2, TEMP_BAR_H = 48;
    private static final int TEMP_U = 176, TEMP_V = 14;

    private static final int SMELT_BAR_X = 14, SMELT_BAR_W = 2, SMELT_BAR_H = 16;
    private static final int SMELT_U = 178, SMELT_V = 14;
    private static final int[] SMELT_SLOT_Y = {17, 35, 53};

    private static final int FLAME_X = 153, FLAME_Y = 19, FLAME_W = 14, FLAME_H = 14;
    private static final int FLAME_U = 176, FLAME_V = 0;

    private static final int ARROW_X = 41, ARROW_Y = 34, ARROW_W = 24, ARROW_H = 16;

    public SmelterScreen(SmelterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = GUI_WIDTH;
        this.backgroundHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (GUI_WIDTH - textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        handler.syncMetalType(MinecraftClient.getInstance().world);
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        drawFluidFill(context, x, y);

        drawBarFromBottom(context, x + TEMP_BAR_X, y + TEMP_BAR_Y, TEMP_BAR_W, TEMP_BAR_H, TEMP_U, TEMP_V,
                handler.getTemperature(), Math.max(1, handler.getMaxTemperature()));

        for (int i = 0; i < 3; i++) {
            drawBarFromBottom(context, x + SMELT_BAR_X, y + SMELT_SLOT_Y[i], SMELT_BAR_W, SMELT_BAR_H,
                    SMELT_U, SMELT_V, handler.getSmeltProgress(i), Math.max(1, handler.getSmeltTotal(i)));
        }

        drawFuelFlame(context, x, y);
        drawBurnArrow(context, x, y);
    }

    private void drawTiledSprite(DrawContext context, int destX, int destY, int destW, int destH, Sprite sprite, float r, float g, float b) {
        if (destW <= 0 || destH <= 0) {
            return;
        }
        float uMin = sprite.getMinU();
        float vMin = sprite.getMinV();
        float uSpan = sprite.getMaxU() - uMin;
        float vSpan = sprite.getMaxV() - vMin;

        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        for (int ty = 0; ty < destH; ty += 16) {
            int tileH = Math.min(16, destH - ty);
            float v0 = vMin;
            float v1 = vMin + (tileH / 16f) * vSpan;

            for (int tx = 0; tx < destW; tx += 16) {
                int tileW = Math.min(16, destW - tx);
                float u0 = uMin;
                float u1 = uMin + (tileW / 16f) * uSpan;

                float sx0 = destX + tx;
                float sy0 = destY + ty;
                float sx1 = sx0 + tileW;
                float sy1 = sy0 + tileH;

                buf.vertex(matrix, sx0, sy1, 0f).texture(u0, v1).color(r, g, b, 1f);
                buf.vertex(matrix, sx1, sy1, 0f).texture(u1, v1).color(r, g, b, 1f);
                buf.vertex(matrix, sx1, sy0, 0f).texture(u1, v0).color(r, g, b, 1f);
                buf.vertex(matrix, sx0, sy0, 0f).texture(u0, v0).color(r, g, b, 1f);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawFluidFill(DrawContext context, int x, int y) {
        Identifier[] metalTypeIds = handler.getMetalTypeIds();
        int[] metalAmounts = handler.getMetalAmounts();
        int[] slagAmounts = handler.getSlagAmounts();
        int cap = handler.getMaxCapacity();
        if (cap <= 0) return;

        Integer[] slots = new Integer[SmelterBlockEntity.MAX_METALS];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        Arrays.sort(slots, (a, b) -> {
            int da = metalTypeIds[a] != null ? SmelterData.getDensity(metalTypeIds[a]) : -1;
            int db = metalTypeIds[b] != null ? SmelterData.getDensity(metalTypeIds[b]) : -1;
            return Integer.compare(db, da);
        });

        float currentFill = 0f;

        for (int slot : slots) {
            if (metalTypeIds[slot] == null) {
                continue;
            }
            float metalFill = (float) metalAmounts[slot] / cap;
            if (metalFill <= 0) {
                continue;
            }
            int filledH = MathHelper.ceil(FLUID_H * metalFill);
            int topY = y + FLUID_Y + FLUID_H - MathHelper.ceil(FLUID_H * (currentFill + metalFill));

            Sprite sprite = MoltenHelper.getFluidSprite(SmelterData.getTexture(metalTypeIds[slot]));
            int color = SmelterData.getColor(metalTypeIds[slot]);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            drawTiledSprite(context, x + FLUID_X, topY, FLUID_W, filledH, sprite, r, g, b);
            currentFill += metalFill;
        }

        int totalSlag = handler.getTotalSlag();
        if (totalSlag > 0) {
            float slagFill = (float) totalSlag / cap;
            int slagFilledH = Math.max(1, MathHelper.ceil(FLUID_H * slagFill));
            int slagTopY = y + FLUID_Y + FLUID_H - MathHelper.ceil(FLUID_H * (currentFill + slagFill));

            Sprite slagSprite = MoltenHelper.getFluidSprite(MoltenHelper.SLAG_TEXTURE);
            float sr = ((SLAG_COLOR >> 16) & 0xFF) / 255f;
            float sg = ((SLAG_COLOR >> 8) & 0xFF) / 255f;
            float sb = (SLAG_COLOR & 0xFF) / 255f;

            drawTiledSprite(context, x + FLUID_X, slagTopY, FLUID_W, slagFilledH, slagSprite, sr, sg, sb);
        }
    }

    private void drawBarFromBottom(DrawContext context, int screenX, int screenY, int barW, int barH,
                                   int u, int v, int current, int max) {
        if (current <= 0) return;
        int filledH = MathHelper.ceil(barH * ((float) current / max));
        int offsetY = barH - filledH;
        context.drawTexture(TEXTURE, screenX, screenY + offsetY, u, v + offsetY, barW, filledH);
    }

    private void drawFuelFlame(DrawContext context, int x, int y) {
        if (!handler.isBurning()) return;
        int litH = MathHelper.ceil(FLAME_H * handler.getFuelPercent());
        if (litH <= 0) return;
        int offsetY = FLAME_H - litH;
        context.drawTexture(TEXTURE, x + FLAME_X, y + FLAME_Y + offsetY, FLAME_U, FLAME_V + offsetY, FLAME_W, litH);
    }

    private void drawBurnArrow(DrawContext context, int x, int y) {
        int arrowW = MathHelper.ceil(ARROW_W * handler.getSmeltPercent(0));
        if (arrowW <= 0) return;
        context.drawGuiTexture(BURN_PROGRESS_SPRITE, ARROW_W, ARROW_H, 0, 0, x + ARROW_X, y + ARROW_Y, arrowW, ARROW_H);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        context.drawText(textRenderer, this.playerInventoryTitle,
                this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
        context.drawText(this.textRenderer, Text.translatable("block.cupellation.smelter.degree"), 131, 18, 0xFFFFFF, true);

        int relX = mouseX - (this.width - GUI_WIDTH) / 2;
        int relY = mouseY - (this.height - GUI_HEIGHT) / 2;

        drawFluidTooltip(context, relX, relY);
        drawGradeInfo(context, relX, relY);
        drawTemperatureTooltip(context, relX, relY);
        drawSmeltingTooltip(context, relX, relY);
        drawFuelTooltip(context, relX, relY);
    }

    private void drawFuelTooltip(DrawContext context, int relX, int relY) {
        if (relX >= 153 && relX <= 153 + 13 && relY >= 19 && relY <= 19 + 13) {
            List<Text> tooltip = new ArrayList<>();
            for (FuelData fuelData : SmelterData.allFuels()) {
                tooltip.add(Registries.ITEM.get(fuelData.itemId()).getName().copyContentOnly()
                        .append(Text.literal(": "))
                        .append(Text.translatable("block.cupellation.smelter.degree.info", fuelData.maxTemperature())));
            }
            if (!tooltip.isEmpty()) {
                context.drawTooltip(textRenderer, tooltip, relX, relY);
            }
        }
    }

    private void drawGradeInfo(DrawContext context, int relX, int relY) {
        if (relX < 131 || relX > 131 + 11 || relY < 18 || relY > 18 + 8) return;

        List<Text> tooltip = new ArrayList<>();
        Identifier[] metalTypeIds = handler.getMetalTypeIds();

        for (Identifier metalTypeId : metalTypeIds) {
            if (metalTypeId == null) {
                continue;
            }
            MetalTypeData metalTypeData = SmelterData.getMetalType(metalTypeId);
            if (metalTypeData == null || !metalTypeData.hasGrades()) {
                continue;
            }
            tooltip.add(Text.literal(SmelterData.getName(metalTypeId))
                    .formatted(Formatting.WHITE));
            tooltip.add(Text.translatable("block.cupellation.smelter.grades"));

            if (metalTypeData.highGrade() != null) {
                tooltip.add(Text.translatable("item.cupellation.tooltip.quality.3").formatted(Formatting.RED)
                        .append(Text.literal(": "))
                        .append(Text.translatable("block.cupellation.smelter.grade.info",
                                metalTypeData.highGrade().min(), metalTypeData.highGrade().max())));
            }
            if (metalTypeData.midGrade() != null) {
                tooltip.add(Text.translatable("item.cupellation.tooltip.quality.2").formatted(Formatting.GOLD)
                        .append(Text.literal(": "))
                        .append(Text.translatable("block.cupellation.smelter.grade.info",
                                metalTypeData.midGrade().min(), metalTypeData.midGrade().max())));
            }
            if (metalTypeData.lowGrade() != null) {
                tooltip.add(Text.translatable("item.cupellation.tooltip.quality.1").formatted(Formatting.YELLOW)
                        .append(Text.literal(": "))
                        .append(Text.translatable("block.cupellation.smelter.grade.info",
                                metalTypeData.lowGrade().min(), metalTypeData.lowGrade().max())));
            }
        }

        if (!tooltip.isEmpty()) {
            context.drawTooltip(textRenderer, tooltip, relX, relY);
        }
    }

    private void drawFluidTooltip(DrawContext context, int relX, int relY) {
        if (relX < FLUID_X || relX > FLUID_X + FLUID_W || relY < FLUID_Y || relY > FLUID_Y + FLUID_H) {
            return;
        }
        Identifier[] metalTypeIds = handler.getMetalTypeIds();
        int[] metalAmounts = handler.getMetalAmounts();
        int[] slagAmounts = handler.getSlagAmounts();
        int cap = handler.getMaxCapacity();

        List<Text> tooltip = new ArrayList<>();
        int totalFluid = 0;

        for (int i = 0; i < SmelterBlockEntity.MAX_METALS; i++) {
            if (metalTypeIds[i] == null) continue;
            if (metalAmounts[i] > 0) {
                int density = SmelterData.getDensity(metalTypeIds[i]);
                tooltip.add(Text.literal(SmelterData.getName(metalTypeIds[i]) + ": "
                                + metalAmounts[i] + " mB")
                        .append(Text.literal(" (")
                                .append(Text.translatable("block.cupellation.smelter.density"))
                                .append(Text.literal(": " + density + ")"))
                                .formatted(Formatting.GRAY)));
                totalFluid += metalAmounts[i];
            }
            if (slagAmounts[i] > 0) {
                tooltip.add(Text.translatable("block.cupellation.smelter.slag")
                        .append(Text.literal(" (" + SmelterData.getName(metalTypeIds[i])
                                + "): " + slagAmounts[i] + " mB"))
                        .formatted(Formatting.GRAY));
                totalFluid += slagAmounts[i];
            }
        }

        tooltip.add(Text.translatable("block.cupellation.smelter.capacity").copyContentOnly().append(Text.literal(": " + totalFluid + " / " + cap + " mB"))
                .formatted(Formatting.DARK_GRAY));

        if (!tooltip.isEmpty()) {
            context.drawTooltip(textRenderer, tooltip, relX, relY);
        }
    }

    private void drawTemperatureTooltip(DrawContext context, int relX, int relY) {
        if (relX >= TEMP_BAR_X && relX <= TEMP_BAR_X + TEMP_BAR_W && relY >= TEMP_BAR_Y && relY <= TEMP_BAR_Y + TEMP_BAR_H) {
            context.drawTooltip(textRenderer, Text.translatable("block.cupellation.smelter.temperature").copyContentOnly().append(Text.literal(": " + handler.getTemperature() + " / " + handler.getMaxTemperature() + " ").append(Text.translatable("block.cupellation.smelter.degree"))), relX, relY);
        }
    }

    private void drawSmeltingTooltip(DrawContext context, int relX, int relY) {
        for (int i = 0; i < 3; i++) {
            int barY = SMELT_SLOT_Y[i];
            if (relX >= SMELT_BAR_X && relX <= SMELT_BAR_X + SMELT_BAR_W && relY >= barY && relY <= barY + SMELT_BAR_H) {
                int total = handler.getSmeltTotal(i);
                if (total > 0) {
                    int percent = (int) (handler.getSmeltPercent(i) * 100);
                    context.drawTooltip(textRenderer, Text.translatable("block.cupellation.smelter.smelting").copyContentOnly().append(Text.literal(": " + percent + "% (" + handler.getSmeltProgress(i) + "/" + total + " ticks)")), relX, relY);
                }
                break;
            }
        }
    }
}