package net.cupellation.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

public class BrickMoldItem extends MoldItem {

    public BrickMoldItem(Identifier metalType, int mb, String suffix, Set<Identifier> blacklist, Settings settings) {
        super(metalType, mb, suffix, blacklist, settings);
    }

    @Override
    public boolean canCastWith(Identifier metalType) {
        return true;
    }

    @Override
    public boolean isSingleUse() {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.cupellation.brick_mold.tooltip.single_use").formatted(Formatting.GRAY));
    }
}