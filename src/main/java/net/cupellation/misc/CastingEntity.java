package net.cupellation.misc;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CastingEntity {

    boolean startFilling(BlockPos smelterPos, Identifier metalType);

    void stopFilling(World world);

    // pixel distance from faucet stream start (including the 2 stream pixels)
    // this means from faucet bottom calculate 4 pixels to where the stream should end
    // example: casting table = 10
    int castingDistance();
}
