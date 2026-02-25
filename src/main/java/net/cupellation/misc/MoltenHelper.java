package net.cupellation.misc;

import net.cupellation.CupellationMain;
import net.cupellation.data.SmelterData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

// TODO: TO BE REMOVED!
public class MoltenHelper {

//    private static final Identifier MOLTEN_IRON_SPRITE = CupellationMain.identifierOf("fluid/molten_iron");
//    private static final Identifier MOLTEN_GOLD_SPRITE = CupellationMain.identifierOf("fluid/molten_gold");
//    private static final Identifier MOLTEN_COPPER_SPRITE = CupellationMain.identifierOf("fluid/molten_copper");
//    private static final Identifier MOLTEN_DEFAULT_SPRITE = CupellationMain.identifierOf("fluid/molten");

//    public static Identifier getMoltenSpriteId(int metalType) {
//        return switch (metalType) {
////            case 1 -> MOLTEN_IRON_SPRITE;
////            case 2 -> MOLTEN_GOLD_SPRITE;
////            case 3 -> MOLTEN_COPPER_SPRITE;
//            default -> MOLTEN_DEFAULT_SPRITE;
//        };
//    }
//
//    public static int getMoltenColor(int metalType) {
//        return switch (metalType) {
////            case 1 -> 0xC8C8C8; // Eisen – silbrig
//            case 1 -> 0xFF4500; // Eisen – silbrig
//            case 2 -> 0xFFD700; // Gold – golden
//            default -> 0xFF4500; // Fallback – orange-rot
//        };
//    }
//
//    public static String getMetalName(int metalType) {
//        return switch (metalType) {
//            case 1 -> "Molten Iron";
//            case 2 -> "Molten Gold";
//            case 3 -> "Molten Copper";
//            default -> "Empty";
//        };
//    }
//
//    public static boolean isSmeltable(ItemStack stack) {
//        return stack.isIn(ItemTags.IRON_ORES)
//                || stack.isIn(ItemTags.GOLD_ORES)
//                || stack.isIn(ItemTags.COPPER_ORES)
//                || stack.isOf(Items.RAW_IRON)
//                || stack.isOf(Items.RAW_GOLD)
//                || stack.isOf(Items.RAW_COPPER)
//                || stack.isOf(Items.IRON_INGOT)
//                || stack.isOf(Items.GOLD_INGOT)
//                || stack.isOf(Items.COPPER_INGOT);
//    }
////
//    public static int getFuelMaxTemp(ItemStack fuel) {
//        if (fuel.isOf(Items.BLAZE_ROD) || fuel.isOf(Items.BLAZE_POWDER)) return 1400;
//        if (fuel.isOf(Items.COAL) || fuel.isOf(Items.COAL_BLOCK)) return 1000;
//        if (fuel.isOf(Items.CHARCOAL)) return 900;
//        if (fuel.isOf(Items.LAVA_BUCKET)) return 1300;
//        return 600;
//    }

//
//    public static int getSmeltTime(ItemStack stack) {
//        return SmelterData.getItemData(stack.getItem()).smeltTime();

//        if (stack.isOf(Items.IRON_ORE) || stack.isOf(Items.DEEPSLATE_IRON_ORE)) return 200;
//        if (stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.DEEPSLATE_GOLD_ORE)) return 200;
//        if (stack.isOf(Items.COPPER_ORE) || stack.isOf(Items.DEEPSLATE_COPPER_ORE)) return 200;
//        if (stack.isOf(Items.RAW_IRON)) return 160;
//        if (stack.isOf(Items.RAW_GOLD)) return 160;
//        if (stack.isOf(Items.RAW_COPPER)) return 160;
//        if (stack.isOf(Items.IRON_INGOT)) return 120;
//        if (stack.isOf(Items.GOLD_INGOT)) return 120;
//        if (stack.isOf(Items.COPPER_INGOT)) return 120;
//        return 150;
//    }
//
//    public static int getMetalYield(ItemStack stack) {
//        return SmelterData.getItemData(stack.getItem()).yield();

//        if (stack.isOf(Items.IRON_ORE) || stack.isOf(Items.DEEPSLATE_IRON_ORE)) return 144;
//        if (stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.DEEPSLATE_GOLD_ORE)) return 144;
//        if (stack.isOf(Items.COPPER_ORE) || stack.isOf(Items.DEEPSLATE_COPPER_ORE)) return 144;
//        if (stack.isOf(Items.RAW_IRON)) return 144;
//        if (stack.isOf(Items.RAW_GOLD)) return 144;
//        if (stack.isOf(Items.RAW_COPPER)) return 144;
//        if (stack.isOf(Items.IRON_INGOT)) return 144;
//        if (stack.isOf(Items.GOLD_INGOT)) return 144;
//        if (stack.isOf(Items.COPPER_INGOT)) return 144;
//        return 0;
//    }

//    public static int getMetalType(ItemStack stack) {
////        return SmelterData.getItemData(stack.getItem()).
//        if (stack.isOf(Items.IRON_ORE) || stack.isOf(Items.DEEPSLATE_IRON_ORE)|| stack.isOf(Items.RAW_IRON) || stack.isOf(Items.IRON_INGOT)) return 1;
//        if (stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.DEEPSLATE_GOLD_ORE)|| stack.isOf(Items.RAW_GOLD) || stack.isOf(Items.GOLD_INGOT)) return 2;
//        if (stack.isOf(Items.COPPER_ORE) || stack.isOf(Items.DEEPSLATE_COPPER_ORE)|| stack.isOf(Items.RAW_COPPER) || stack.isOf(Items.COPPER_INGOT)) return 3;
//        return 0;
//    }
//
//    public static int getRequiredTemp(int type) {
//        return switch (type) {
//            case 1 -> 900;  // Iron:   needs coal+ fuel
//            case 2 -> 700;  // Gold:   charcoal is enough
//            case 3 -> 800;  // Copper: charcoal is enough
//            default -> 9999;
//        };
//    }

}
