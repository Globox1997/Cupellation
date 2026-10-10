package net.cupellation.misc;

import net.cupellation.init.SoundInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

import java.util.Map;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public class CastingSoundHelper {

    private static final Map<BlockEntity, SoundInstance> SOUNDS = new WeakHashMap<>();

    public static void tick(BlockEntity blockEntity, boolean filling) {
        if (!filling) {
            stop(blockEntity);
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        SoundInstance sound = SOUNDS.get(blockEntity);
        if (sound == null || !client.getSoundManager().isPlaying(sound)) {
            sound = new PositionedSoundInstance(SoundInit.CASTING_EVENT, SoundCategory.BLOCKS, 1.0f, 0.9F + blockEntity.getWorld().getRandom().nextFloat() * 0.15F,
                    blockEntity.getWorld().getRandom(), blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ());
            client.getSoundManager().play(sound);
            SOUNDS.put(blockEntity, sound);
        }
    }

    public static void stop(BlockEntity blockEntity) {
        SoundInstance sound = SOUNDS.remove(blockEntity);
        if (sound != null) {
            MinecraftClient.getInstance().getSoundManager().stop(sound);
        }
    }
}
