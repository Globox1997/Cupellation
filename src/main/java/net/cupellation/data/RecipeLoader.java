package net.cupellation.data;

import net.cupellation.CupellationMain;
import net.cupellation.init.ItemInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class RecipeLoader extends FabricRecipeProvider {

    public RecipeLoader(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        for (ToolMaterials material : ToolMaterials.values()) {
            if (material == ToolMaterials.WOOD) {
                continue;
            }
            String materialName = material.toString().toLowerCase();
            String recipeMaterial = materialName.equals("gold") ? "golden" : materialName;

            for (ItemInit.MoldType moldType : ItemInit.MOLD_TYPES) {
                if (!moldType.extraOutput()) {
                    continue;
                }
                Identifier headId = CupellationMain.identifierOf(recipeMaterial + "_" + moldType.suffix());
                Item headItem = Registries.ITEM.get(headId);
                if (headItem == Items.AIR) {
                    continue;
                }
                String toolName = switch (moldType.suffix()) {
                    case "axe_head" -> "axe";
                    case "hoe_head" -> "hoe";
                    case "pickaxe_head" -> "pickaxe";
                    case "shovel_head" -> "shovel";
                    case "sword_blade" -> "sword";
                    default -> null;
                };
                if (toolName == null) {
                    continue;
                }
                Item resultItem = Registries.ITEM.get(Identifier.ofVanilla(recipeMaterial + "_" + toolName));
                if (resultItem == Items.AIR) {
                    continue;
                }

                if (materialName.equals("stone")) {
                    RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.TOOLS, headItem, Blocks.COBBLESTONE);
                }
                switch (moldType.suffix()) {
                    case "sword_blade" -> ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, resultItem)
                            .pattern(" H ")
                            .pattern(" | ")
                            .input('H', headItem)
                            .input('|', Items.STICK)
                            .criterion(hasItem(headItem), conditionsFromItem(headItem))
                            .offerTo(exporter);
                    case "axe_head", "pickaxe_head", "hoe_head", "shovel_head" -> ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, resultItem)
                            .pattern(" H ")
                            .pattern(" | ")
                            .pattern(" | ")
                            .input('H', headItem)
                            .input('|', Items.STICK)
                            .criterion(hasItem(headItem), conditionsFromItem(headItem))
                            .offerTo(exporter);
                }
            }
        }
    }
}
