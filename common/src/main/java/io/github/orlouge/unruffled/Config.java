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
import java.util.*;

public class Config {
    public final HungerConfig hungerConfig;
    public final Set<Enchantment> unobtainableEnchantments;
    public final Set<Enchantment> disabledEnchantments;
    public final Map<Item, Map<Enchantment, Integer>> itemEnchantments;
    public final Map<Identifier, List<Integer>> lootCodicesAdd, lootCodicesModify;
    public final boolean peacefulChunks;
    public final int sleepTime;
    public final float structureSpreadFactor, structureSpreadCorrection;

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
            new HungerConfig(),
            UnruffledMod.DEFAULT_UNOBTAINABLE_ENCHANTMENTS, UnruffledMod.DEFAULT_DISABLED_ENCHANTMENTS,
            UnruffledMod.DEFAULT_ITEM_ENCHANTMENTS,
            UnruffledMod.DEFAULT_LOOT_CODICES_ADD, UnruffledMod.DEFAULT_LOOT_CODICES_MODIFY,
            true, 16000, UnruffledMod.DEFAULT_STRUCTURE_SPREAD_FACTOR, UnruffledMod.DEFAULT_STRUCTURE_SPREAD_CORRECTION
        );
    }

    public Config(HungerConfig hungerConfig, Set<Enchantment>unobtainableEnchantments, Set<Enchantment> disabledEnchantments, Map<Item, Map<Enchantment, Integer>> itemEnchantments, Map<Identifier, List<Integer>> lootCodicesAdd, Map<Identifier, List<Integer>> lootCodicesModify, boolean peacefulChunks, int sleepTime, float structureSpreadFactor, float structureSpreadCorrection) {
        this.hungerConfig = hungerConfig;
        this.unobtainableEnchantments = unobtainableEnchantments;
        this.disabledEnchantments = disabledEnchantments;
        this.itemEnchantments = itemEnchantments;
        this.lootCodicesAdd = lootCodicesAdd;
        this.lootCodicesModify = lootCodicesModify;
        this.peacefulChunks = peacefulChunks;
        this.sleepTime = sleepTime;
        this.structureSpreadFactor = structureSpreadFactor;
        this.structureSpreadCorrection = structureSpreadCorrection;
    }

    public static Codec<Map<Enchantment, Integer>> ENCHANTMENT_MAP_CODEC = Codec.unboundedMap(Registries.ENCHANTMENT.getCodec(), Codecs.POSITIVE_INT);
    public static Codec<Set<Enchantment>> ENCHANTMENT_SET_CODEC = new ListCodec<>(Registries.ENCHANTMENT.getCodec()).xmap(HashSet::new, LinkedList::new);

    public static Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HungerConfig.CODEC.fieldOf("hunger").forGetter(config -> config.hungerConfig),
            ENCHANTMENT_SET_CODEC.fieldOf("unobtainable_enchantments").forGetter(config -> config.unobtainableEnchantments),
            ENCHANTMENT_SET_CODEC.fieldOf("disabled_enchantments").forGetter(config -> config.disabledEnchantments),
            Codec.unboundedMap(Registries.ITEM.getCodec(), ENCHANTMENT_MAP_CODEC).fieldOf("item_enchantments").forGetter(config -> config.itemEnchantments),
            Codec.unboundedMap(Identifier.CODEC, new ListCodec<>(Codecs.rangedInt(1, 50))).fieldOf("loot_codices_numbers_chest").forGetter(config -> config.lootCodicesAdd),
            Codec.unboundedMap(Identifier.CODEC, new ListCodec<>(Codecs.rangedInt(1, 50))).fieldOf("loot_codices_numbers_archaeology").forGetter(config -> config.lootCodicesModify),
            Codec.BOOL.fieldOf("disable_hostile_mobs_around_spawn_beds").forGetter(config -> config.peacefulChunks),
            Codec.INT.fieldOf("sleep_time_set_to_negative_to_disable").forGetter(config -> config.sleepTime),
            Codec.FLOAT.optionalFieldOf("structure_spread_factor", UnruffledMod.DEFAULT_STRUCTURE_SPREAD_FACTOR).forGetter(config -> config.structureSpreadFactor),
            Codec.FLOAT.optionalFieldOf("structure_spread_correction", UnruffledMod.DEFAULT_STRUCTURE_SPREAD_CORRECTION).forGetter(config -> config.structureSpreadCorrection)
    ).apply(instance, Config::new));

    public static final Lazy<Config> INSTANCE = new Lazy<>(() -> Config.read());

    public record HungerConfig(
            float staminaDepletionRate, float travelPenaltyFactor, float maxTravelPenalty,
            float hungerDepletionRate, float staminaRegenerationRate, float inventoryWeightPenaltyFactor,
            float attackExhaustionFactor, boolean attackAlwaysAllowInTime,
            float eatCooldownFactor
    ) {
        public HungerConfig() {
            this(0.25f, 0.003f, 2.8f, 0.2f, 0.0028f, 1f, 2f, true, 0.2f);
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
                Codec.FLOAT.fieldOf("eat_cooldown_factor").forGetter(HungerConfig::eatCooldownFactor)
        ).apply(instance, HungerConfig::new));
    }
}
