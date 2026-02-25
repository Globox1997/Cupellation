package net.cupellation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cupellation.data.SmelterData;
import net.cupellation.init.ItemInit;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @WrapOperation(method = "craftRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
    private static ItemStack craftRecipeMixin(ItemStack instance, Operation<ItemStack> original, DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        ItemStack itemStack = slots.get(0);
        if (SmelterData.getItemData(itemStack.getItem()) != null) {
            instance.set(ItemInit.QUALITY_GRADE, 1);
        }
        return original.call(instance);
    }
}
