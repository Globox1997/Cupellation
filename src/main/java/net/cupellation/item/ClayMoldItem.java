package net.cupellation.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ClayMoldItem extends Item {

    public ClayMoldItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.cupellation.clay_mold.tooltip.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.cupellation.clay_mold.tooltip.2").formatted(Formatting.GRAY));
    }
}