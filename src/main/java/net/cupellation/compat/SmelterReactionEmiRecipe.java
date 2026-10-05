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
import net.cupellation.data.SmelterReactionData;
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

public class SmelterReactionEmiRecipe implements EmiRecipe {

    private static final int SLAG_COLOR = 0x8C857F;

    private final SmelterReactionData reaction;
    private final Identifier id;

    public SmelterReactionEmiRecipe(SmelterReactionData reaction) {
        this.reaction = reaction;
        this.id = CupellationMain.identifierOf("/smelter_reaction/" + reaction.itemId().toUnderscoreSeparatedString()
                + "/" + reaction.fromMetal().toUnderscoreSeparatedString() + "_" + reaction.fromState().name().toLowerCase()
                + "_to_" + reaction.toMetal().toUnderscoreSeparatedString() + "_" + reaction.toState().name().toLowerCase());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CupellationEmiPlugin.REACTION_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(EmiIngredient.of(Ingredient.ofItems(Registries.ITEM.get(reaction.itemId()))));
        // Barren des Ausgangsmetalls -> Reaktion taucht bei "Verwendungen" des Barrens auf
        Identifier fromIngot = SmelterData.getIngotId(reaction.fromMetal());
        if (fromIngot != null) {
            inputs.add(EmiStack.of(Registries.ITEM.get(fromIngot)));
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        Identifier toIngot = SmelterData.getIngotId(reaction.toMetal());
        return toIngot != null ? List.of(EmiStack.of(Registries.ITEM.get(toIngot))) : List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 30;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(Ingredient.ofItems(Registries.ITEM.get(reaction.itemId()))), 0, 1);
        widgets.addText(Text.literal("+"), 21, 6, 0x888888, false);

        addFluid(widgets, reaction.fromMetal(), reaction.fromState(), 30, 2);

        widgets.addAnimatedTexture(EmiTexture.EMPTY_ARROW, 52, 3, 1000, true, false, false);

        addFluid(widgets, reaction.toMetal(), reaction.toState(), 84, 2);

        Identifier toIngot = SmelterData.getIngotId(reaction.toMetal());
        if (toIngot != null) {
            widgets.addSlot(EmiStack.of(Registries.ITEM.get(toIngot)), 108, 1).recipeContext(this);
        }

        widgets.addText(Text.translatable("emi.smelter.reaction.rate", reaction.amountPerItem()), 0, 22, 0x888888, false);
        if (reaction.minTemperature() > 0) {
            widgets.addText(Text.translatable("emi.smelter.degree", reaction.minTemperature()), 70, 22, 0xFF6633, false);
        }
    }

    private void addFluid(WidgetHolder widgets, Identifier metalId, SmelterReactionData.State state, int x, int y) {
        boolean slag = state == SmelterReactionData.State.SLAG;
        int color = slag ? SLAG_COLOR : SmelterData.getColor(metalId);
        Identifier texture = slag ? MoltenHelper.SLAG_TEXTURE : SmelterData.getTexture(metalId);

        widgets.addDrawable(x, y, 16, 16, (context, mouseX, mouseY, delta) -> {
            Sprite sprite = MoltenHelper.getFluidSprite(texture);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            SmelterScreen.drawTiledSprite(context, 0, 0, 16, 16, sprite, r, g, b);
        });

        MetalTypeData metal = SmelterData.getMetalType(metalId);
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.translatable(metal != null ? metal.name() : metalId.getPath()));
        if (slag) {
            tooltip.add(Text.translatable("block.cupellation.smelter.slag").formatted(Formatting.GRAY));
        }
        widgets.addTooltipText(tooltip, x, y, 16, 16);
    }
}
