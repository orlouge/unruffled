package io.github.orlouge.unruffled.potions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public record BrewingPotionRecipe(RegistryEntry<Potion> input, Item ingredient, RegistryEntry<Potion> output) {
    public static final Codec<BrewingPotionRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Registries.POTION.getEntryCodec().fieldOf("base").forGetter(BrewingPotionRecipe::input),
        Registries.ITEM.getCodec().fieldOf("ingredient").forGetter(BrewingPotionRecipe::ingredient),
        Registries.POTION.getEntryCodec().fieldOf("result").forGetter(BrewingPotionRecipe::output)
    ).apply(instance, BrewingPotionRecipe::new));
}
