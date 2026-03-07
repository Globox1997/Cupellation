package net.cupellation.init;

import com.mojang.serialization.Codec;
import net.cupellation.CupellationMain;
import net.cupellation.item.MoldItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public class ItemInit {

    public static final List<Item> MOLDS = new ArrayList<>();
    public static final List<Item> MOLDABLES = new ArrayList<>();

    public record MoldType(String suffix, int mb, boolean extraOutput, Set<Identifier> blacklist) {
    }

    public static final List<MoldType> MOLD_TYPES = List.of(
            new MoldType("axe_head", 432, true, Set.of()),
            new MoldType("hoe_head", 288, true, Set.of()),
            new MoldType("pickaxe_head", 432, true, Set.of()),
            new MoldType("shovel_head", 144, true, Set.of()),
            new MoldType("sword_blade", 288, true, Set.of()),
            new MoldType("helmet", 720, false, Set.of()),
            new MoldType("chestplate", 1152, false, Set.of()),
            new MoldType("leggings", 1008, false, Set.of()),
            new MoldType("boots", 576, false, Set.of())
    );

    // Item Group
    public static final RegistryKey<ItemGroup> CUPELLATION_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, CupellationMain.identifierOf("item_group"));

    // Item Component
    public static final ComponentType<Integer> QUALITY_GRADE = registerComponent("quality_grade", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER));

    // Items
    public static final Item CALCITE_POWDER = register("calcite_powder", new Item(new Item.Settings()));
    public static final Item QUARTZ_POWDER = register("quartz_powder", new Item(new Item.Settings()));
    public static final Item INGOT_MOLD = register("ingot_mold", new MoldItem(CupellationMain.identifierOf("gold"), 144, "ingot", Set.of(), new Item.Settings()));

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
                FabricItemGroup.builder().icon(() -> new ItemStack(BlockInit.DEEPSLATE_BRICK_SMELTER)).displayName(Text.translatable("item.cupellation.item_group")).build());

        for (MoldType moldType : ItemInit.MOLD_TYPES) {
            Item item = register(moldType.suffix() + "_mold", new MoldItem(CupellationMain.identifierOf("gold"), moldType.mb(), moldType.suffix(), moldType.blacklist(), new Item.Settings()));
            MOLDS.add(item);
        }
        for (ToolMaterials toolMaterial : ToolMaterials.values()) {
            if (toolMaterial == ToolMaterials.WOOD) {
                continue;
            }
            for (MoldType moldType : ItemInit.MOLD_TYPES) {
                if (!moldType.extraOutput()) {
                    continue;
                }
                String material = toolMaterial.toString().toLowerCase();
                if (material.equals("gold")) {
                    material = "golden";
                }
                Item.Settings settings = new Item.Settings();
                if (material.equals("netherite")) {
                    settings = settings.fireproof();
                }
                Item item = register(material + "_" + moldType.suffix(), new Item(settings));
                MOLDABLES.add(item);
            }
        }
    }
}
