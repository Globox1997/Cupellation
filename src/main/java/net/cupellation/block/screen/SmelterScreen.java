package net.cupellation.block.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cupellation.CupellationMain;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SmelterScreen extends HandledScreen<SmelterScreenHandler> {

    private static final Identifier TEXTURE = CupellationMain.identifierOf("textures/gui/smelter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

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
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        SmelterBlockEntity be = handler.getBlockEntity();
        if (be != null) {
            drawProgressBar(context, be, x, y);
            drawFluidBar(context, be, x, y);
        }
    }

    private void drawProgressBar(DrawContext context, SmelterBlockEntity be, int x, int y) {
        int progress = be.getSmeltProgress();
        int maxProgress = 200;
        int barWidth = (int) (24 * ((float) progress / maxProgress));

        context.drawTexture(TEXTURE, x + 79, y + 34, 176, 0, barWidth, 17);
    }

    private void drawFluidBar(DrawContext context, SmelterBlockEntity be, int x, int y) {
        float fillPercent = be.getFillPercent();
        int barHeight = (int) (52 * fillPercent);
        int barY = y + 17 + (52 - barHeight);

        context.drawTexture(TEXTURE, x + 148, y + 17, 176, 17, 16, 52);

        if (barHeight > 0) {
            context.drawTexture(TEXTURE, x + 148, barY, 192, 17 + (52 - barHeight), 16, barHeight);
        }

        if (isMouseOver(x + 148, y + 17, 16, 52)) {
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        context.drawText(textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);

        SmelterBlockEntity be = handler.getBlockEntity();
        if (be != null) {
            int relX = mouseX - (this.width - GUI_WIDTH) / 2;
            int relY = mouseY - (this.height - GUI_HEIGHT) / 2;

            if (relX >= 148 && relX <= 164 && relY >= 17 && relY <= 69) {
                int mb = be.getMoltenMetal();
                String metalName = getMetalName(be.getMetalType());
                context.drawTooltip(textRenderer,
                        Text.literal(metalName + ": " + mb + " / " + be.getMaxCapacity() + " mB"),
                        relX, relY);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private boolean isMouseOver(int x, int y, int width, int height) {
        return false;
    }

    private String getMetalName(int metalType) {
        return switch (metalType) {
            case 1 -> "Molten Iron";
            case 2 -> "Molten Gold";
            default -> "Unknown";
        };
    }
}