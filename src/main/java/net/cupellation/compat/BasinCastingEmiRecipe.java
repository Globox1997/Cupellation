package net.cupellation.compat;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.cupellation.CupellationMain;
import net.cupellation.block.entity.CastingBasinEntity;
import net.cupellation.block.screen.SmelterScreen;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.misc.MoltenHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BasinCastingEmiRecipe implements EmiRecipe {

    private final Identifier metalTypeId;
    private final Identifier recipeId;

    public BasinCastingEmiRecipe(Identifier metalTypeId) {
        this.metalTypeId = metalTypeId;
        this.recipeId = CupellationMain.identifierOf("/basin_casting/" + metalTypeId.toUnderscoreSeparatedString());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CupellationEmiPlugin.BASIN_CASTING_CATEGORY;
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
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        MetalTypeData metal = SmelterData.getMetalType(metalTypeId);
        if (metal != null && metal.blockId() != null) {
            return List.of(EmiStack.of(Registries.ITEM.get(metal.blockId())));
        }
        return List.of();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        widgets.addAnimatedTexture(EmiTexture.EMPTY_ARROW, 30, 3, CastingBasinEntity.COOL_TIME * 20, true, false, false);

        int color = SmelterData.getColor(metalTypeId);
        Identifier texture = SmelterData.getTexture(metalTypeId);

        widgets.addSlot(EmiStack.EMPTY, 0, 1).drawBack(false);
        widgets.addDrawable(1, 2, 16, 16, (context, mouseX, mouseY, delta) -> {
            Sprite sprite = MoltenHelper.getFluidSprite(texture);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            SmelterScreen.drawTiledSprite(context, 0, 0, 16, 16, sprite, r, g, b);
        });

        MetalTypeData metal = SmelterData.getMetalType(metalTypeId);
        widgets.addTooltipText(List.of(Text.translatable(metal.name())), 0, 2, 16, 16);

        widgets.addText(Text.translatable("emi.smelter.yield", CastingBasinEntity.CAPACITY), 60, 13, 0x888888, false);

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

        widgets.addTooltipText(tooltip, 21, 1, 32, 7);

        widgets.addText(Text.translatable("emi.smelter.degree", metal.requiredTemp()), 21, 1, 0xFF6633, false);

        if (metal.blockId() != null) {
            widgets.addSlot(EmiStack.of(Registries.ITEM.get(metal.blockId())), 100, 1).recipeContext(this);
        }
    }
}
