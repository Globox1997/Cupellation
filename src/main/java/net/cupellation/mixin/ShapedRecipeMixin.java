package net.cupellation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cupellation.init.ConfigInit;
import net.cupellation.init.ItemInit;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {

    @WrapOperation(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
    private ItemStack craftMixin(ItemStack instance, Operation<ItemStack> original, CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (instance.isDamageable()) {
            int totalQuality = 0;
            int count = 0;
            for (int i = 0; i < craftingRecipeInput.getSize(); i++) {
                ItemStack ingredient = craftingRecipeInput.getStackInSlot(i);
                if (!ingredient.isEmpty() && ingredient.contains(ItemInit.QUALITY_GRADE)) {
                    totalQuality += ingredient.get(ItemInit.QUALITY_GRADE);
                    count++;
                }
            }
            if (count > 0) {
                int finalQuality = totalQuality / count;
                instance.set(ItemInit.QUALITY_GRADE, finalQuality);

                int maxDamage = instance.getMaxDamage();
                int newDurability = switch (finalQuality) {
                    case 1 -> maxDamage * (100 - ConfigInit.CONFIG.lowGradeDurability) / 100;
                    case 2 -> maxDamage * (100 + ConfigInit.CONFIG.midGradeDurability) / 100;
                    case 3 -> maxDamage * (100 + ConfigInit.CONFIG.highGradeDurability) / 100;
                    default -> maxDamage;
                };

                instance.set(DataComponentTypes.MAX_DAMAGE, newDurability);

            }
        }
        return original.call(instance);
    }
}
