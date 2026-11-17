package net.cupellation.compat;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.cupellation.CupellationMain;
import net.cupellation.block.screen.SmelterScreen;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.MoltenHelper;
import net.cupellation.misc.SlagHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlagSmeltingEmiRecipe implements EmiRecipe {

    private static final int SLAG_COLOR = 0x8C857F;

    private final Identifier metalTypeId;
    private final Identifier id;
    private final EmiStack input;

    public SlagSmeltingEmiRecipe(Identifier metalTypeId) {
        this.metalTypeId = metalTypeId;
        this.id = CupellationMain.identifierOf("/slag_smelting/" + metalTypeId.toUnderscoreSeparatedString());
        this.input = EmiStack.of(SlagHelper.createSlagStack(metalTypeId, 1));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CupellationEmiPlugin.SMELTER_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 20;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(input, 0, 1);
        widgets.addAnimatedTexture(EmiTexture.EMPTY_ARROW, 30, 3, SlagHelper.SMELT_TIME_PER_LAYER * 50, true, false, false);

        widgets.addDrawable(100, 3, 16, 16, (context, mouseX, mouseY, delta) -> {
            Sprite sprite = MoltenHelper.getSprite(MoltenHelper.SLAG_TEXTURE);
            float r = ((SLAG_COLOR >> 16) & 0xFF) / 255f;
            float g = ((SLAG_COLOR >> 8) & 0xFF) / 255f;
            float b = (SLAG_COLOR & 0xFF) / 255f;
            SmelterScreen.drawTiledSprite(context, 0, 0, 16, 16, sprite, r, g, b);
        });
        widgets.addTooltipText(List.of(Text.translatable("block.cupellation.smelter.slag_amount", SmelterData.getName(metalTypeId), SlagHelper.MB_PER_LAYER), Text.translatable("emi.smelter.slag_info")), 100, 3, 16, 16);
        widgets.addText(Text.translatable("emi.smelter.yield", SlagHelper.MB_PER_LAYER), 60, 13, 0x888888, false);
        widgets.addText(Text.translatable("emi.smelter.degree", SmelterData.getRequiredTemp(metalTypeId)), 21, 1, 0xFF6633, false);
    }
}
