package net.cupellation.misc;

import net.cupellation.data.SmelterData;
import net.cupellation.init.BlockInit;
import net.cupellation.init.ItemInit;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SlagHelper {

    public static final int MB_PER_LAYER = 162;
    public static final int MAX_LAYERS = 8;
    public static final int SMELT_TIME_PER_LAYER = 100;
    private static final int DEFAULT_TINT = 0xFFFFFF;

    public static int layersFor(int mb) {
        if (mb <= 0) {
            return 0;
        }
        return Math.max(1, Math.round((float) mb / MB_PER_LAYER));
    }

    public static ItemStack createSlagStack(Identifier metalTypeId, int count) {
        ItemStack stack = new ItemStack(BlockInit.SLAG, count);
        stack.set(ItemInit.SLAG_METALS, List.of(metalTypeId));
        return stack;
    }

    public static List<ItemStack> createSlagStacks(List<Identifier> layers) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Map.Entry<Identifier, Integer> entry : countLayers(layers).entrySet()) {
            stacks.add(createSlagStack(entry.getKey(), entry.getValue()));
        }
        return stacks;
    }

    public static Map<Identifier, Integer> countLayers(List<Identifier> layers) {
        Map<Identifier, Integer> counts = new LinkedHashMap<>();
        for (Identifier layer : layers) {
            counts.merge(layer, 1, Integer::sum);
        }
        return counts;
    }

    public static int getTint(List<Identifier> layers) {
        if (layers == null || layers.isEmpty()) {
            return DEFAULT_TINT;
        }
        Identifier top = layers.get(layers.size() - 1);
        if (SmelterData.getMetalType(top) == null) {
            return DEFAULT_TINT;
        }
        return MoltenHelper.lerpColor(SmelterData.getCooledColor(top), DEFAULT_TINT, 0.35f);
    }
}
