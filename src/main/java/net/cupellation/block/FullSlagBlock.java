package net.cupellation.block;

import com.mojang.serialization.MapCodec;
import net.cupellation.block.entity.SlagBlockEntity;
import net.cupellation.init.ItemInit;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FullSlagBlock extends BlockWithEntity {

    public static final MapCodec<FullSlagBlock> CODEC = createCodec(FullSlagBlock::new);

    public FullSlagBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SlagBlockEntity(pos, state);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        if (world.getBlockEntity(pos) instanceof SlagBlockEntity slag && !slag.getLayers().isEmpty()) {
            stack.set(ItemInit.SLAG_METALS, List.copyOf(slag.getLayers()));
        }
        return stack;
    }
}
