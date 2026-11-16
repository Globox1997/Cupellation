package net.cupellation.block;

import com.mojang.serialization.MapCodec;
import net.cupellation.api.CastingResultCallback;
import net.cupellation.block.entity.CastingTableEntity;
import net.cupellation.init.BlockInit;
import net.cupellation.item.ClayMoldItem;
import net.cupellation.item.MoldItem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CastingTable extends BlockWithEntity {

    private static final VoxelShape SHAPE =
            VoxelShapes.union(createCuboidShape(0, 11, 0, 2, 16, 16),
                    createCuboidShape(2, 11, 2, 14, 15, 14),
                    createCuboidShape(14, 11, 0, 16, 16, 16),
                    createCuboidShape(2, 11, 0, 14, 16, 2),
                    createCuboidShape(2, 11, 14, 14, 16, 16),
                    createCuboidShape(0, 0, 0, 4, 11, 4),
                    createCuboidShape(12, 0, 0, 16, 11, 4),
                    createCuboidShape(0, 0, 12, 4, 11, 16),
                    createCuboidShape(12, 0, 12, 16, 11, 16)
            );


    public CastingTable(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CastingTable.createCodec(CastingTable::new);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CastingTableEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, BlockInit.CASTING_TABLE_ENTITY, world.isClient() ? CastingTableEntity::clientTick : CastingTableEntity::serverTick);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof CastingTableEntity table)) {
            return ActionResult.PASS;
        }

        ItemStack heldItem = player.getMainHandStack();

        if (player.isSneaking() && heldItem.isEmpty() && table.tryDiscardMolten()) {
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 1f);
            return ActionResult.SUCCESS;
        }

        if (!table.isFilling() && table.getMoltenAmount() <= 0) {
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof ClayMoldItem && table.tryInsertClayMold(heldItem)) {
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.SUCCESS;
            }

            if (!heldItem.isEmpty() && table.hasClayMold()) {
                ItemStack imprinted = table.tryImprintClay(heldItem);
                if (!imprinted.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1f, 1.2f);
                }
                return ActionResult.SUCCESS;
            }

            if (table.hasClayMold() && heldItem.isEmpty()) {
                ItemStack clay = table.tryExtractClayMold();
                if (!clay.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1.2f);
                    if (!player.getInventory().insertStack(clay)) {
                        player.dropItem(clay, false);
                    }
                    return ActionResult.SUCCESS;
                }
            }

            if (!heldItem.isEmpty()) {
                if (heldItem.getItem() instanceof MoldItem && table.tryInsertMold(heldItem)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
                if (table.tryInsertResult(heldItem)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
                Identifier castMetalTypeId = table.getResultMetalTypeId();
                if (giveExtractedStack(world, pos, player, table.tryExtractResult(), castMetalTypeId, table)) {
                    return ActionResult.SUCCESS;
                }
            } else {
                Identifier castMetalTypeId = table.getResultMetalTypeId();
                if (giveExtractedStack(world, pos, player, table.tryExtractResult(), castMetalTypeId, table)) {
                    return ActionResult.SUCCESS;
                }
                castMetalTypeId = table.getMoldMetalTypeId();
                if (giveExtractedStack(world, pos, player, table.tryExtractMold(), castMetalTypeId, table)) {
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient() && world.getBlockEntity(pos) instanceof CastingTableEntity table) {
            ItemScatterer.spawn(world, pos, table.getDroppedStacks());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    // Gives the extracted stack to the player, castMetalTypeId is set if the stack got cast on the table
    private static boolean giveExtractedStack(World world, BlockPos pos, PlayerEntity player, ItemStack stack, @Nullable Identifier castMetalTypeId, CastingTableEntity table) {
        if (stack.isEmpty()) {
            return false;
        }
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1.2f);
        if (castMetalTypeId != null && player instanceof ServerPlayerEntity serverPlayerEntity) {
            CastingResultCallback.EVENT.invoker().onCastTaken(serverPlayerEntity, stack.copy(), castMetalTypeId, table);
        }
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (world.getBlockEntity(pos) instanceof CastingTableEntity castingTableEntity && castingTableEntity.hasResult()) {
            return VoxelShapes.fullCube();
        }
        return SHAPE;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CastingTableEntity castingTableEntity && castingTableEntity.hasResult()) {
            return VoxelShapes.fullCube();
        }
        return SHAPE;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}
