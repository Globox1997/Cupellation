package net.cupellation.api;

import net.cupellation.misc.CastingEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/*
 * Called on the server when a player takes a finished cast out of a casting table or casting basin.
 * Items which were put onto the table by a player don't trigger it.
 * */
@FunctionalInterface
public interface CastingResultCallback {

    Event<CastingResultCallback> EVENT = EventFactory.createArrayBacked(CastingResultCallback.class, listeners -> (player, stack, metalTypeId, castingEntity) -> {
        for (CastingResultCallback listener : listeners) {
            listener.onCastTaken(player, stack, metalTypeId, castingEntity);
        }
    });

    /**
     * @param stack         a copy of the taken cast, e.g. an ingot, a tool head, a mold or a metal block
     * @param metalTypeId   the metal type the cast was made of
     * @param castingEntity the casting table or casting basin entity
     */
    void onCastTaken(ServerPlayerEntity player, ItemStack stack, Identifier metalTypeId, CastingEntity castingEntity);
}
