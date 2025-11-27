package io.github.orlouge.unruffled.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.orlouge.unruffled.Platform;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.util.Lazy;
import net.minecraft.util.dynamic.Codecs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class Tools {
    public static final String CONFIG_FNAME = Platform.getConfigDirectory() + "/" + UnruffledMod.MOD_ID + "-tools.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean loaded = false;

    public final ToolConfig toolConfig;

    public static boolean isLoaded() {
        return loaded;
    }

    private static Tools read() {
        File file = new File(CONFIG_FNAME);
        Tools defaultConfig = new Tools();
        if (file.isFile()) {
            try (FileReader in = new FileReader(file)) {
                return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(in)).getOrThrow();
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

    public Tools() {
        this(Optional.empty());
    }

    public Tools(Optional<ToolConfig> toolConfig) {
        this.toolConfig = toolConfig.orElse(new ToolConfig());
    }

    public static final Lazy<Tools> INSTANCE = new Lazy<>(Tools::read);

    public static Codec<Tools> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ToolConfig.CODEC.optionalFieldOf("stats").forGetter(config -> Optional.ofNullable(config.toolConfig))
    ).apply(instance, Tools::new));

    public record ToolConfig(Map<String, Integer> durability, MiningSpeedMultiplierCurve miningSpeedMultiplierCurve) {
        public ToolConfig() {
            this(
                Map.of("wood", 118, "stone", 262, "iron", 500, "diamond", 3122, "gold", 200, "netherite", 4062),
                new MiningSpeedMultiplierCurve()
            );
        }

        public static final Codec<ToolConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codecs.POSITIVE_INT).fieldOf("durability").forGetter(ToolConfig::durability),
            MiningSpeedMultiplierCurve.CODEC.fieldOf("mining_speed_multiplier_curve").forGetter(ToolConfig::miningSpeedMultiplierCurve)
        ).apply(instance, ToolConfig::new));

        public record MiningSpeedMultiplierCurve(float a, float b, float c, boolean neverIncrease, boolean neverDecrease) {
            public MiningSpeedMultiplierCurve() { this(0.5f, 0f, 0f, false, true); }

            public float compute(float mul) {
                float mul2 = a * mul * mul + b * mul + c;
                if (neverDecrease) mul2 = Math.max(mul, mul2);
                if (neverIncrease) mul2 = Math.min(mul, mul2);
                return mul2;
            }

            public static final Codec<MiningSpeedMultiplierCurve> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("a").forGetter(MiningSpeedMultiplierCurve::a),
                Codec.FLOAT.fieldOf("b").forGetter(MiningSpeedMultiplierCurve::b),
                Codec.FLOAT.fieldOf("c").forGetter(MiningSpeedMultiplierCurve::c),
                Codec.BOOL.fieldOf("never_increase").forGetter(MiningSpeedMultiplierCurve::neverIncrease),
                Codec.BOOL.fieldOf("never_decrease").forGetter(MiningSpeedMultiplierCurve::neverDecrease)
            ).apply(instance, MiningSpeedMultiplierCurve::new));
        }
    }
}
