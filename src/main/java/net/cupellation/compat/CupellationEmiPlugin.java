package net.cupellation.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.cupellation.CupellationMain;
import net.cupellation.data.MetalTypeData;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ItemInit;
import net.cupellation.item.MoldItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CupellationEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory SMELTER_CATEGORY = new EmiRecipeCategory(CupellationMain.identifierOf("smelter"), EmiStack.of(BlockInit.DEEPSLATE_BRICK_SMELTER));
    public static final EmiRecipeCategory TABLE_CASTING_CATEGORY = new EmiRecipeCategory(CupellationMain.identifierOf("table_casting"), EmiStack.of(BlockInit.DEEPSLATE_BRICK_CASTING_TABLE));
    public static final EmiRecipeCategory BASIN_CASTING_CATEGORY = new EmiRecipeCategory(CupellationMain.identifierOf("basin_casting"), EmiStack.of(BlockInit.DEEPSLATE_BRICK_CASTING_BASIN));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(SMELTER_CATEGORY);
        registry.addWorkstation(SMELTER_CATEGORY, EmiStack.of(BlockInit.DEEPSLATE_BRICK_SMELTER));

        for (var entry : SmelterData.getAllItems().entrySet()) {
            registry.addRecipe(new SmeltingEmiRecipe(entry.getValue()));
        }

        registry.addCategory(TABLE_CASTING_CATEGORY);
        registry.addWorkstation(TABLE_CASTING_CATEGORY, EmiStack.of(BlockInit.DEEPSLATE_BRICK_CASTING_TABLE));

        for (Item moldItem : ItemInit.MOLDS) {
            if (!(moldItem instanceof MoldItem mold)) {
                continue;
            }
            for (var metalEntry : SmelterData.getAllMetals().entrySet()) {
                Identifier metalTypeId = metalEntry.getKey();

                if (!mold.canCastWith(metalTypeId)) {
                    continue;
                }
                Identifier resultId = mold.resolveResultId(metalTypeId);
                if (resultId == null) {
                    continue;
                }
                registry.addRecipe(new CastingEmiRecipe(mold, metalTypeId, resultId));
            }
        }

        for (Item moldItem : ItemInit.MOLDS) {
            if (!(moldItem instanceof MoldItem mold)) {
                continue;
            }
            Identifier moldingType = mold.getMoldingMetalTypeId();
            if (moldingType == null) {
                continue;
            }
            for (Item moldable : ItemInit.MOLDABLES) {
                Identifier moldableId = Registries.ITEM.getId(moldable);
                if (!moldableId.getPath().endsWith("_" + mold.getOutputSuffix())) {
                    continue;
                }
                registry.addRecipe(new MoldCastingEmiRecipe(mold, moldingType, moldableId));
            }
        }

        registry.addCategory(BASIN_CASTING_CATEGORY);
        registry.addWorkstation(BASIN_CASTING_CATEGORY, EmiStack.of(BlockInit.DEEPSLATE_BRICK_CASTING_BASIN));

        for (var entry : SmelterData.getAllMetals().entrySet()) {
            MetalTypeData metal = entry.getValue();
            if (metal.blockId() == null) {
                continue;
            }
            if (!Registries.ITEM.containsId(metal.blockId())) {
                continue;
            }
            registry.addRecipe(new BasinCastingEmiRecipe(entry.getKey()));
        }
    }
}
