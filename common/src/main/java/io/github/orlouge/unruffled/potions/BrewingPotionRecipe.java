package io.github.orlouge.unruffled.potions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;

public record BrewingPotionRecipe(Potion input, Item ingredient, Potion output) {
    public static final Codec<BrewingPotionRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Registries.POTION.getCodec().fieldOf("base").forGetter(BrewingPotionRecipe::input),
        Registries.ITEM.getCodec().fieldOf("ingredient").forGetter(BrewingPotionRecipe::ingredient),
        Registries.POTION.getCodec().fieldOf("result").forGetter(BrewingPotionRecipe::output)
    ).apply(instance, BrewingPotionRecipe::new));
}
