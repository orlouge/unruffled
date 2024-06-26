package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.util.Rarity;

import java.util.Collections;
import java.util.Map;

public class CustomItems {
    public static final Item GOLDEN_BERRIES = new Item((new Item.Settings()).rarity(Rarity.RARE).food(
            (new FoodComponent.Builder()).hunger(2).snack().saturationModifier(0.1F).statusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0), 1.0F).alwaysEdible().build()
    ));
    public static final Item IRON_BOLSTER = new BolsterItem(1.5f, -3, ToolMaterials.IRON, new Item.Settings());
    public static final Item DIAMOND_BOLSTER = new BolsterItem(1.5f, -3, ToolMaterials.DIAMOND, new Item.Settings());
    public static final Item NETHERITE_BOLSTER = new BolsterItem(1.5f, -3, ToolMaterials.NETHERITE, new Item.Settings());
    public static final Item CHARGED_TRIDENT = new TridentItem((new Item.Settings()).maxDamage(250));
    public static final Item MAGNETIC_TRIDENT = new TridentItem((new Item.Settings()).maxDamage(250));
    public static final Item PIERCING_ARROW = new ArrowItem(new Item.Settings());
    public static final Item IGNITING_ARROW = new ArrowItem(new Item.Settings());
    public static final Item ANCIENT_CODEX = new AncientCodexItem((new Item.Settings()).maxCount(16).rarity(Rarity.UNCOMMON));
    public static final Item EVIL_TOTEM = new Item((new Item.Settings()).maxCount(1).rarity(Rarity.UNCOMMON));
    public static final Item BLAZING_SWORD = new SwordItem(ToolMaterials.NETHERITE, 2, -2.4F, new Item.Settings());
    public static final Item SACRED_SWORD = new SwordItem(ToolMaterials.IRON, 3, -2.4F, new Item.Settings());
    public static final Map<String, Item> TRIDENTS = Map.of(
            "charged", CustomItems.CHARGED_TRIDENT,
            "magnetic", CustomItems.MAGNETIC_TRIDENT
    );

    public static boolean isDisabled(Enchantment enchantment, ItemStack stack) {
        return UnruffledMod.DISABLED_ENCHANTMENTS.contains(enchantment) && !(UnruffledMod.ITEM_ENCHANTMENTS.getOrDefault(stack.getItem(), Collections.emptyMap()).containsKey(enchantment));
    }
}
