package net.cupellation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cupellation.data.SmelterData;
import net.cupellation.init.ItemInit;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @WrapOperation(method = {"canAcceptRecipeOutput", "craftRecipe"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/Recipe;getResult(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack getResultMixin(Recipe<?> instance, RegistryWrapper.WrapperLookup registriesLookup, Operation<ItemStack> original, DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        ItemStack result = original.call(instance, registriesLookup);
        if (!result.isEmpty() && SmelterData.hasItem(slots.get(0).getItem())) {
            result = result.copy();
            result.set(ItemInit.QUALITY_GRADE, 1);
        }
        return result;
    }
}
