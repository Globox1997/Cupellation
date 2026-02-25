package net.cupellation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SmelterFaucet extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BooleanProperty.of("open");

    private static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7, 5, 0, 9, 7, 6), Block.createCuboidShape(9, 5, 0, 11, 10, 6),
            Block.createCuboidShape(5, 5, 0, 7, 10, 6));

    private static final VoxelShape NORTH_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7, 5, 10, 9, 7, 16), Block.createCuboidShape(5, 5, 10, 7, 10, 16),
            Block.createCuboidShape(9, 5, 10, 11, 10, 16));

    private static final VoxelShape EAST_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 5, 7, 6, 7, 9), Block.createCuboidShape(0, 5, 5, 6, 10, 7),
            Block.createCuboidShape(0, 5, 9, 6, 10, 11));

    private static final VoxelShape WEST_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(10, 5, 7, 16, 7, 9), Block.createCuboidShape(10, 5, 5, 16, 10, 7),
            Block.createCuboidShape(10, 5, 9, 16, 10, 11));

    public SmelterFaucet(Settings settings) {
        super(settings);
        this.setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(OPEN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos drainPos = ctx.getBlockPos().offset(ctx.getSide().getOpposite(), 1);
        BlockState drainState = ctx.getWorld().getBlockState(drainPos);

        if (drainState.getBlock() instanceof SmelterDrain) {
            if (SmelterDrain.isFaucetSide(drainState, ctx.getSide())) {
                return this.getDefaultState().with(FACING, ctx.getSide()).with(OPEN, false);
            }
            return null;
        } else {
            return null;
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {

        return switch (state.get(FACING)) {
            case Direction.NORTH -> NORTH_SHAPE;
            case Direction.EAST -> EAST_SHAPE;
            case Direction.SOUTH -> SOUTH_SHAPE;
            case Direction.WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos drainPos = pos.offset(facing.getOpposite());
        BlockState drainState = world.getBlockState(drainPos);
        return canAttachTo(drainState, facing);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        boolean nowOpen = !state.get(OPEN);
        world.setBlockState(pos, state.with(OPEN, nowOpen));

        Direction facing = state.get(FACING);
        BlockPos drainPos = pos.offset(facing.getOpposite());
        world.updateNeighbor(drainPos, this, pos);

        return ActionResult.SUCCESS;
    }

    private boolean canAttachTo(BlockState drainState, Direction faucetFacing) {
        if (!(drainState.getBlock() instanceof SmelterDrain)) {
            return false;
        }
        return SmelterDrain.isFaucetSide(drainState, faucetFacing);
    }

    public static boolean isOpen(BlockState state) {
        return state.get(OPEN);
    }

    public static BlockPos getDrainPos(BlockState faucetState, BlockPos faucetPos) {
        return faucetPos.offset(faucetState.get(FACING).getOpposite());
    }
}