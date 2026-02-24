package net.cupellation.init;

import net.cupellation.CupellationMain;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemInit {

    // Item Group
    public static final RegistryKey<ItemGroup> CUPELLATION_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, CupellationMain.identifierOf("item_group"));

    private static Item register(String id, Item item) {
        return register(CupellationMain.identifierOf(id), item);
    }

    private static Item register(Identifier id, Item item) {
        ItemGroupEvents.modifyEntriesEvent(CUPELLATION_ITEM_GROUP).register(entries -> entries.add(item));
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, CUPELLATION_ITEM_GROUP,
                FabricItemGroup.builder().icon(() -> new ItemStack(Items.STICK)).displayName(Text.translatable("item.cupellation.item_group")).build());
    }
}
