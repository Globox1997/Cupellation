package net.cupellation.data;

import net.cupellation.init.ItemInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryWrapper;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class LangLoader extends FabricLanguageProvider {

    public LangLoader(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        for (Item mold : ItemInit.MOLDS) {
            translationBuilder.add(mold, getTranslation(mold));
        }
        for (Item moldable : ItemInit.MOLDABLES) {
            translationBuilder.add(moldable, getTranslation(moldable));
        }
        try {
            Path existingFilePath = dataOutput.getModContainer().findPath("assets/cupellation/lang/en_us.existing.json").get();
            translationBuilder.add(existingFilePath);

        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }

    private String getTranslation(Item item) {
        String[] translationParts = item.getTranslationKey().split("\\.");
        String[] words = translationParts[translationParts.length - 1].split("_");

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }
}
