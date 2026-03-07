package net.cupellation;

import net.cupellation.data.LangLoader;
import net.cupellation.data.ModelLoader;
import net.cupellation.data.RecipeLoader;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CupellationDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModelLoader::new);
        pack.addProvider(RecipeLoader::new);
        pack.addProvider(LangLoader::new);
    }

}
