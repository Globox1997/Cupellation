package net.cupellation.compat;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.cupellation.CupellationMain;
import net.cupellation.api.MoldType;
import net.cupellation.init.ItemInit;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ClayImprintEmiRecipe implements EmiRecipe {

    private final Item blankClayMold;
    private final Item imprintedClayMold;
    private final MoldType moldType;
    private final Identifier recipeId;

    public ClayImprintEmiRecipe(Item imprintedClayMold, MoldType moldType) {
        this.blankClayMold = ItemInit.CLAY_MOLD;
        this.imprintedClayMold = imprintedClayMold;
        this.moldType = moldType;
        this.recipeId = CupellationMain.identifierOf("/clay_imprint/" + moldType.suffix());
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
        return 94;
    }

    @Override
    public int getDisplayHeight() {
        return 20;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<Item> stamps = getStamps();

        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(EmiStack.of(blankClayMold));
        if (!stamps.isEmpty()) {
            inputs.add(EmiIngredient.of(stamps.stream().map(i -> (EmiIngredient) EmiStack.of(i)).toList()));
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(imprintedClayMold));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiStack.of(blankClayMold), 0, 1);
        widgets.addText(Text.literal("+"), 20, 5, 0x888888, false);

        List<Item> stamps = getStamps();
        if (!stamps.isEmpty()) {
            widgets.addSlot(EmiIngredient.of(stamps.stream().map(i -> (EmiIngredient) EmiStack.of(i)).toList()), 28, 1);
        }

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 50, 3);
        widgets.addSlot(EmiStack.of(imprintedClayMold), 76, 1).recipeContext(this);
    }

    private List<Item> getStamps() {
        return MoldStampFinder.findStamps(moldType);
    }

    public boolean hasNoStamps() {
        return getStamps().isEmpty();
    }

}