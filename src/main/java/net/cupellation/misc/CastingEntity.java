package net.cupellation.misc;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CastingEntity {

    boolean startFilling(BlockPos smelterPos, Identifier metalType);

    void stopFilling(World world);

    int castingDistance();
}
