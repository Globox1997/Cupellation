package net.cupellation.block.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cupellation.CupellationMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SmelterScreen extends HandledScreen<SmelterScreenHandler> {

    private static final Identifier TEXTURE = CupellationMain.identifierOf("textures/gui/smelter.png");
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.ofVanilla("container/furnace/burn_progress");

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

        drawBarFromBottom(context, x + TEMP_BAR_X, y + TEMP_BAR_Y, TEMP_BAR_W, TEMP_BAR_H, TEMP_U, TEMP_V, handler.getTemperature(), Math.max(1, handler.getMaxTemperature()));

        for (int i = 0; i < 3; i++) {
            drawBarFromBottom(context, x + SMELT_BAR_X, y + SMELT_SLOT_Y[i], SMELT_BAR_W, SMELT_BAR_H, SMELT_U, SMELT_V, handler.getSmeltProgress(i), Math.max(1, handler.getSmeltTotal(i)));
        }

        drawFuelFlame(context, x, y);
        drawBurnArrow(context, x, y);
    }

    private void drawFluidFill(DrawContext context, int x, int y) {
        float fill = handler.getFillPercent();
        if (fill <= 0f) return;

        int filledH = MathHelper.ceil(FLUID_H * fill);
        int topY = y + FLUID_Y + (FLUID_H - filledH);

        Identifier spriteId = handler.getMetalTexture();
        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(spriteId);

        int color = handler.getMetalColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, g, b, 1f);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

        for (int tileX = 0; tileX < FLUID_W; tileX += 16) {
            for (int tileY = 0; tileY < filledH; tileY += 16) {
                int drawW = Math.min(16, FLUID_W - tileX);
                int drawH = Math.min(16, filledH - tileY);
                context.drawSprite(x + FLUID_X + tileX, topY + tileY, 0, drawW, drawH, sprite);
            }
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawBarFromBottom(DrawContext context, int screenX, int screenY, int barW, int barH, int u, int v, int current, int max) {
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

        int relX = mouseX - (this.width - GUI_WIDTH) / 2;
        int relY = mouseY - (this.height - GUI_HEIGHT) / 2;

        drawFluidTooltip(context, relX, relY);
        drawTemperatureTooltip(context, relX, relY);
        drawSmeltingTooltip(context, relX, relY);
    }

    private void drawFluidTooltip(DrawContext context, int relX, int relY) {
        if (relX >= FLUID_X && relX <= FLUID_X + FLUID_W && relY >= FLUID_Y && relY <= FLUID_Y + FLUID_H) {
            int mb = handler.getMoltenMetal();
            int cap = handler.getMaxCapacity();
            context.drawTooltip(textRenderer, Text.literal(handler.getMetalName() + ": " + mb + " / " + cap + " mB"), relX, relY);
        }
    }

    private void drawTemperatureTooltip(DrawContext context, int relX, int relY) {
        if (relX >= TEMP_BAR_X && relX <= TEMP_BAR_X + TEMP_BAR_W && relY >= TEMP_BAR_Y && relY <= TEMP_BAR_Y + TEMP_BAR_H) {
            context.drawTooltip(textRenderer, Text.literal("Temperature: " + handler.getTemperature() + " / " + handler.getMaxTemperature() + " °C"), relX, relY);
        }
    }

    private void drawSmeltingTooltip(DrawContext context, int relX, int relY) {
        for (int i = 0; i < 3; i++) {
            int barY = SMELT_SLOT_Y[i];
            if (relX >= SMELT_BAR_X && relX <= SMELT_BAR_X + SMELT_BAR_W && relY >= barY && relY <= barY + SMELT_BAR_H) {
                int total = handler.getSmeltTotal(i);
                if (total > 0) {
                    int percent = (int) (handler.getSmeltPercent(i) * 100);
                    context.drawTooltip(textRenderer, Text.literal("Smelting: " + percent + "% (" + handler.getSmeltProgress(i) + "/" + total + " ticks)"), relX, relY);
                }
                break;
            }
        }
    }
}
