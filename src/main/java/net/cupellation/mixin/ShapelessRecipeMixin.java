package net.cupellation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cupellation.data.SmelterData;
import net.cupellation.init.ItemInit;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {

    @WrapOperation(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
    private ItemStack craftMixin(ItemStack instance, Operation<ItemStack> original, CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        int totalQuality = 0;
        int count = 0;

        for (int i = 0; i < craftingRecipeInput.getSize(); i++) {
            ItemStack ingredient = craftingRecipeInput.getStackInSlot(i);
            if (!ingredient.isEmpty()) {
                if (ingredient.contains(ItemInit.QUALITY_GRADE)) {
                    totalQuality += ingredient.get(ItemInit.QUALITY_GRADE);
                    count++;
                } else if (SmelterData.hasItem(ingredient.getItem())) {
                    totalQuality += 1;
                    count++;
                }
            }
        }
        if (count > 0) {
            int finalQuality = totalQuality / count;
            instance.set(ItemInit.QUALITY_GRADE, finalQuality);
        }
        return original.call(instance);
    }
}
