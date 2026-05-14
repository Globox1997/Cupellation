package net.cupellation.api;

public interface CupellationEntrypoint {

    default void registerSmelterTypes() {}

    default void registerMoldTypes() {}
}