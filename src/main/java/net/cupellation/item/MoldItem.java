package net.cupellation.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class MoldItem extends Item {

    private final Identifier metalTypeId;

    public MoldItem(Identifier metalTypeId, Settings settings) {
        super(settings);
        this.metalTypeId = metalTypeId;
    }

    public Identifier getMetalTypeId() {
        return metalTypeId;
    }
}
