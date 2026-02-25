package net.cupellation.block;

import com.mojang.serialization.MapCodec;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SmelterBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public SmelterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return SmelterBlock.createCodec(SmelterBlock::new);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmelterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, BlockInit.SMELTER_ENTITY, world.isClient() ? SmelterBlockEntity::clientTick : SmelterBlockEntity::serverTick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(LIT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SmelterBlockEntity smelterBlockEntity && smelterBlockEntity.isFormed()) {
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            } else {
                player.sendMessage(Text.translatable("block.cupellation.smelter.incorrect"));
            }
        }
        return ActionResult.SUCCESS;
    }

}
