package io.github.orlouge.unruffled.potions;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

public record BrewingPotionRecipe(Potion input, Item ingredient, Potion output) {
}
