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
import net.minecraft.util.Identifier;

import java.util.List;

public class MoldCastingEmiRecipe implements EmiRecipe {

    private final MoldItem resultMold;
    private final Identifier metalTypeId;
    private final Identifier moldableItemId;
    private final Identifier recipeId;

    public MoldCastingEmiRecipe(MoldItem resultMold, Identifier metalTypeId, Identifier moldableItemId) {
        this.resultMold = resultMold;
        this.metalTypeId = metalTypeId;
        this.moldableItemId = moldableItemId;
        this.recipeId = CupellationMain.identifierOf("/mold_casting/" + moldableItemId.getPath() + "_mold");
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
        return List.of(EmiIngredient.of(Ingredient.ofItems(Registries.ITEM.get(moldableItemId))));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(resultMold));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(Ingredient.ofItems(Registries.ITEM.get(moldableItemId))), 0, 1);

        widgets.addAnimatedTexture(EmiTexture.EMPTY_ARROW, 60, 3, CastingTableEntity.COOL_TIME * 20, true, false, false);

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
        MetalTypeData metal = SmelterData.getMetalType(metalTypeId);
        String metalName = metal != null ? metal.name() : metalTypeId.getPath();
        widgets.addText(Text.translatable("emi.smelter.yield", CastingTableEntity.CAPACITY), 38, 1, 0x888888, false);

        widgets.addTooltipText(List.of(Text.translatable(metalName)), 20, 3, 16, 16);

        widgets.addSlot(EmiStack.of(resultMold), 100, 1).recipeContext(this);
    }
}
