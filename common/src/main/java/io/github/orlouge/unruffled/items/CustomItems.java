package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.Map;

public class CustomItems {
    public static final RegistryKey<Item> GOLDEN_BERRIES_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "golden_berries"));
    public static final RegistryKey<Item> IRON_BOLSTER_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "iron_bolster"));
    public static final RegistryKey<Item> DIAMOND_BOLSTER_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "diamond_bolster"));
    public static final RegistryKey<Item> NETHERITE_BOLSTER_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "netherite_bolster"));
    public static final RegistryKey<Item> CHARGED_TRIDENT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "charged_trident"));
    public static final RegistryKey<Item> MAGNETIC_TRIDENT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "magnetic_trident"));
    public static final RegistryKey<Item> PIERCING_ARROW_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "piercing_arrow"));
    public static final RegistryKey<Item> IGNITING_ARROW_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "igniting_arrow"));
    public static final RegistryKey<Item> ANCIENT_CODEX_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "ancient_codex"));
    public static final RegistryKey<Item> EVIL_TOTEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "evil_totem"));
    public static final RegistryKey<Item> BLAZING_SWORD_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "blazing_sword"));
    public static final RegistryKey<Item> SACRED_SWORD_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UnruffledMod.MOD_ID, "sacred_sword"));

    public static final Item GOLDEN_BERRIES = new Item((new Item.Settings()).rarity(Rarity.RARE).food(
            (new FoodComponent.Builder()).nutrition(2).saturationModifier(0.1F).alwaysEdible().build(),
        ConsumableComponents.food().consumeSeconds(0.8F)
            .consumeEffect(new ApplyEffectsConsumeEffect(List.of(
                new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1),
                new StatusEffectInstance(StatusEffects.GLOWING, 600, 0)
            ))).build()
    ).registryKey(GOLDEN_BERRIES_KEY));
    public static final Item IRON_BOLSTER = new BolsterItem(ToolMaterial.IRON, new Item.Settings().registryKey(IRON_BOLSTER_KEY));
    public static final Item DIAMOND_BOLSTER = new BolsterItem(ToolMaterial.DIAMOND, new Item.Settings().registryKey(DIAMOND_BOLSTER_KEY));
    public static final Item NETHERITE_BOLSTER = new BolsterItem(ToolMaterial.NETHERITE, new Item.Settings().fireproof().registryKey(NETHERITE_BOLSTER_KEY));
    public static final Item CHARGED_TRIDENT = new TridentItem((new Item.Settings()).rarity(Rarity.EPIC).maxDamage(250).attributeModifiers(TridentItem.createAttributeModifiers()).component(DataComponentTypes.TOOL, TridentItem.createToolComponent()).registryKey(CHARGED_TRIDENT_KEY));
    public static final Item MAGNETIC_TRIDENT = new TridentItem((new Item.Settings()).rarity(Rarity.EPIC).maxDamage(250).attributeModifiers(TridentItem.createAttributeModifiers()).component(DataComponentTypes.TOOL, TridentItem.createToolComponent()).registryKey(MAGNETIC_TRIDENT_KEY));
    public static final Item PIERCING_ARROW = new ArrowItem(new Item.Settings().registryKey(PIERCING_ARROW_KEY));
    public static final Item IGNITING_ARROW = new ArrowItem(new Item.Settings().registryKey(IGNITING_ARROW_KEY));
    public static final Item ANCIENT_CODEX = new AncientCodexItem((new Item.Settings()).maxCount(16).rarity(Rarity.UNCOMMON).registryKey(ANCIENT_CODEX_KEY));
    public static final Item EVIL_TOTEM = new Item((new Item.Settings()).maxCount(1).rarity(Rarity.UNCOMMON).registryKey(EVIL_TOTEM_KEY));
    public static final Item BLAZING_SWORD = new Item(new Item.Settings().sword(ToolMaterial.NETHERITE, 2, -2.4F).fireproof().registryKey(BLAZING_SWORD_KEY));
    public static final Item SACRED_SWORD = new Item(new Item.Settings().sword(ToolMaterial.IRON, 3, -2.4F).fireproof().registryKey(SACRED_SWORD_KEY));
    public static final Map<String, Item> TRIDENTS = Map.of(
            "charged", CustomItems.CHARGED_TRIDENT,
            "magnetic", CustomItems.MAGNETIC_TRIDENT
    );

}
