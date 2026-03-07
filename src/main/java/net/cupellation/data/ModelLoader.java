package net.cupellation.data;

import net.cupellation.init.ItemInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.Item;

public class ModelLoader extends FabricModelProvider {

    public ModelLoader(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        for (Item mold : ItemInit.MOLDS) {
            itemModelGenerator.register(mold, Models.GENERATED);
        }
        for (Item moldable : ItemInit.MOLDABLES) {
            itemModelGenerator.register(moldable, Models.GENERATED);
        }
    }

}
