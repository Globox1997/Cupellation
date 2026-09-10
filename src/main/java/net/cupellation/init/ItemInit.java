package net.cupellation.init;

import com.mojang.serialization.Codec;
import net.cupellation.CupellationMain;
import net.cupellation.api.CupellationAPI;
import net.cupellation.api.CupellationEntrypoint;
import net.cupellation.api.MoldType;
import net.cupellation.item.BrickMoldItem;
import net.cupellation.item.ClayMoldItem;
import net.cupellation.item.MoldItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
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
import java.util.function.UnaryOperator;

public class ItemInit {

    public static final List<Item> CLAY_MOLDS = new ArrayList<>();
    public static final List<Item> BRICK_MOLDS = new ArrayList<>();
    public static final List<Item> MOLDS = new ArrayList<>();
    public static final List<Item> MOLDABLES = new ArrayList<>();

    // Item Group
    public static final RegistryKey<ItemGroup> CUPELLATION_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, CupellationMain.identifierOf("item_group"));

    // Item Component
    public static final ComponentType<Integer> QUALITY_GRADE = registerComponent("quality_grade", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER));

    // Items
    public static final Item CALCITE_POWDER = register("calcite_powder", new Item(new Item.Settings()));
    public static final Item QUARTZ_POWDER = register("quartz_powder", new Item(new Item.Settings()));
    public static final Item CLAY_MOLD = register("clay_mold", new ClayMoldItem(new Item.Settings()));

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

        CupellationAPI.registerDefaultMoldTypes();

        FabricLoader.getInstance().getEntrypoints("cupellation", CupellationEntrypoint.class).forEach(CupellationEntrypoint::registerMoldTypes);

        Identifier defaultMoldingMetal = CupellationMain.identifierOf("gold");

        for (MoldType moldType : CupellationAPI.getMoldTypes()) {
            Identifier moldingMetal = moldType.moldingMetalTypeId() != null ? moldType.moldingMetalTypeId() : defaultMoldingMetal;

            MoldItem item = (MoldItem) register(moldType.suffix() + "_mold",
                    new MoldItem(moldingMetal, moldType.mb(), moldType.suffix(), moldType.blacklist(), moldType.strategy(), new Item.Settings()));
            MOLDS.add(item);
            CupellationAPI.registerMoldItem(moldType, item);

            Item clayMold = register("clay_" + moldType.suffix() + "_mold", new Item(new Item.Settings()));
            CLAY_MOLDS.add(clayMold);

            BrickMoldItem brickMold = (BrickMoldItem) register("brick_" + moldType.suffix() + "_mold",
                    new BrickMoldItem(null, moldType.mb(), moldType.suffix(), moldType.blacklist(), moldType.strategy(), new Item.Settings()));
            BRICK_MOLDS.add(brickMold);
            MOLDS.add(brickMold);
        }
        for (ToolMaterials toolMaterial : ToolMaterials.values()) {
            if (toolMaterial == ToolMaterials.WOOD) {
                continue;
            }
            for (MoldType moldType : CupellationAPI.getMoldTypes()) {
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
