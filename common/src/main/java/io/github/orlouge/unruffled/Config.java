package io.github.orlouge.unruffled;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.dynamic.Codecs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config {
    public final HungerConfig hungerConfig;
    public final EnchantmentsConfig enchantmentsConfig;
    public final ElytraConfig elytraConfig;
    public final MechanicsConfig mechanicsConfig;
    public final WorldgenConfig worldgenConfig;
    public final LootConfig lootConfig;
    public final Trades.TradesConfig tradesConfig;

    public static final String CONFIG_FNAME = Platform.getConfigDirectory() + "/" + UnruffledMod.MOD_ID + ".json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }

    private static Config read() {
        File file = new File(CONFIG_FNAME);
        Config defaultConfig = new Config();
        if (file.isFile()) {
            try (FileReader in = new FileReader(file)) {
                return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(in)).getOrThrow(false, System.out::println);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.copy(Path.of(CONFIG_FNAME), Path.of(CONFIG_FNAME + ".old"));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        {
            try (FileWriter out = new FileWriter(file)) {
                Optional<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, defaultConfig).resultOrPartial(System.out::println);
                if (result.isPresent()) {
                    out.write(GSON.toJson(result.get()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loaded = true;
        return defaultConfig;
    }

    public Config() {
        this(
            new HungerConfig(), new EnchantmentsConfig(), new ElytraConfig(), new MechanicsConfig(),
            new WorldgenConfig(), new LootConfig(), Trades.DEFAULT_CONFIG
        );
    }

    public Config(
        HungerConfig hungerConfig, EnchantmentsConfig enchantmentsConfig, ElytraConfig elytraConfig, MechanicsConfig mechanicsConfig,
        WorldgenConfig worldgenConfig, LootConfig lootConfig, Trades.TradesConfig tradesConfig) {
        this.hungerConfig = hungerConfig;
        this.tradesConfig = tradesConfig;
        this.worldgenConfig = worldgenConfig;
        this.elytraConfig = elytraConfig;
        this.mechanicsConfig = mechanicsConfig;
        this.enchantmentsConfig = enchantmentsConfig;
        this.lootConfig = lootConfig;
    }

    public static Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HungerConfig.CODEC.fieldOf("hunger").forGetter(config -> config.hungerConfig),
            EnchantmentsConfig.CODEC.fieldOf("enchantments").forGetter(config -> config.enchantmentsConfig),
            ElytraConfig.CODEC.fieldOf("elytra").forGetter(config -> config.elytraConfig),
            MechanicsConfig.CODEC.fieldOf("mechanics").forGetter(config -> config.mechanicsConfig),
            WorldgenConfig.CODEC.fieldOf("worldgen").forGetter(config -> config.worldgenConfig),
            LootConfig.CODEC.fieldOf("loot").forGetter(config -> config.lootConfig),
            Trades.TradesConfig.CODEC.fieldOf("trades").forGetter(config -> config.tradesConfig)
    ).apply(instance, Config::new));

    public static final Lazy<Config> INSTANCE = new Lazy<>(Config::read);

    public record HungerConfig(
            float staminaDepletionRate, float travelPenaltyFactor, float maxTravelPenalty,
            float hungerDepletionRate, float staminaRegenerationRate, float inventoryWeightPenaltyFactor,
            float attackExhaustionFactor, boolean attackAlwaysAllowInTime,
            float eatCooldownFactor, float wearinessIncreaseFactor
    ) {
        public HungerConfig() {
            this(0.25f, 0.003f, 2.8f, 0.2f, 0.0028f, 1f, 2f, true, 0.2f, 0.1f);
        }

        public static Codec<HungerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("stamina_depletion_rate").forGetter(HungerConfig::staminaDepletionRate),
                Codec.FLOAT.fieldOf("travel_penalty_factor").forGetter(HungerConfig::travelPenaltyFactor),
                Codec.FLOAT.fieldOf("travel_penalty_max").forGetter(HungerConfig::maxTravelPenalty),
                Codec.FLOAT.fieldOf("hunger_depletion_rate").forGetter(HungerConfig::hungerDepletionRate),
                Codec.FLOAT.fieldOf("stamina_regeneration_factor").forGetter(HungerConfig::staminaRegenerationRate),
                Codec.FLOAT.fieldOf("inventory_weight_penalty_factor").forGetter(HungerConfig::inventoryWeightPenaltyFactor),
                Codec.FLOAT.fieldOf("attack_exhaustion_factor").forGetter(HungerConfig::attackExhaustionFactor),
                Codec.BOOL.fieldOf("attack_always_allow_in_time").forGetter(HungerConfig::attackAlwaysAllowInTime),
                Codec.FLOAT.fieldOf("eat_cooldown_factor").forGetter(HungerConfig::eatCooldownFactor),
                Codec.FLOAT.fieldOf("weariness_increase_factor").forGetter(HungerConfig::wearinessIncreaseFactor)
        ).apply(instance, HungerConfig::new));
    }


    public record EnchantmentsConfig(
        boolean disableEnchantingTable, boolean disableGrindstone, boolean disableRepairXpCost, boolean disableGlint, boolean showLevelNumber,
        Set<Enchantment> unobtainableEnchantments, Set<Enchantment> disabledEnchantments, Map<Item, Map<Enchantment, Integer>> itemEnchantments) {

        public EnchantmentsConfig() {
            this(true, true, true, true, false, UnruffledMod.DEFAULT_UNOBTAINABLE_ENCHANTMENTS, UnruffledMod.DEFAULT_DISABLED_ENCHANTMENTS, UnruffledMod.DEFAULT_ITEM_ENCHANTMENTS);
        }

        public static Codec<Set<Enchantment>> ENCHANTMENT_SET_CODEC = new ListCodec<>(Registries.ENCHANTMENT.getCodec()).xmap(HashSet::new, LinkedList::new);
        public static Codec<Map<Enchantment, Integer>> ENCHANTMENT_MAP_CODEC = Codec.unboundedMap(Registries.ENCHANTMENT.getCodec(), Codecs.POSITIVE_INT);
        public static final Codec<EnchantmentsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("disable_enchanting_table").forGetter(EnchantmentsConfig::disableEnchantingTable),
            Codec.BOOL.fieldOf("disable_grindstone").forGetter(EnchantmentsConfig::disableGrindstone),
            Codec.BOOL.fieldOf("disable_repair_xp_cost").forGetter(EnchantmentsConfig::disableRepairXpCost),
            Codec.BOOL.fieldOf("disable_glint").forGetter(EnchantmentsConfig::disableGlint),
            Codec.BOOL.fieldOf("display_level_number").forGetter(EnchantmentsConfig::showLevelNumber),
            ENCHANTMENT_SET_CODEC.fieldOf("unselectable_enchantments").forGetter(config -> config.unobtainableEnchantments),
            ENCHANTMENT_SET_CODEC.fieldOf("disabled_enchantments").forGetter(config -> config.disabledEnchantments),
            Codec.unboundedMap(Registries.ITEM.getCodec(), ENCHANTMENT_MAP_CODEC).fieldOf("intrinsic_enchantments").forGetter(config -> config.itemEnchantments)
            ).apply(instance, EnchantmentsConfig::new)
        );
    }

    public record ElytraConfig(
        double horizontalGlidingSpeedFactor,
        double verticalGlidingSpeedFactor,
        boolean disableRocketSpeedChanges,
        double minRocketSpeed,
        double horizontalRocketSpeedReduction
    ) {
        public ElytraConfig() {
            this(0.97, 0.96, false, 0.04d, 5d);
        }

        public static final Codec<ElytraConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("gliding_speed_horizontal_factor").forGetter(ElytraConfig::horizontalGlidingSpeedFactor),
            Codec.DOUBLE.fieldOf("gliding_speed_vertical_factor").forGetter(ElytraConfig::verticalGlidingSpeedFactor),
            Codec.BOOL.fieldOf("rocket_speed_disable_changes").forGetter(ElytraConfig::disableRocketSpeedChanges),
            Codec.DOUBLE.fieldOf("rocket_speed_minimum_factor").forGetter(ElytraConfig::minRocketSpeed),
            Codec.DOUBLE.fieldOf("rocket_speed_horizontal_reduction").forGetter(ElytraConfig::horizontalRocketSpeedReduction)
        ).apply(instance, ElytraConfig::new));
    }

    public record MechanicsConfig(
        boolean peacefulChunks, int sleepTime, float dropSpreadFactor,
        boolean disableTotemOfUndying, boolean evokerDropsEvilTotem, boolean badOmenFromEvoker, boolean badOmenFromCaptain,
        boolean canTeleportMobs, float potionDurationFactor, int bundleSize, int wanderingSpawnFrequency) {
        public MechanicsConfig() {
            this(
                true, 16000, 0.2f,
                true, true, true, false,
                true, 2f, 256, 5
            );
        }
        public static final Codec<MechanicsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("no_hostile_mobs_around_spawn_beds").forGetter(config -> config.peacefulChunks),
            Codec.INT.fieldOf("sleep_time___set_to_negative_to_disable").forGetter(config -> config.sleepTime),
            Codec.FLOAT.fieldOf("drop_spread_factor").forGetter(config -> config.dropSpreadFactor),
            Codec.BOOL.fieldOf("disable_totem_of_undying").forGetter(config -> config.disableTotemOfUndying),
            Codec.BOOL.fieldOf("evoker_drops_evil_totem").forGetter(config -> config.evokerDropsEvilTotem),
            Codec.BOOL.fieldOf("evoker_gives_bad_omen").forGetter(config -> config.badOmenFromEvoker),
            Codec.BOOL.fieldOf("captain_gives_bad_omen").forGetter(config -> config.badOmenFromCaptain),
            Codec.BOOL.fieldOf("potion_can_teleport_mobs").forGetter(config -> config.canTeleportMobs),
            Codec.FLOAT.fieldOf("potion_duration_factor").forGetter(config -> config.potionDurationFactor),
            Codec.INT.fieldOf("bundle_size").forGetter(config -> config.bundleSize),
            Codec.INT.fieldOf("wandering_trader_spawn_frequency").forGetter(config -> config.wanderingSpawnFrequency)
            ).apply(instance, MechanicsConfig::new));
    }

    public record WorldgenConfig(float structureSpreadFactor, float structureSpreadCorrection, boolean disableOreVeins) {
        public WorldgenConfig() {
            this(UnruffledMod.DEFAULT_STRUCTURE_SPREAD_FACTOR, UnruffledMod.DEFAULT_STRUCTURE_SPREAD_CORRECTION, true);
        }

        public static final Codec<WorldgenConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("structure_spread_factor").forGetter(config -> config.structureSpreadFactor),
            Codec.FLOAT.fieldOf("structure_spread_correction").forGetter(config -> config.structureSpreadCorrection),
            Codec.BOOL.fieldOf("disable_large_ore_veins").forGetter(config -> config.disableOreVeins)
            ).apply(instance, WorldgenConfig::new));
    }

    public record LootConfig(Map<Identifier, List<Integer>> lootCodicesAdd, Map<Identifier, List<Integer>> lootCodicesModify) {
        public LootConfig() {
            this(UnruffledMod.DEFAULT_LOOT_CODICES_ADD, UnruffledMod.DEFAULT_LOOT_CODICES_MODIFY);
        }

        public static final Codec<LootConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, new ListCodec<>(Codecs.rangedInt(1, 50))).fieldOf("ancient_codices_numbers_chest").forGetter(config -> config.lootCodicesAdd),
            Codec.unboundedMap(Identifier.CODEC, new ListCodec<>(Codecs.rangedInt(1, 50))).fieldOf("ancient_codices_numbers_archaeology").forGetter(config -> config.lootCodicesModify)
        ).apply(instance, LootConfig::new));
    }
}
