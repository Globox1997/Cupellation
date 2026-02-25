package net.cupellation.init;

import com.mojang.serialization.Codec;
import net.cupellation.CupellationMain;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class ItemInit {

    // Item Group
    public static final RegistryKey<ItemGroup> CUPELLATION_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, CupellationMain.identifierOf("item_group"));

    // Item Component
    public static final ComponentType<Integer> QUALITY_GRADE = registerComponent("quality_grade", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER));

    // Items
    public static final Item CALCITE_POWDER = register("calcite_powder", new Item(new Item.Settings()));

    private static <T> ComponentType<T> registerComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, builderOperator.apply(ComponentType.builder()).build());
    }

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
