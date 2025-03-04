package io.github.orlouge.unruffled;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import io.github.orlouge.unruffled.advancements.*;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.interfaces.HasLockedDeathPosition;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import io.github.orlouge.unruffled.items.ItemEnchantmentsLootFunction;
import io.github.orlouge.unruffled.mixin.accessors.ItemAccessor;
import io.github.orlouge.unruffled.potions.BrewingPotionRecipe;
import io.github.orlouge.unruffled.potions.TeleportEffect;
import io.github.orlouge.unruffled.worldgen.NorthboundGateStructure;
import io.github.orlouge.unruffled.worldgen.UndergroundCabinFeature;
import io.github.orlouge.unruffled.worldgen.UndergroundPondFeature;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.structure.StructureType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UnruffledMod {
    public static final String MOD_ID = "unruffled";
    public static final TagKey<Block> STEADY = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "steady"));
    public static final TagKey<Block> UNSTEADY = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "unsteady"));
    public static final int PROTOCOL_VERSION = 0;

    public static final BadBrewCriterion BAD_BREW_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "bad_brew"), new BadBrewCriterion());
    public static final ChargedTridentCriterion CHARGED_TRIDENT_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "charged_trident"), new ChargedTridentCriterion());
    public static final MagneticTridentCriterion MAGNETIC_TRIDENT_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "magnetic_trident"), new MagneticTridentCriterion());
    public static final FastAttackCriterion FAST_ATTACK_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "fast_attack"), new FastAttackCriterion());
    public static final KnockbackCriterion KNOCKBACK_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "knockback"), new KnockbackCriterion());
    public static final HeavyInventoryCriterion HEAVY_INVENTORY_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "heavy_inventory"), new HeavyInventoryCriterion());
    public static final HeavyEnderChestCriterion HEAVY_ENDER_CHEST_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "heavy_ender_chest"), new HeavyEnderChestCriterion());
    public static final PeacefulChunkCriterion PEACEFUL_CHUNK_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "peaceful_chunk"), new PeacefulChunkCriterion());
    public static final TeleportationCriterion TELEPORTATION_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "teleportation"), new TeleportationCriterion());
    public static final PigTeleportationCriterion PIG_TELEPORTATION_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "pig_teleportation"), new PigTeleportationCriterion());
    public static final AquaAffinityCriterion AQUA_AFFINITY_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "aqua_affinity"), new AquaAffinityCriterion());
    public static final PiercingCriterion PIERCING_CRITERION = Registry.register(Registries.CRITERION, Identifier.of(UnruffledMod.MOD_ID, "piercing"), new PiercingCriterion());

    public static final Supplier<LootFunctionType<ItemEnchantmentsLootFunction>> ITEM_ENCHANTMENTS_LOOT_FUNCTION_TYPE =
        Platform.registerLootFunctionType(Identifier.of(MOD_ID, "item_enchantments"), MapCodec.of(Encoder.empty(), Decoder.unit(new ItemEnchantmentsLootFunction())));

    public static final UndergroundPondFeature UNDERGROUND_POND_FEATURE = new UndergroundPondFeature(DefaultFeatureConfig.CODEC);
    public static final UndergroundCabinFeature UNDERGROUND_CABIN_FEATURE = new UndergroundCabinFeature(DefaultFeatureConfig.CODEC);
    /*
    public static final ConfiguredFeature<DefaultFeatureConfig, UndergroundPondFeature> UNDERGROUND_POND_CONFIGURED_FEATURE =
            new ConfiguredFeature<>(UNDERGROUND_POND_FEATURE, new DefaultFeatureConfig());
    public static final ConfiguredFeature<DefaultFeatureConfig, UndergroundCabinFeature> UNDERGROUND_CABIN_CONFIGURED_FEATURE =
            new ConfiguredFeature<>(UNDERGROUND_CABIN_FEATURE, new DefaultFeatureConfig());
     */
    public static StructureType<NorthboundGateStructure> NORTHBOUND_GATE_STRUCTURE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(UnruffledMod.MOD_ID, "northbound_gate"), () -> NorthboundGateStructure.CODEC);
    public static StructurePieceType NORTHBOUND_GATE_STRUCTURE_PIECE = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(UnruffledMod.MOD_ID, "northbound_gate_piece"), (StructurePieceType.Simple) NorthboundGateStructure.Piece::new);

    public static final RegistryEntry<StatusEffect> TELEPORTATION_EFFECT = Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(UnruffledMod.MOD_ID, "teleportation"), new TeleportEffect()
        .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of(UnruffledMod.MOD_ID, "teleport_slowness"), -0.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Identifier.of(UnruffledMod.MOD_ID, "teleport_fatigue"), -0.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final RegistryEntry<Potion> TELEPORTATION_POTION = Registry.registerReference(Registries.POTION, Identifier.of(UnruffledMod.MOD_ID, "teleportation"), new Potion(new StatusEffectInstance(TELEPORTATION_EFFECT, 100, 0)));

    public static final ComponentType<Boolean> LOCKED_COMPASS_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build();

    public static final Set<RegistryKey<Enchantment>> DEFAULT_DISABLED_ENCHANTMENTS = Set.of(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION,
            Enchantments.FEATHER_FALLING, Enchantments.MENDING, Enchantments.EFFICIENCY, Enchantments.UNBREAKING,
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.QUICK_CHARGE, Enchantments.POWER, Enchantments.IMPALING, Enchantments.LURE
    );

    public static final Map<Item, Map<RegistryKey<Enchantment>, Integer>> DEFAULT_ITEM_ENCHANTMENTS = Map.of(
            CustomItems.IRON_BOLSTER, Map.of(Enchantments.SILK_TOUCH, 1),
            CustomItems.DIAMOND_BOLSTER, Map.of(Enchantments.SILK_TOUCH, 1),
            CustomItems.NETHERITE_BOLSTER, Map.of(Enchantments.SILK_TOUCH, 1),
            CustomItems.CHARGED_TRIDENT, Map.of(Enchantments.CHANNELING, 1),
            CustomItems.MAGNETIC_TRIDENT, Map.of(Enchantments.LOYALTY, 3),
            CustomItems.BLAZING_SWORD, Map.of(Enchantments.FIRE_ASPECT, 2),
            CustomItems.SACRED_SWORD, Map.of(Enchantments.SMITE, 4)
    );

    public static final List<BrewingPotionRecipe> POTION_RECIPES = List.of(
            new BrewingPotionRecipe(Potions.MUNDANE, Items.FERMENTED_SPIDER_EYE, Potions.AWKWARD),
            new BrewingPotionRecipe(Potions.AWKWARD, Items.ENDER_EYE, TELEPORTATION_POTION)
    );

    public static Map<RegistryKey<LootTable>, List<Integer>> DEFAULT_LOOT_CODICES_ADD;

    public static final float DEFAULT_STRUCTURE_SPREAD_FACTOR = 3f;
    public static final float DEFAULT_STRUCTURE_SPREAD_CORRECTION = 64f;

    public static final RegistryKey<LootTable> BOOKSHELF_LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "chests/bookshelf"));
    public static final RegistryKey<LootTable> DECORATED_POT_LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "decorated_pot"));
    public static final RegistryKey<LootTable> NORTHBOUND_GATE_LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(UnruffledMod.MOD_ID, "archaeology/northbound_gate"));
    static {
        DEFAULT_LOOT_CODICES_ADD = Map.ofEntries(
                Map.entry(LootTables.BURIED_TREASURE_CHEST, List.of(1, 4, 6)),
                Map.entry(LootTables.SHIPWRECK_MAP_CHEST, List.of(2, 5, 10)),
                Map.entry(LootTables.UNDERWATER_RUIN_BIG_CHEST, List.of(3, 7, 9)),
                Map.entry(LootTables.SIMPLE_DUNGEON_CHEST, List.of(11, 16)),
                Map.entry(LootTables.ANCIENT_CITY_CHEST, List.of(12, 15, 20)),
                Map.entry(LootTables.BASTION_TREASURE_CHEST, List.of(14, 17, 18)),
                Map.entry(LootTables.IGLOO_CHEST_CHEST, List.of(21, 25, 28)),
                Map.entry(LootTables.STRONGHOLD_LIBRARY_CHEST, List.of(23, 24, 29)),
                Map.entry(LootTables.WOODLAND_MANSION_CHEST, List.of(22, 26, 30)),
                Map.entry(LootTables.DESERT_PYRAMID_CHEST, List.of(31, 34, 36)),
                Map.entry(LootTables.NETHER_BRIDGE_CHEST, List.of(33, 35, 39)),
                Map.entry(LootTables.JUNGLE_TEMPLE_CHEST, List.of(41, 45, 48)),
                Map.entry(LootTables.TRIAL_CHAMBERS_ENTRANCE_CHEST, List.of(15, 27, 38)),
                Map.entry(BOOKSHELF_LOOT_TABLE, List.of(8, 13, 19, 47))
        );
    }

    public static Map<RegistryKey<LootTable>, List<Integer>> DEFAULT_LOOT_CODICES_MODIFY = Map.ofEntries(
            Map.entry(LootTables.DESERT_PYRAMID_ARCHAEOLOGY, List.of(32, 37, 40)),
            Map.entry(LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY, List.of(43, 46, 49)),
            Map.entry(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY, List.of(42, 44, 50)),
            Map.entry(NORTHBOUND_GATE_LOOT_TABLE, List.of(8, 12, 28))
    );

    public static Map<RegistryKey<LootTable>, List<Integer>> DEFAULT_ASSORTED_POTIONS_ADD = Map.ofEntries(
        Map.entry(LootTables.ABANDONED_MINESHAFT_CHEST, List.of(0, 2)),
        Map.entry(LootTables.SIMPLE_DUNGEON_CHEST, List.of(0, 2)),
        Map.entry(LootTables.BASTION_OTHER_CHEST, List.of(0, 2)),
        Map.entry(LootTables.DESERT_PYRAMID_CHEST, List.of(0, 1)),
        Map.entry(LootTables.IGLOO_CHEST_CHEST, List.of(0, 2)),
        Map.entry(LootTables.NETHER_BRIDGE_CHEST, List.of(0, 1)),
        Map.entry(LootTables.SHIPWRECK_TREASURE_CHEST, List.of(0, 1)),
        Map.entry(LootTables.STRONGHOLD_CORRIDOR_CHEST, List.of(0, 2)),
        Map.entry(LootTables.UNDERWATER_RUIN_SMALL_CHEST, List.of(0, 2)),
        Map.entry(LootTables.WOODLAND_MANSION_CHEST, List.of(0, 3)),
        Map.entry(LootTables.PILLAGER_OUTPOST_CHEST, List.of(1, 3)),
        Map.entry(LootTables.JUNGLE_TEMPLE_CHEST, List.of(0, 7)),
        Map.entry(LootTables.END_CITY_TREASURE_CHEST, List.of(0, 2)),
        Map.entry(LootTables.ANCIENT_CITY_ICE_BOX_CHEST, List.of(0, 2)),
        Map.entry(LootTables.VILLAGE_TEMPLE_CHEST, List.of(1, 3)),
        Map.entry(LootTables.VILLAGE_SAVANNA_HOUSE_CHEST, List.of(0, 2)),
        Map.entry(LootTables.TRIAL_CHAMBERS_INTERSECTION_CHEST, List.of(0, 3)),
        Map.entry(LootTables.TRIAL_CHAMBERS_INTERSECTION_BARREL_CHEST, List.of(0, 1))
    );

    public static void init() {
        Packets.AttackMiss.register(player -> {
            if (player.getHungerManager() instanceof ExtendedHungerManager ext) {
                ext.addStaminaIfCanAttack(-0.02f, player);
            }
            player.updateLastActionTime();
            player.swingHand(Hand.MAIN_HAND, false);
        });

        Packets.LockRecoveryCompass.register(player -> {
            if (player instanceof HasLockedDeathPosition lockedDeathPosition && Config.INSTANCE.get().navigationConfig.recoveryCompassLocking()) {
                ItemStack compass = player.getMainHandStack();
                if (!compass.isEmpty() && compass.isOf(Items.RECOVERY_COMPASS)) {
                    compass = compass.copy();
                    if (compass.contains(LOCKED_COMPASS_COMPONENT) && compass.get(LOCKED_COMPASS_COMPONENT)) {
                        /*
                        compass.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
                        compass.remove(DataComponentTypes.LORE);
                        compass.remove(LOCKED_COMPASS_COMPONENT);
                         */
                        compass = new ItemStack(Items.RECOVERY_COMPASS, compass.getCount());
                    } else {
                        if (!player.isInSneakingPose()) {
                            lockedDeathPosition.setLockedDeathPosition();
                        }
                        compass.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                        compass.set(DataComponentTypes.LORE, new LoreComponent(Text.translatable("item.minecraft.recovery_compass.locked").getWithStyle(Style.EMPTY.withColor(Formatting.BLUE))));
                        compass.set(LOCKED_COMPASS_COMPONENT, true);
                    }
                    player.setStackInHand(Hand.MAIN_HAND, compass);
                }
            }
        });


        Config.StackSizeConfig stackSizeConfig = Config.INSTANCE.get().stackSizeConfig;
        if (stackSizeConfig.foodStackSize() != 64) {
            for (Item item : Registries.ITEM.stream().toList()) {
                if (item.getComponents().contains(DataComponentTypes.FOOD) && item.getMaxCount() > 16) {
                    ((ItemAccessor) item).setComponents(ComponentMapImpl.create(item.getComponents(), ComponentChanges.builder().add(DataComponentTypes.MAX_STACK_SIZE, stackSizeConfig.foodStackSize()).build()));
                }
                if (item instanceof TridentItem) {
                    ((ItemAccessor) item).setComponents(ComponentMapImpl.create(item.getComponents(), ComponentChanges.builder().add(DataComponentTypes.MAX_DAMAGE, item.getComponents().get(DataComponentTypes.MAX_DAMAGE) * 4).build()));
                }
            }
        }
        for (Map.Entry<Item, Integer> itemStackSize : stackSizeConfig.itemStackSize().entrySet()) {
            ((ItemAccessor) itemStackSize.getKey()).setComponents(ComponentMapImpl.create(itemStackSize.getKey().getComponents(), ComponentChanges.builder().add(DataComponentTypes.MAX_STACK_SIZE, itemStackSize.getValue()).build()));
        }
        //((ItemAccessor) Items.POTION).setMaxCount(16);
        //((ToolMaterialsAccessor) (Object) ToolMaterials.GOLD).setItemDurability(200);
    }

    public static void sendLockedDeathPosition(ServerPlayerEntity player, Optional<GlobalPos> pos) {
        new Packets.LockedDeathPositionUpdate(pos).sendToPlayer(player);
    }

    public static void sendLockedDeathPosition(ServerPlayerEntity player) {
        if (player instanceof HasLockedDeathPosition pos) sendLockedDeathPosition(player, pos.getLockedDeathPosition());
    }
}
