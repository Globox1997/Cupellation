package net.cupellation.compat;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.cupellation.CupellationMain;
import net.cupellation.block.entity.CastingTableEntity;
import net.cupellation.block.screen.SmelterScreen;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.item.MoldItem;
import net.cupellation.misc.MoltenHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CastingEmiRecipe implements EmiRecipe {

    private final MoldItem mold;
    private final Identifier resultId;
    private final Identifier metalTypeId;
    private final Identifier recipeId;

    public CastingEmiRecipe(MoldItem mold, Identifier metalTypeId, Identifier resultId) {
        this.mold = mold;
        this.metalTypeId = metalTypeId;
        this.resultId = resultId;
        this.recipeId = CupellationMain.identifierOf("/casting/" + Registries.ITEM.getId(mold).getPath() + "/" + metalTypeId.toUnderscoreSeparatedString());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CupellationEmiPlugin.TABLE_CASTING_CATEGORY;
    }

    @Override
    public Identifier getId() {
        return recipeId;
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
    public List<EmiIngredient> getInputs() {
        return List.of(EmiIngredient.of(Ingredient.ofItems(mold)));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(Registries.ITEM.get(resultId)));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(Ingredient.ofItems(mold)), 0, 1);

        widgets.addAnimatedTexture(EmiTexture.EMPTY_ARROW, 74, 3, CastingTableEntity.COOL_TIME * 20, true, false, false);

        MetalTypeData metal = SmelterData.getMetalType(metalTypeId);
        int color = SmelterData.getColor(metalTypeId);
        Identifier texture = SmelterData.getTexture(metalTypeId);
        widgets.addSlot(EmiStack.EMPTY, 19, 1).recipeContext(this).drawBack(false);
        widgets.addDrawable(20, 2, 16, 16, (context, mouseX, mouseY, delta) -> {
            Sprite sprite = MoltenHelper.getFluidSprite(texture);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            SmelterScreen.drawTiledSprite(context, 0, 0, 16, 16, sprite, r, g, b);
        });

        String metalName = metal != null ? metal.name() : metalTypeId.getPath();
        widgets.addText(Text.translatable("emi.smelter.yield", mold.getMb()), 38, 1, 0x888888, false);
        widgets.addTooltipText(List.of(Text.translatable(metalName)), 20, 3, 16, 16);

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.translatable("block.cupellation.smelter.grades"));

        if (metal.highGrade() != null) {
            tooltip.add(Text.translatable("item.cupellation.tooltip.quality.3").formatted(Formatting.RED)
                    .append(Text.literal(": "))
                    .append(Text.translatable("block.cupellation.smelter.grade.info",
                            metal.highGrade().min(), metal.highGrade().max())));
        }
        if (metal.midGrade() != null) {
            tooltip.add(Text.translatable("item.cupellation.tooltip.quality.2").formatted(Formatting.GOLD)
                    .append(Text.literal(": "))
                    .append(Text.translatable("block.cupellation.smelter.grade.info",
                            metal.midGrade().min(), metal.midGrade().max())));
        }
        if (metal.lowGrade() != null) {
            tooltip.add(Text.translatable("item.cupellation.tooltip.quality.1").formatted(Formatting.YELLOW)
                    .append(Text.literal(": "))
                    .append(Text.translatable("block.cupellation.smelter.grade.info",
                            metal.lowGrade().min(), metal.lowGrade().max())));
        }

        int requiredTemp = SmelterData.getRequiredTemp(metalTypeId);
        widgets.addText(Text.translatable("emi.smelter.degree", requiredTemp), 38, 10, 0xFF6633, false);

        widgets.addTooltipText(tooltip, 38, 10, 32, 7);

        widgets.addSlot(EmiStack.of(Registries.ITEM.get(resultId)), 100, 1).recipeContext(this);
    }
}
