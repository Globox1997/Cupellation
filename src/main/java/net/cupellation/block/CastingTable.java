package net.cupellation.block;

import com.mojang.serialization.MapCodec;
import net.cupellation.block.entity.CastingTableEntity;
import net.cupellation.init.BlockInit;
import net.cupellation.item.MoldItem;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
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

        if (!table.isFilling() && table.getMoltenAmount() <= 0) {
            if (!heldItem.isEmpty()) {
                if (heldItem.isIn(ConventionalItemTags.INGOTS) && table.tryInsertResult(heldItem)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                } else if (heldItem.getItem() instanceof MoldItem && table.tryInsertMold(heldItem)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }

                ItemStack ingot = table.tryExtractResult();
                if (!ingot.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1.2f);
                    if (!player.getInventory().insertStack(ingot)) {
                        player.dropItem(ingot, false);
                    }
                    return ActionResult.SUCCESS;
                }
            } else {
                ItemStack ingot = table.tryExtractResult();
                if (!ingot.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1.2f);
                    if (!player.getInventory().insertStack(ingot)) {
                        player.dropItem(ingot, false);
                    }
                    return ActionResult.SUCCESS;
                }

                ItemStack mold = table.tryExtractMold();
                if (!mold.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1.2f);
                    if (!player.getInventory().insertStack(mold)) {
                        player.dropItem(mold, false);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}
