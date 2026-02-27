package net.cupellation.block;

import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.block.entity.SmelterFaucetEntity;
import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ConfigInit;
import net.cupellation.init.TagInit;
import net.cupellation.misc.CastingEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SmelterFaucet extends Block implements BlockEntityProvider {

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

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmelterFaucetEntity(pos, state);
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
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        boolean nowOpen = !state.get(OPEN);

        if (nowOpen) {
            CastingEntity castingEntityBelow = findCastingEntityBelow(world, pos);
            if (castingEntityBelow == null) {
                return ActionResult.FAIL;
            }
            BlockPos drainPos = getDrainPos(state, pos);
            BlockState drainState = world.getBlockState(drainPos);

            if (!(drainState.getBlock() instanceof SmelterDrain)) {
                return ActionResult.FAIL;
            }

            SmelterBlockEntity smelter = findSmelter(world, drainPos, state);

            if (smelter == null) {
                return ActionResult.FAIL;
            }

            if (!isMoltenHighEnough(smelter, drainPos)) {
                return ActionResult.FAIL;
            }
            Identifier metalType = smelter.getMetalTypeId();
            if (metalType == null || smelter.getMoltenMetal() <= 0) {
                return ActionResult.FAIL;
            }
            if (SmelterData.getMetalType(metalType) != null && SmelterData.getMetalType(metalType).getMinGradeTemperature() > smelter.getTemperature()) {
                return ActionResult.FAIL;
            }

            boolean started = castingEntityBelow.startFilling(smelter.getPos(), metalType);
            if (!started) {
                return ActionResult.FAIL;
            }
            if (world.getBlockEntity(pos) instanceof SmelterFaucetEntity faucetEntity) {
                faucetEntity.link(smelter.getPos(), metalType);
            }
            world.setBlockState(pos, state.with(OPEN, true));
            return ActionResult.SUCCESS;
        } else {
            if (world.getBlockEntity(pos) instanceof SmelterFaucetEntity faucetEntity) {
                faucetEntity.unlink();
            }
            world.setBlockState(pos, state.with(OPEN, false));

            CastingEntity castingEntityBelow = findCastingEntityBelow(world, pos);
            if (castingEntityBelow != null) {
                castingEntityBelow.stopFilling(world);
            }

            return ActionResult.SUCCESS;
        }
    }

    private CastingEntity findCastingEntityBelow(World world, BlockPos faucetPos) {
        for (int dy = 1; dy <= 2; dy++) {
            BlockPos checkPos = faucetPos.down(dy);
            BlockEntity blockEntity = world.getBlockEntity(checkPos);
            if (blockEntity instanceof CastingEntity castingEntity) {
                return castingEntity;
            }
        }
        return null;
    }

    @Nullable
    private SmelterBlockEntity findSmelter(World world, BlockPos drainPos, BlockState faucetState) {
        Direction inward = faucetState.get(FACING).getOpposite();
        BlockPos startAir = drainPos.offset(inward);

        if (!world.isAir(startAir)) {
            return null;
        }
        int minX = startAir.getX(), maxX = startAir.getX();
        int minY = startAir.getY(), maxY = startAir.getY();
        int minZ = startAir.getZ(), maxZ = startAir.getZ();

        for (Direction dir : Direction.values()) {
            for (int i = 1; i <= ConfigInit.CONFIG.smelterMaxWidth + 1; i++) {
                BlockPos check = startAir.offset(dir, i);
                if (!world.isAir(check)) {
                    if (!world.getBlockState(check).isIn(TagInit.SMELTER_BLOCKS) && !world.getBlockState(check).isOf(BlockInit.SMELTER)) {
                        return null;
                    }
                    break;
                }
                minX = Math.min(minX, check.getX());
                maxX = Math.max(maxX, check.getX());
                minY = Math.min(minY, check.getY());
                maxY = Math.max(maxY, check.getY());
                minZ = Math.min(minZ, check.getZ());
                maxZ = Math.max(maxZ, check.getZ());
            }
        }

        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int y = minY - 1; y <= maxY + 1; y++) {
                for (int z = minZ - 1; z <= maxZ + 1; z++) {
                    if (x == minX - 1 || x == maxX + 1 || y == minY - 1 || y == maxY + 1 || z == minZ - 1 || z == maxZ + 1) {
                        BlockEntity blockEntity = world.getBlockEntity(new BlockPos(x, y, z));
                        if (blockEntity instanceof SmelterBlockEntity smelter) {
                            if (smelter.isFormed()) {
                                return smelter;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isMoltenHighEnough(SmelterBlockEntity smelter, BlockPos drainPos) {
        BlockPos corner = smelter.getCornerMin();
        if (corner == null) {
            return false;
        }
        float fillHeight = smelter.getFillPercent() * smelter.getStructureHeight() * 1f; // 1.0f instead of 0.8 makes sense here


        float drainRelativeY = drainPos.getY() - corner.getY();

        return drainRelativeY <= fillHeight;
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