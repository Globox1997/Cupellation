package net.cupellation.compat;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.cupellation.CupellationMain;
import net.cupellation.block.screen.SmelterScreen;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.data.SmelterItemData;
import net.cupellation.misc.MoltenHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmeltingEmiRecipe implements EmiRecipe {

    private final SmelterItemData item;
    private final Identifier id;

    public SmeltingEmiRecipe(SmelterItemData item) {
        this.item = item;
        this.id = CupellationMain.identifierOf("/smelting/" + item.itemId().toUnderscoreSeparatedString());
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
        return List.of(EmiIngredient.of(Ingredient.ofItems(Registries.ITEM.get(item.itemId()))));
    }

    @Override
    public List<EmiStack> getOutputs() {
        MetalTypeData metal = SmelterData.getMetalType(item.metalTypeId());
        if (metal != null && metal.ingotId() != null) {
            return List.of(EmiStack.of(Registries.ITEM.get(metal.ingotId())));
        }
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
        widgets.addSlot(EmiIngredient.of(Ingredient.ofItems(Registries.ITEM.get(item.itemId()))), 0, 1);

        widgets.addAnimatedTexture(EmiTexture.EMPTY_ARROW, 30, 3, item.smeltTime() * 20, true, false, false);

        MetalTypeData metal = SmelterData.getMetalType(item.metalTypeId());


        if (metal != null) {
            if (metal.ingotId() != null) {
                widgets.addSlot(EmiStack.EMPTY, 99, 2).recipeContext(this).drawBack(false);
            }
            int color = SmelterData.getColor(item.metalTypeId());
            Identifier texture = SmelterData.getTexture(item.metalTypeId());

            widgets.addDrawable(100, 3, 16, 16, (context, mouseX, mouseY, delta) -> {
                Sprite sprite = MoltenHelper.getFluidSprite(texture);
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                SmelterScreen.drawTiledSprite(context, 0, 0, 16, 16, sprite, r, g, b);
            });
            widgets.addTooltipText(List.of(Text.translatable(metal.name())), 100, 3, 16, 16);
            widgets.addText(Text.translatable("emi.smelter.yield", item.yield()), 60, 13, 0x888888, false);

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
        }

        int requiredTemp = SmelterData.getRequiredTemp(item.metalTypeId());
        widgets.addText(Text.translatable("emi.smelter.degree", requiredTemp), 21, 1, 0xFF6633, false);
    }
}
