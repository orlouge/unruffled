package io.github.orlouge.unruffled;

import io.github.orlouge.unruffled.advancements.*;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import io.github.orlouge.unruffled.mixin.accessors.ItemAccessor;
import io.github.orlouge.unruffled.utils.BrewingPotionRecipe;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UnruffledMod {
    public static final String MOD_ID = "unruffled";
    public static final TagKey<Block> STEADY = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "steady"));
    public static final int PROTOCOL_VERSION = 0;

    public static final BadBrewCriterion BAD_BREW_CRITERION = Criteria.register(new BadBrewCriterion(new Identifier(UnruffledMod.MOD_ID, "bad_brew")));
    public static final ChargedTridentCriterion CHARGED_TRIDENT_CRITERION = Criteria.register(new ChargedTridentCriterion(new Identifier(UnruffledMod.MOD_ID, "charged_trident")));
    public static final MagneticTridentCriterion MAGNETIC_TRIDENT_CRITERION = Criteria.register(new MagneticTridentCriterion(new Identifier(UnruffledMod.MOD_ID, "magnetic_trident")));
    public static final FastAttackCriterion FAST_ATTACK_CRITERION = Criteria.register(new FastAttackCriterion(new Identifier(UnruffledMod.MOD_ID, "fast_attack")));
    public static final KnockbackCriterion KNOCKBACK_CRITERION = Criteria.register(new KnockbackCriterion(new Identifier(UnruffledMod.MOD_ID, "knockback")));
    public static final HeavyInventoryCriterion HEAVY_INVENTORY_CRITERION = Criteria.register(new HeavyInventoryCriterion(new Identifier(UnruffledMod.MOD_ID, "heavy_inventory")));
    public static final PeacefulChunkCriterion PEACEFUL_CHUNK_CRITERION = Criteria.register(new PeacefulChunkCriterion(new Identifier(UnruffledMod.MOD_ID, "peaceful_chunk")));

    public static final UndergroundPondFeature UNDERGROUND_POND_FEATURE = new UndergroundPondFeature(DefaultFeatureConfig.CODEC);
    public static final ConfiguredFeature<DefaultFeatureConfig, UndergroundPondFeature> UNDERGROUND_POND_CONFIGURED_FEATURE =
            new ConfiguredFeature<>(UNDERGROUND_POND_FEATURE, new DefaultFeatureConfig());


    public static final Set<Enchantment> UNOBTAINABLE_ENCHANTMENTS = Registries.ENCHANTMENT.getEntrySet().stream().filter(
            entry -> !entry.getValue().isCursed() && entry.getKey().getValue().getNamespace().equals("minecraft")
    ).map(Map.Entry::getValue).collect(Collectors.toSet());

    public static final Set<Enchantment> DISABLED_ENCHANTMENTS = Set.of(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION,
            Enchantments.FEATHER_FALLING, Enchantments.MENDING, Enchantments.EFFICIENCY, Enchantments.UNBREAKING,
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.QUICK_CHARGE, Enchantments.POWER, Enchantments.IMPALING
    );

    public static final Map<Item, Map<Enchantment, Integer>> ITEM_ENCHANTMENTS = Map.of(
            CustomItems.IRON_BOLSTER, Map.of(Enchantments.SILK_TOUCH, 1),
            CustomItems.DIAMOND_BOLSTER, Map.of(Enchantments.SILK_TOUCH, 1),
            CustomItems.NETHERITE_BOLSTER, Map.of(Enchantments.SILK_TOUCH, 1),
            CustomItems.CHARGED_TRIDENT, Map.of(Enchantments.CHANNELING, 1),
            CustomItems.MAGNETIC_TRIDENT, Map.of(Enchantments.LOYALTY, 3)
    );

    public static final List<BrewingPotionRecipe> POTION_RECIPES = List.of(
            new BrewingPotionRecipe(Potions.MUNDANE, Items.FERMENTED_SPIDER_EYE, Potions.AWKWARD)
    );

    public static Map<Identifier, List<Integer>> LOOT_CODICES_ADD = Map.ofEntries(
            Map.entry(LootTables.BURIED_TREASURE_CHEST, List.of(1, 4, 8, 6)),
            Map.entry(LootTables.SHIPWRECK_MAP_CHEST, List.of(2, 5, 10)),
            Map.entry(LootTables.UNDERWATER_RUIN_BIG_CHEST, List.of(3, 7, 9)),
            Map.entry(LootTables.SIMPLE_DUNGEON_CHEST, List.of(11, 13, 16)),
            Map.entry(LootTables.ANCIENT_CITY_CHEST, List.of(12, 15, 19, 20)),
            Map.entry(LootTables.BASTION_TREASURE_CHEST, List.of(14, 17, 18)),
            Map.entry(LootTables.IGLOO_CHEST_CHEST, List.of(21, 25, 28)),
            Map.entry(LootTables.STRONGHOLD_LIBRARY_CHEST, List.of(23, 24, 27, 29)),
            Map.entry(LootTables.WOODLAND_MANSION_CHEST, List.of(22, 26, 30)),
            Map.entry(LootTables.DESERT_PYRAMID_CHEST, List.of(31, 34, 36)),
            Map.entry(LootTables.NETHER_BRIDGE_CHEST, List.of(33, 35, 38, 39)),
            Map.entry(LootTables.JUNGLE_TEMPLE_CHEST, List.of(41, 45, 47, 48))
    );

    public static Map<Identifier, List<Integer>> LOOT_CODICES_MODIFY = Map.ofEntries(
            Map.entry(LootTables.DESERT_PYRAMID_ARCHAEOLOGY, List.of(32, 37, 40)),
            Map.entry(LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY, List.of(43, 46, 49)),
            Map.entry(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY, List.of(42, 44, 50))
    );

    public static void init() {
        Packets.AttackMiss.register(player -> {
            if (player.getHungerManager() instanceof ExtendedHungerManager ext) {
                ext.addStaminaIfCanAttack(-0.02f, player);
            }
        });
        for (Item item : Registries.ITEM.stream().toList()) {
            if (item.isFood() && item.getMaxCount() > 16) {
                ((ItemAccessor) item).setMaxCount(16);
            }
        }
        ((ItemAccessor) Items.POTION).setMaxCount(16);
        //((ToolMaterialsAccessor) (Object) ToolMaterials.GOLD).setItemDurability(200);
    }
}
