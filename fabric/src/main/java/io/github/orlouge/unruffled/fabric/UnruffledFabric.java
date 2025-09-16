package io.github.orlouge.unruffled.fabric;

import io.github.orlouge.unruffled.Packets;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import io.github.orlouge.unruffled.potions.BrewingPotionRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

import java.util.List;
import java.util.Optional;

public class UnruffledFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(Packets.AttackMiss.PACKET_ID, Packets.AttackMiss.CODEC);
        PayloadTypeRegistry.playC2S().register(Packets.LockCompass.PACKET_ID, Packets.LockCompass.CODEC);
        PayloadTypeRegistry.playS2C().register(Packets.LockedDeathPositionUpdate.PACKET_ID, Packets.LockedDeathPositionUpdate.CODEC);
        PayloadTypeRegistry.playS2C().register(Packets.ExtendedHungerUpdate.PACKET_ID, Packets.ExtendedHungerUpdate.CODEC);

        Registry.register(Registries.FEATURE, Identifier.of(UnruffledMod.MOD_ID, "underground_pond"), UnruffledMod.UNDERGROUND_POND_FEATURE);
        Registry.register(Registries.FEATURE, Identifier.of(UnruffledMod.MOD_ID, "underground_cabin"), UnruffledMod.UNDERGROUND_CABIN_FEATURE);

        //Registry.register(Registries.STATUS_EFFECT, Identifier.of(UnruffledMod.MOD_ID, "teleportation"), UnruffledMod.TELEPORTATION_EFFECT);
        //Registry.register(Registries.POTION, Identifier.of(UnruffledMod.MOD_ID, "teleportation"), UnruffledMod.TELEPORTATION_POTION);

        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(UnruffledMod.MOD_ID, "codex_number"), AncientCodexItem.NUMBER);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(UnruffledMod.MOD_ID, "locked_compass"), UnruffledMod.LOCKED_COMPASS_COMPONENT);

        Registry.register(Registries.ITEM, CustomItems.GOLDEN_BERRIES_KEY, CustomItems.GOLDEN_BERRIES);
        Registry.register(Registries.ITEM, CustomItems.IRON_BOLSTER_KEY, CustomItems.IRON_BOLSTER);
        Registry.register(Registries.ITEM, CustomItems.DIAMOND_BOLSTER_KEY, CustomItems.DIAMOND_BOLSTER);
        Registry.register(Registries.ITEM, CustomItems.NETHERITE_BOLSTER_KEY, CustomItems.NETHERITE_BOLSTER);
        Registry.register(Registries.ITEM, CustomItems.CHARGED_TRIDENT_KEY, CustomItems.CHARGED_TRIDENT);
        Registry.register(Registries.ITEM, CustomItems.MAGNETIC_TRIDENT_KEY, CustomItems.MAGNETIC_TRIDENT);
        Registry.register(Registries.ITEM, CustomItems.PIERCING_ARROW_KEY, CustomItems.PIERCING_ARROW);
        Registry.register(Registries.ITEM, CustomItems.IGNITING_ARROW_KEY, CustomItems.IGNITING_ARROW);
        Registry.register(Registries.ITEM, CustomItems.ANCIENT_CODEX_KEY, CustomItems.ANCIENT_CODEX);
        Registry.register(Registries.ITEM, CustomItems.EVIL_TOTEM_KEY, CustomItems.EVIL_TOTEM);
        Registry.register(Registries.ITEM, CustomItems.BLAZING_SWORD_KEY, CustomItems.BLAZING_SWORD);
        Registry.register(Registries.ITEM, CustomItems.SACRED_SWORD_KEY, CustomItems.SACRED_SWORD);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            RegistryEntryLookup.RegistryLookup lookup = content.getContext().lookup();
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.IRON_BOLSTER, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.DIAMOND_BOLSTER, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.NETHERITE_BOLSTER, lookup));
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            RegistryEntryLookup.RegistryLookup lookup = content.getContext().lookup();
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.CHARGED_TRIDENT, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.MAGNETIC_TRIDENT, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.PIERCING_ARROW, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.IGNITING_ARROW, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.BLAZING_SWORD, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.SACRED_SWORD, lookup));
            content.add(ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.EVIL_TOTEM, lookup));
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(CustomItems.GOLDEN_BERRIES);
        });

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin()) {
                if (Config.INSTANCE.get().lootConfig.lootCodicesAdd().containsKey(key)) {
                    List<LootPoolEntry> entries = Config.INSTANCE.get().lootConfig.lootCodicesAdd().get(key).stream().map(
                            number -> ItemEntry.builder(CustomItems.ANCIENT_CODEX).apply(
                                    SetComponentsLootFunction.builder(AncientCodexItem.NUMBER, number)
                            ).build()
                    ).toList();
                    LootPool.Builder poolBuilder = LootPool.builder().with(entries).rolls(UniformLootNumberProvider.create(0, 1));
                    tableBuilder.pool(poolBuilder);
                } else if (Config.INSTANCE.get().lootConfig.lootCodicesModify().containsKey(key)) {
                    List<LootPoolEntry> entries = Config.INSTANCE.get().lootConfig.lootCodicesModify().get(key).stream().map(
                            number -> ItemEntry.builder(CustomItems.ANCIENT_CODEX).apply(
                                SetComponentsLootFunction.builder(AncientCodexItem.NUMBER, number)
                            ).build()
                    ).toList();
                    tableBuilder.modifyPools(pool -> pool.with(entries));
                }
                if (Config.INSTANCE.get().lootConfig.assortedPotionsAdd().containsKey(key)) {
                    List<Integer> rolls = Config.INSTANCE.get().lootConfig.assortedPotionsAdd().get(key);
                    int min_rolls = !rolls.isEmpty() ? rolls.get(0) : 1;
                    LootPool.Builder assortedPotionsBuilder = LootPool.builder();
                    assortedPotionsBuilder = assortedPotionsBuilder.with(LootTableEntry.builder(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/assorted_potions"))));
                    tableBuilder.pool(assortedPotionsBuilder.rolls(UniformLootNumberProvider.create(min_rolls, rolls.size() > 1 ? rolls.get(1) : min_rolls)));
                }
                Optional<RegistryKey<LootTable>> extraTable = Optional.empty();
                if (key.equals(LootTables.RUINED_PORTAL_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/ruined_portal_extra")));
                } else if (key.equals(LootTables.SHIPWRECK_MAP_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/shipwreck_map")));
                } else if (key.equals(LootTables.BASTION_TREASURE_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/bastion_treasure")));
                } else if (key.equals(LootTables.UNDERWATER_RUIN_BIG_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/underwater_ruin_big")));
                } else if (key.equals(LootTables.JUNGLE_TEMPLE_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/jungle_temple")));
                } else if (key.equals(LootTables.VILLAGE_CARTOGRAPHER_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/village_cartographer")));
                } else if (key.equals(LootTables.WOODLAND_MANSION_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/woodland_mansion")));
                } else if (key.equals(LootTables.PILLAGER_OUTPOST_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/pillager_outpost")));
                } else if (key.equals(LootTables.ANCIENT_CITY_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/ancient_city")));
                } else if (key.equals(LootTables.SIMPLE_DUNGEON_CHEST)) {
                    extraTable = Optional.ofNullable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/simple_dungeon")));
                }
                extraTable.ifPresent(lootTableRegistryKey -> tableBuilder.pool(LootPool.builder().with(LootTableEntry.builder(lootTableRegistryKey))));
            }
        });

        if (Config.INSTANCE.get().mechanicsConfig.sleepTime() >= 0) {
            EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, pos, isNight) -> !isNight || (player.getWorld().getLunarTime() % 24000 < Config.INSTANCE.get().mechanicsConfig.sleepTime() /*player.getWorld().getAmbientDarkness() < 11*/ && !player.getWorld().isThundering()) ? ActionResult.FAIL : ActionResult.SUCCESS);
        }

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            for (BrewingPotionRecipe brewingPotionRecipe : Config.INSTANCE.get().potionsConfig.recipes()) {
                if (brewingPotionRecipe.ingredient().equals(Items.AIR)) continue;
                builder.registerPotionRecipe(brewingPotionRecipe.input(), brewingPotionRecipe.ingredient(), brewingPotionRecipe.output());
            }
        });

        FuelRegistryEvents.BUILD.register(((builder, context) -> {
            builder.add(Items.LAVA_BUCKET, 200);
        }));

        UnruffledMod.init();
        BiomeModifications.addSpawn(
                ctx -> ctx.getBiomeKey().getValue().equals(Identifier.ofVanilla("nether_wastes")),
                SpawnGroup.MONSTER, EntityType.BLAZE, 20, 1, 1
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(UnruffledMod.MOD_ID, "ore_emerald_lower"))
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(), GenerationStep.Feature.LAKES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(UnruffledMod.MOD_ID, "underground_pond"))
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(), GenerationStep.Feature.LAKES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(UnruffledMod.MOD_ID, "underground_cabin"))
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OCEAN), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(UnruffledMod.MOD_ID, "ore_prismarine"))
        );
    }
}
