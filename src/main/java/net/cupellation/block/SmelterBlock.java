package net.cupellation.block;

import com.mojang.serialization.MapCodec;
import net.cupellation.block.entity.SmelterBlockEntity;
import net.cupellation.init.BlockInit;
import net.cupellation.init.SoundInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
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

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SmelterBlockEntity smelterBlockEntity && smelterBlockEntity.isFormed() && smelterBlockEntity.getTotalMoltenMetal() > 0) {

            BlockPos corner = smelterBlockEntity.getCornerMin();
            if (corner == null) {
                return;
            }
            int innerW = smelterBlockEntity.getStructureWidth() - 2;
            int innerD = smelterBlockEntity.getStructureDepth() - 2;
            int innerH = smelterBlockEntity.getStructureHeight();

            float fillHeight = smelterBlockEntity.getFillPercent() * innerH;

            double px = corner.getX() + 1 + random.nextDouble() * innerW;
            double pz = corner.getZ() + 1 + random.nextDouble() * innerD;
            double py = corner.getY() + fillHeight;

            BlockPos surfacePos = BlockPos.ofFloored(px, py, pz);
            BlockPos aboveSurface = surfacePos.up();
            if (!world.getBlockState(aboveSurface).isAir()) {
                return;
            }

            world.addParticle(ParticleTypes.LAVA, px, py, pz, 0.0, 0.0, 0.0);

            if (random.nextInt(50) == 0) {
                world.playSound(px, py, pz, SoundInit.MOLTEN_EVENT, SoundCategory.BLOCKS,
                        0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient()) {
            boolean powered = world.isReceivingRedstonePower(pos) || isNeighborPowered(world, pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SmelterBlockEntity smelterBlockEntity) {
                smelterBlockEntity.setRedstonePowered(powered);
            }
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SmelterBlockEntity smelterBlockEntity) {
                smelterBlockEntity.onStructureDestroyed();
            }
        }
    }

    private boolean isNeighborPowered(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.offset(direction);
            if (world.isReceivingRedstonePower(neighborPos)) {
                return true;
            }
        }
        return false;
    }

}
