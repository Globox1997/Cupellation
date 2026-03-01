package net.cupellation.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class MoldItem extends Item {

    private final Identifier metalTypeId;
    private final int mb;

    public MoldItem(Identifier metalTypeId, int mb, Settings settings) {
        super(settings);
        this.metalTypeId = metalTypeId;
        this.mb = mb;
    }

    public Identifier getMetalTypeId() {
        return metalTypeId;
    }

    public int getMb() {
        return mb;
    }
}
