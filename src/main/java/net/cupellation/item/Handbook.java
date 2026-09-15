package net.cupellation.item;

import net.cupellation.CupellationMain;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

public class Handbook extends Item {

    private final boolean isPatchouliLoaded = FabricLoader.getInstance().isModLoaded("patchouli");

    public Handbook(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (isPatchouliLoaded) {
            if (!world.isClient()) {
                PatchouliAPI.get().openBookGUI((ServerPlayerEntity) user, CupellationMain.identifierOf("cupellation"));
                return TypedActionResult.success(user.getStackInHand(hand));
            }
        }
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        if (!isPatchouliLoaded) {
            tooltip.add(Text.translatable("item.cupellation.patchouli_book.tooltip"));
        }
        tooltip.add(Text.translatable("book.byAuthor", "Globox_Z").formatted(Formatting.GRAY));
    }

}
