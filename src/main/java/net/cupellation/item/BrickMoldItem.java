package net.cupellation.item;

import net.cupellation.api.strategy.MoldResultStrategy;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class BrickMoldItem extends MoldItem {

    public BrickMoldItem(@Nullable Identifier moldingMetalTypeId, int mb, String outputSuffix, Set<Identifier> blacklist, @Nullable MoldResultStrategy strategy, Settings settings) {
        super(moldingMetalTypeId, mb, outputSuffix, blacklist, strategy, settings);
    }

    public BrickMoldItem(Identifier metalType, int mb, String suffix, Set<Identifier> blacklist, Settings settings) {
        this(metalType, mb, suffix, blacklist, null, settings);
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