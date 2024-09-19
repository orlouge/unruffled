package io.github.orlouge.unruffled.fabric;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.fabric.mixin.BrewingRecipeRegistryAccessor;
import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import io.github.orlouge.unruffled.potions.BrewingPotionRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

import java.util.List;

public class UnruffledFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registries.FEATURE, new Identifier(UnruffledMod.MOD_ID, "underground_pond"), UnruffledMod.UNDERGROUND_POND_FEATURE);
        Registry.register(Registries.FEATURE, new Identifier(UnruffledMod.MOD_ID, "underground_cabin"), UnruffledMod.UNDERGROUND_CABIN_FEATURE);

        Registry.register(Registries.STATUS_EFFECT, new Identifier(UnruffledMod.MOD_ID, "teleportation"), UnruffledMod.TELEPORTATION_EFFECT);
        Registry.register(Registries.POTION, new Identifier(UnruffledMod.MOD_ID, "teleportation"), UnruffledMod.TELEPORTATION_POTION);

        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "golden_berries"), CustomItems.GOLDEN_BERRIES);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "iron_bolster"), CustomItems.IRON_BOLSTER);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "diamond_bolster"), CustomItems.DIAMOND_BOLSTER);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "netherite_bolster"), CustomItems.NETHERITE_BOLSTER);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "charged_trident"), CustomItems.CHARGED_TRIDENT);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "magnetic_trident"), CustomItems.MAGNETIC_TRIDENT);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "piercing_arrow"), CustomItems.PIERCING_ARROW);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "igniting_arrow"), CustomItems.IGNITING_ARROW);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "ancient_codex"), CustomItems.ANCIENT_CODEX);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "evil_totem"), CustomItems.EVIL_TOTEM);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "blazing_sword"), CustomItems.BLAZING_SWORD);
        Registry.register(Registries.ITEM, new Identifier(UnruffledMod.MOD_ID, "sacred_sword"), CustomItems.SACRED_SWORD);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.IRON_BOLSTER));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.DIAMOND_BOLSTER));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.NETHERITE_BOLSTER));
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.CHARGED_TRIDENT));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.MAGNETIC_TRIDENT));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.PIERCING_ARROW));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.IGNITING_ARROW));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.BLAZING_SWORD));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.SACRED_SWORD));
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(CustomItems.GOLDEN_BERRIES);
        });

        LootTableEvents.REPLACE.register((resourceManager, lootManager, id, original, source) -> {
            if (source.isBuiltin()) {
                if (id.equals(LootTables.PIGLIN_BARTERING_GAMEPLAY)) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "gameplay/piglin_bartering"));
                } else if (id.equals(LootTables.FISHING_TREASURE_GAMEPLAY)) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "gameplay/fishing/treasure"));
                } else if (id.equals(LootTables.END_CITY_TREASURE_CHEST)) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "chests/end_city_treasure"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "entities/iron_golem"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "entities/iron_golem"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "entities/spider"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "entities/spider"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "entities/evoker")) && Config.INSTANCE.get().mechanicsConfig.disableTotemOfUndying()) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "entities/evoker"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "entities/wither_skeleton"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "entities/wither_skeleton"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "entities/skeleton"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "entities/skeleton"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "entities/zombie"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "entities/zombie"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "blocks/nether_quartz_ore"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "blocks/nether_quartz_ore"));
                } else if (id.equals(new Identifier(Identifier.DEFAULT_NAMESPACE, "blocks/ender_chest"))) {
                    return lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "blocks/ender_chest"));
                }
            }
            return null;
        });

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin()) {
                if (Config.INSTANCE.get().lootConfig.lootCodicesAdd().containsKey(id)) {
                    List<LootPoolEntry> entries = Config.INSTANCE.get().lootConfig.lootCodicesAdd().get(id).stream().map(
                            number -> ItemEntry.builder(CustomItems.ANCIENT_CODEX).apply(
                                    SetNbtLootFunction.builder(AncientCodexItem.getNumberNbt(number))
                            ).build()
                    ).toList();
                    LootPool.Builder poolBuilder = LootPool.builder().with(entries).rolls(UniformLootNumberProvider.create(0, 1));
                    tableBuilder.pool(poolBuilder);
                } else if (Config.INSTANCE.get().lootConfig.lootCodicesModify().containsKey(id)) {
                    List<LootPoolEntry> entries = Config.INSTANCE.get().lootConfig.lootCodicesModify().get(id).stream().map(
                            number -> ItemEntry.builder(CustomItems.ANCIENT_CODEX).apply(
                                    SetNbtLootFunction.builder(AncientCodexItem.getNumberNbt(number))
                            ).build()
                    ).toList();
                    tableBuilder.modifyPools(pool -> pool.with(entries));
                } else if (id.equals(LootTables.RUINED_PORTAL_CHEST)) {
                    tableBuilder.pools(List.of(lootManager.getLootTable(new Identifier(UnruffledMod.MOD_ID, "chests/ruined_portal_extra")).pools));
                }
            }
        });

        if (Config.INSTANCE.get().mechanicsConfig.sleepTime() >= 0) {
            EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, pos, isNight) -> !isNight || (player.getWorld().getLunarTime() % 24000 < Config.INSTANCE.get().mechanicsConfig.sleepTime() /*player.getWorld().getAmbientDarkness() < 11*/ && !player.getWorld().isThundering()) ? ActionResult.FAIL : ActionResult.SUCCESS);
        }

        for (BrewingPotionRecipe brewingPotionRecipe : UnruffledMod.POTION_RECIPES) {
            BrewingRecipeRegistryAccessor.registerPotionRecipe(brewingPotionRecipe.input(), brewingPotionRecipe.ingredient(), brewingPotionRecipe.output());
        }

        UnruffledMod.init();
        BiomeModifications.addSpawn(
                ctx -> ctx.getBiomeKey().getValue().equals(new Identifier("minecraft", "nether_wastes")),
                SpawnGroup.MONSTER, EntityType.BLAZE, 20, 1, 1
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(UnruffledMod.MOD_ID, "ore_emerald_lower"))
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(), GenerationStep.Feature.LAKES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(UnruffledMod.MOD_ID, "underground_pond"))
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(), GenerationStep.Feature.LAKES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(UnruffledMod.MOD_ID, "underground_cabin"))
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OCEAN), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(UnruffledMod.MOD_ID, "ore_prismarine"))
        );

        FuelRegistry.INSTANCE.add(Items.LAVA_BUCKET, 200);
    }
}
