package io.github.orlouge.unruffled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import net.minecraft.world.gen.structure.Structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Trades {
    private static final ConfiguredTrade[] WANDERING_TRADER_BUY = new ConfiguredTrade[] {
            buyItemForOneEmerald(Items.DIAMOND, 1, 128, 12),
            buyItem(Items.DIAMOND, 1, 3, 42, 12),
            buyItem(Items.DIAMOND, 1, 4, 32, 12),
            buyItem(CustomItems.EVIL_TOTEM, 1, 64, 12, 12)
    };
    private static final ConfiguredTrade[] WANDERING_TRADER_CODEX = IntStream.rangeClosed(1, 50).mapToObj(
            number -> buyAncientCodex(number, 32, 12, 12)
    ).toArray(ConfiguredTrade[]::new);
    private static final ConfiguredTrade[] WANDERING_TRADER_DECORATION = new ConfiguredTrade[] {
            sellItem(Items.TERRACOTTA, 1, 16, 64, 1),
            sellItem(Items.OCHRE_FROGLIGHT, 1, 8, 64, 1),
            sellItem(Items.VERDANT_FROGLIGHT, 1, 8, 64, 1),
            sellItem(Items.PEARLESCENT_FROGLIGHT, 1, 8, 64, 1),
            sellItem(Items.QUARTZ_BLOCK, 1, 12, 64, 1),
            sellItem(Items.PRISMARINE_CRYSTALS, 1, 8, 64, 1),
            sellItem(Items.CANDLE, 1, 16, 64, 1),
    };
    private static final ConfiguredTrade[] WANDERING_TRADER_GLAZED_TERRACOTTA = new ConfiguredTrade[] {
            sellItem(Items.RED_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.ORANGE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.YELLOW_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.LIME_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.GREEN_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.CYAN_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.BLUE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.WHITE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.PINK_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.MAGENTA_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.PURPLE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.BROWN_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.BLACK_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.GRAY_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            sellItem(Items.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 12, 64, 1)
    };
    private static final ConfiguredTrade[] WANDERING_TRADER_ASSORTED = new ConfiguredTrade[] {
            sellItem(Items.FEATHER, 1, 16, 12, 1),
            sellItem(Items.FERMENTED_SPIDER_EYE, 1, 4, 12, 1),
            sellItem(Items.BUNDLE, 1, 1, 12, 1),
            sellItem(Items.SHULKER_SHELL, 8, 1, 2, 1),
            sellItem(Items.OBSIDIAN, 1, 5, 12, 1),
            sellItem(Items.SPONGE, 1, 8, 6, 1),
            sellItem(Items.AMETHYST_SHARD, 1, 5, 12, 1),
            sellItem(Items.PHANTOM_MEMBRANE, 4, 1, 4, 1),
            sellItem(Items.RECOVERY_COMPASS, 16, 1, 1, 1),
            sellItem(Items.ENDER_EYE, 1, 1, 12, 1),
            sellWithItemEnchantments(CustomItems.SACRED_SWORD, 48, 1, 1, 10),
            sellItem(Items.LODESTONE, 24, 1, 1, 10, 0.2F),
    };
    private static final ConfiguredTrade[] WANDERING_TRADER_POTIONS = new ConfiguredTrade[] {
            sellWithPotion(Items.SPLASH_POTION, Potions.STRONG_HEALING, 1, 1, 16, 1),
            sellWithPotion(Items.POTION, Potions.STRONG_HEALING, 1, 1, 16, 1),
            sellWithPotion(Items.POTION, Potions.REGENERATION, 1, 1, 16, 1),
            sellWithPotion(Items.POTION, Potions.STRONG_REGENERATION, 2, 1, 16, 1),
            sellWithPotion(Items.POTION, Potions.LONG_REGENERATION, 2, 1, 16, 1)
    };
    private static final Map<VillagerProfession, List<ConfiguredTrade[]>> VILLAGER_TRADES = Map.ofEntries(
            Map.entry(VillagerProfession.ARMORER, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.IRON_HELMET, 1, 1, 12, 1, 0.2F),
                            sellItem(Items.IRON_BOOTS, 1, 1, 12, 1, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.IRON_CHESTPLATE, 2, 1, 12, 2, 0.2F),
                            sellItem(Items.IRON_LEGGINGS, 2, 1, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.CHAINMAIL_HELMET, 1, 1, 12, 3, 0.2F),
                            sellItem(Items.CHAINMAIL_BOOTS, 1, 1, 12, 3, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.CHAINMAIL_CHESTPLATE, 2, 1, 12, 4, 0.2F),
                            sellItem(Items.CHAINMAIL_LEGGINGS, 2, 1, 12, 4, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.IRON_HORSE_ARMOR, 2, 1, 12, 5, 0.2F),
                            sellItem(Items.GOLDEN_HORSE_ARMOR, 4, 1, 12, 5, 0.2F),
                    }
            )),
            Map.entry(VillagerProfession.BUTCHER, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.FEATHER, 1, 24, 12, 1, 0.2F),
                            sellItem(Items.CHICKEN,  1, 16, 12, 1, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.BEEF, 2, 16, 12, 2, 0.2F),
                            sellItem(Items.LEATHER, 1, 8, 12, 1, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.PORKCHOP, 1, 12, 12, 2, 0.2F),
                            sellItem(Items.BONE, 1, 16, 12, 1, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.RABBIT, 1, 8, 12, 3, 0.2F),
                            sellItem(Items.RABBIT_HIDE, 1, 8, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.MUTTON, 1, 16, 12, 3, 0.2F),
                            sellItem(Items.RABBIT_FOOT, 1, 8, 12, 4, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.CARTOGRAPHER, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.COMPASS, 1, 4, 12, 1, 0.2F),
                            buyAncientCodex(10, 64, 1, 10)
                    },
                    new ConfiguredTrade[] {
                            sellMap(StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapIcon.Type.MONUMENT, 6, 12, 5),
                            buyAncientCodex(20, 64, 1, 10),
                    },
                    new ConfiguredTrade[] {
                            sellMap(StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapIcon.Type.MANSION, 8, 12, 10),
                            buyAncientCodex(30, 64, 1, 10)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.LODESTONE,  32, 1, 2, 10, 0.2F),
                            buyAncientCodex(40, 64, 1, 10),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.RECOVERY_COMPASS, 16, 1, 12, 1, 0.2F),
                            buyAncientCodex(50, 64, 1, 10)
                    }
            )),
            Map.entry(VillagerProfession.CLERIC, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.GLOWSTONE, 1, 16, 12, 1, 0.2F),
                            sellWithPotion(Items.POTION, Potions.STRONG_HEALING, 2, 1, 12, 2),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.NETHER_WART,  4, 16, 12, 2, 0.2F),
                            sellWithPotion(Items.POTION, Potions.LONG_FIRE_RESISTANCE, 1, 1, 12, 5),
                    },
                    new ConfiguredTrade[] {
                            sellWithPotion(Items.POTION, Potions.LONG_REGENERATION, 2, 1, 12, 10),
                            sellWithPotion(Items.POTION, Potions.LONG_WATER_BREATHING, 1, 1, 12, 10),
                    },
                    new ConfiguredTrade[] {
                            sellWithPotion(Items.POTION, Potions.STRONG_REGENERATION, 2, 1, 12, 10),
                            sellWithPotion(Items.POTION, Potions.LONG_NIGHT_VISION, 2, 1, 12, 10),
                    },
                    new ConfiguredTrade[] {
                            sellWithPotion(Items.POTION, Potions.LONG_SLOW_FALLING, 1, 1, 12, 10),
                            buyItem(CustomItems.EVIL_TOTEM, 1, 32, 12, 10),
                    }
            )),
            Map.entry(VillagerProfession.FARMER, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.WHEAT_SEEDS, 1, 24, 12, 1, 0.2F),
                            sellItem(Items.WHEAT,  1, 16, 12, 1, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.POTATO, 1, 16, 12, 2, 0.2F),
                            sellItem(Items.SUGAR_CANE, 1, 16, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.CARROT, 1, 16, 12, 3, 0.2F),
                            sellItem(Items.BONE_MEAL, 1, 8, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.BEETROOT, 1, 8, 12, 4, 0.2F),
                            sellItem(Items.BEETROOT_SEEDS, 1, 16, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.COCOA_BEANS, 1, 8, 12, 5, 0.2F),
                            sellItem(Items.EGG, 1, 8, 12, 5, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.FISHERMAN, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.COD, 1, 8, 12, 1, 0.2F),
                            sellItem(Items.INK_SAC,  1, 8, 12, 1, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.SALMON, 2, 12, 12, 2, 0.2F),
                            sellItem(Items.PUFFERFISH, 2, 8, 12, 3, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.TROPICAL_FISH, 2, 8, 12, 1, 0.2F),
                            sellItem(Items.COD_BUCKET, 1, 1, 12, 10, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.SALMON_BUCKET, 1, 1, 12, 10, 0.2F),
                            sellItem(Items.PUFFERFISH_BUCKET, 1, 1, 12, 10, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.GLOW_INK_SAC, 2, 8, 12, 1, 0.2F),
                            sellItem(Items.NAUTILUS_SHELL, 1, 3, 12, 1, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.FLETCHER, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.ARROW, 1, 16, 12, 1, 0.2F),
                            sellWithPotion(Items.TIPPED_ARROW, Potions.STRONG_SLOWNESS, 1, 12, 12, 2),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.SPECTRAL_ARROW, 1, 12, 12, 2, 0.2F),
                            sellWithPotion(Items.TIPPED_ARROW, Potions.STRONG_HARMING, 1, 8, 12, 3),
                    },
                    new ConfiguredTrade[] {
                            sellItem(CustomItems.PIERCING_ARROW, 1, 12, 12, 3, 0.2F),
                            sellWithPotion(Items.TIPPED_ARROW, Potions.LONG_WEAKNESS, 1, 12, 12, 3),
                    },
                    new ConfiguredTrade[] {
                            sellItem(CustomItems.IGNITING_ARROW, 1, 12, 12, 4, 0.2F),
                            sellWithPotion(Items.TIPPED_ARROW, Potions.STRONG_POISON, 1, 12, 12, 4),
                    },
                    new ConfiguredTrade[] {
                            sellWithPotion(Items.TIPPED_ARROW, Potions.STRONG_TURTLE_MASTER, 1, 8, 12, 5),
                            sellWithPotion(Items.TIPPED_ARROW, Potions.STRONG_HEALING, 1, 12, 12, 5),
                    }
            )),
            Map.entry(VillagerProfession.LEATHERWORKER, List.of(
                    new ConfiguredTrade[] {
                            sellDyedArmor(Items.LEATHER_HELMET, 1, 12, 1),
                            sellDyedArmor(Items.LEATHER_BOOTS, 1, 12, 1)
                    },
                    new ConfiguredTrade[] {
                            sellDyedArmor(Items.LEATHER_CHESTPLATE, 1, 12, 2),
                            sellDyedArmor(Items.LEATHER_LEGGINGS, 1, 12, 2),
                    },
                    new ConfiguredTrade[] {
                            sellDyedArmor(Items.LEATHER_HORSE_ARMOR, 1, 12, 5),
                            sellItem(Items.SADDLE, 1, 1, 12, 3, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.ITEM_FRAME, 1, 32, 12, 2, 0.2F),
                            sellItem(Items.GLOW_ITEM_FRAME, 1, 16, 12, 3, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.BUNDLE, 2, 1, 12, 5, 0.2F),
                            buyItemForOneEmerald(Items.PHANTOM_MEMBRANE, 1, 12, 10)
                    }
            )),
            Map.entry(VillagerProfession.LIBRARIAN, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.BOOK, 1, 4, 12, 1),
                            buyBiomeBasedAncientCodex(1, 64, 1, 10),
                    },
                    new ConfiguredTrade[] {
                            buyBiomeBasedAncientCodex(2, 64, 1, 30),
                            buyBiomeBasedAncientCodex(3, 64, 1, 30),
                    },
                    new ConfiguredTrade[] {
                            buyBiomeBasedAncientCodex(4, 64, 1, 40),
                            buyBiomeBasedAncientCodex(5, 64, 1, 40),
                    },
                    new ConfiguredTrade[] {
                            buyBiomeBasedAncientCodex(6, 64, 1, 50),
                            buyBiomeBasedAncientCodex(7, 64, 1, 50),
                    },
                    new ConfiguredTrade[] {
                            buyBiomeBasedAncientCodex(8, 64, 1, 125),
                            buyBiomeBasedAncientCodex(9, 64, 1, 125),
                    }
            )),
            Map.entry(VillagerProfession.MASON, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.BRICKS, 1, 16, 12, 1, 0.2F),
                            sellItem(Items.STONE_BRICKS,  1, 16, 12, 1, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.POLISHED_ANDESITE, 1, 16, 12, 2, 0.2F),
                            sellItem(Items.POLISHED_DIORITE, 1, 16, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.POLISHED_GRANITE, 1, 16, 12, 3, 0.2F),
                            sellItem(Items.CUT_SANDSTONE, 1, 16, 12, 3, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.DEEPSLATE_BRICKS, 1, 16, 12, 5, 0.2F),
                            sellItem(Items.POLISHED_DEEPSLATE, 1, 16, 12, 5, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.QUARTZ_BRICKS, 1, 12, 16, 10, 0.2F),
                            sellItem(Items.DEEPSLATE_TILES, 1, 16, 16, 5, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.SHEPHERD, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.STRING, 1, 16, 12, 1, 0.2F),
                            sellItem(Items.WHITE_WOOL,  1, 16, 12, 1, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.BROWN_WOOL,  1, 16, 12, 2, 0.2F),
                            sellItem(Items.BLACK_WOOL,  1, 16, 12, 2, 0.2F),
                            sellItem(Items.GRAY_WOOL,  1, 16, 12, 2, 0.2F),
                            sellItem(Items.LIGHT_GRAY_WOOL,  1, 16, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.RED_WOOL,  1, 12, 12, 3, 0.2F),
                            sellItem(Items.PINK_WOOL,  1, 12, 12, 3, 0.2F),
                            sellItem(Items.PURPLE_WOOL,  1, 12, 12, 3, 0.2F),
                            sellItem(Items.MAGENTA_WOOL,  1, 12, 12, 3, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.BLUE_WOOL,  1, 12, 12, 4, 0.2F),
                            sellItem(Items.LIGHT_BLUE_WOOL,  1, 12, 12, 4, 0.2F),
                            sellItem(Items.CYAN_WOOL,  1, 12, 12, 4, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.GREEN_WOOL,  1, 12, 12, 5, 0.2F),
                            sellItem(Items.LIME_WOOL,  1, 12, 12, 5, 0.2F),
                            sellItem(Items.YELLOW_WOOL,  1, 12, 12, 5, 0.2F),
                            sellItem(Items.ORANGE_WOOL,  1, 12, 12, 5, 0.2F),
                    }
            )),
            Map.entry(VillagerProfession.TOOLSMITH, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.IRON_PICKAXE, 1, 1, 12, 1, 0.2F),
                            sellItem(Items.IRON_SHOVEL, 1, 1, 12, 1, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.IRON_AXE, 1, 1, 12, 2, 0.2F),
                            sellItem(Items.IRON_HOE, 1, 1, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellWithItemEnchantments(CustomItems.IRON_BOLSTER, 1, 1, 12, 2),
                            sellItem(Items.GOLDEN_PICKAXE, 2, 1, 12, 5, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.GOLDEN_AXE, 2, 1, 12, 5, 0.2F),
                            buyItemForOneEmerald(Items.DIAMOND, 1, 12, 10),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.ANVIL, 5, 1, 12, 10, 0.2F),
                            sellItem(Items.DIAMOND_PICKAXE, 16, 1, 12, 10, 0.2F),
                    }
            )),
            Map.entry(VillagerProfession.WEAPONSMITH, List.of(
                    new ConfiguredTrade[] {
                            sellItem(Items.STONE_SWORD, 1, 1, 12, 1, 0.2F),
                            sellItem(Items.STONE_AXE, 1, 1, 12, 1, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.IRON_SWORD, 1, 1, 12, 2, 0.2F),
                            sellItem(Items.IRON_AXE, 1, 1, 12, 2, 0.2F),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.GOLDEN_SWORD, 1, 1, 12, 4, 0.2F),
                            sellItem(Items.GOLDEN_AXE, 2, 1, 12, 4, 0.2F)
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.SHIELD, 1, 1, 12, 2, 0.2F),
                            buyItemForOneEmerald(Items.DIAMOND, 1, 12, 10),
                    },
                    new ConfiguredTrade[] {
                            sellItem(Items.DIAMOND_SWORD, 16, 1, 12, 10, 0.2F),
                            sellItem(Items.DIAMOND_AXE, 16, 1, 12, 10, 0.2F),
                    }
            ))
    );
    public static final TradesConfig DEFAULT_CONFIG = new TradesConfig(
        Optional.of(VILLAGER_TRADES.entrySet().stream().map(
            entry -> Map.entry(entry.getKey(), new ConfiguredVillagerTrades(
                true,
                entry.getValue().stream().map(
                    pool -> new ConfiguredVillagerPool(2, pool)
                ).toArray(ConfiguredVillagerPool[]::new)
            ))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
        Optional.of(new ConfiguredWanderingTraderTrades(true, new ConfiguredWanderingTraderPool[]{
            new ConfiguredWanderingTraderPool(2, Trades.WANDERING_TRADER_DECORATION),
            new ConfiguredWanderingTraderPool(4, Trades.WANDERING_TRADER_ASSORTED),
            new ConfiguredWanderingTraderPool(1, Trades.WANDERING_TRADER_POTIONS),
            new ConfiguredWanderingTraderPool(1, Trades.WANDERING_TRADER_BUY),
            new ConfiguredWanderingTraderPool(1, Trades.WANDERING_TRADER_CODEX)
        }))
    );

    private static ConfiguredTrade sellItem(Item item, int price, int count, int maxUses, int experience) {
        return sellItem(item, price, count, maxUses, experience, 0.05f);
    }

    private static ConfiguredTrade sellItem(Item item, int price, int count, int maxUses, int experience, float multiplier) {
        return new ConfiguredTrade(
            Optional.of(item), false, price, count, maxUses, experience, multiplier,
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ConfiguredTrade sellMap(TagKey<Structure> structureTag, String nameKey, MapIcon.Type icon, int price, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.empty(), false, price, 1, maxUses, experience, 0.05f,
            Optional.empty(), Optional.empty(),
            Optional.of(structureTag), Optional.of(nameKey), Optional.of(icon),
            Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ConfiguredTrade sellWithPotion(Item item, Potion potion, int price, int count, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.of(item), false, price, count, maxUses, experience, 0.05f,
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.of(potion), Optional.empty(), Optional.empty());
    }

    private static ConfiguredTrade sellWithItemEnchantments(Item item, int price, int count, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.of(item), false, price, count, maxUses, experience, 0.05f,
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.of(true));
    }

    private static ConfiguredTrade sellDyedArmor(Item item, int price, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.of(item), false, price, 1, maxUses, experience, 0.05f,
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.of(true), Optional.empty());
    }

    private static ConfiguredTrade buyItemForOneEmerald(Item item, int count, int maxUses, int experience) {
        // !!!
        return new ConfiguredTrade(
            Optional.of(item), true, 1, count, maxUses, experience, 0.05f,
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ConfiguredTrade buyItem(Item item, int count, int payment, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.of(item), true, payment, count, maxUses, experience, 0.05f,
            Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ConfiguredTrade buyAncientCodex(int number, int payment, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.empty(), true, payment, 1, maxUses, experience, 0.05f,
            Optional.of(number), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ConfiguredTrade buyBiomeBasedAncientCodex(int number, int payment, int maxUses, int experience) {
        return new ConfiguredTrade(
            Optional.empty(), true, payment, 1, maxUses, experience, 0.05f,
            Optional.empty(), Optional.of(number),
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ItemStack process(ItemConvertible item, Function<ItemStack, ItemStack> processor) {
        ItemStack stack = new ItemStack(item);
        stack.setNbt(new NbtCompound());
        stack = processor.apply(stack);
        return stack;
    }

    private static BiFunction<VillagerType, ItemStack, ItemStack> setCodexBasedOnBiome(int add) {
        return (type, stack) -> {
            int base = 10;
            if (type == VillagerType.DESERT) base = 0;
            if (type == VillagerType.SAVANNA || type == VillagerType.JUNGLE) base = 20;
            if (type == VillagerType.SNOW) base = 30;
            if (type == VillagerType.TAIGA) base = 40;
            return AncientCodexItem.setNumber(stack, base + add);
        };
    }

    public record ConfiguredTrade(
        Optional<Item> item,
        boolean buy,
        int payment,
        int count,
        int maxUses,
        int experience,
        float multiplier,
        Optional<Integer> codexNumber,
        Optional<Integer> biomeBasedCodexNumber,
        Optional<TagKey<Structure>> mapStructureTag,
        Optional<String> mapNameKey,
        Optional<MapIcon.Type> mapIcon,
        Optional<Potion> potion,
        Optional<Boolean> randomDyed,
        Optional<Boolean> applyItemEnchantments
    ) {
        public TradeOffers.Factory toFactory() {
            if (mapStructureTag.isPresent()) {
                return new TradeOffers.SellMapFactory(payment, mapStructureTag.get(), mapNameKey.orElse(""), mapIcon.orElse(MapIcon.Type.RED_X), maxUses, experience);
            } else if (randomDyed().orElse(false) && item.isPresent()) {
                return new TradeOffers.SellDyedArmorFactory(item.get(), payment, maxUses, experience);
            } else if (biomeBasedCodexNumber.isPresent()) {
                return new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(biomeBasedCodexNumber.get()), count, payment, maxUses, experience);
            } else {
                Item tradeItem = null;
                Function<ItemStack, ItemStack> itemFunction = null;
                if (codexNumber.isPresent()) {
                    tradeItem = CustomItems.ANCIENT_CODEX;
                    itemFunction = stack -> AncientCodexItem.setNumber(stack, codexNumber.get());
                } else if (item.isPresent()) {
                    tradeItem = item.get();
                    if (potion.isPresent()) {
                        itemFunction = stack -> PotionUtil.setPotion(stack, potion.get());
                    } else if (applyItemEnchantments.orElse(false)) {
                        itemFunction = ItemEnchantmentsHelper::setItemEnchantments;
                    }
                }
                if (tradeItem != null) {
                    if (buy) {
                        if (itemFunction == null) {
                            if (payment == 1) return new TradeOffers.BuyForOneEmeraldFactory(tradeItem, count, maxUses, experience);
                            return new Trades.BuyNbtItemFactory(tradeItem, count, payment, maxUses, experience);
                        } else {
                            return new Trades.BuyNbtItemFactory(tradeItem, itemFunction, count, payment, maxUses, experience);
                        }
                    } else {
                        if (itemFunction == null) {
                            return new TradeOffers.SellItemFactory(tradeItem, payment, count, maxUses, experience);
                        } else {
                            return new SellNbtItemFactory(tradeItem, itemFunction, payment, count, maxUses, experience);
                        }
                    }
                }
            }
            return null;
        }

        public static final Codec<ConfiguredTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().optionalFieldOf("item").forGetter(ConfiguredTrade::item),
            Codec.BOOL.optionalFieldOf("buy", false).forGetter(ConfiguredTrade::buy),
            Codec.INT.optionalFieldOf("payment", 1).forGetter(ConfiguredTrade::payment),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ConfiguredTrade::count),
            Codec.INT.optionalFieldOf("max_uses", 12).forGetter(ConfiguredTrade::maxUses),
            Codec.INT.optionalFieldOf("experience", 1).forGetter(ConfiguredTrade::experience),
            Codec.FLOAT.optionalFieldOf("multiplier", 0.05f).forGetter(ConfiguredTrade::multiplier),
            Codec.INT.optionalFieldOf("codex_number").forGetter(ConfiguredTrade::codexNumber),
            Codec.INT.optionalFieldOf("biome_based_codex_number").forGetter(ConfiguredTrade::biomeBasedCodexNumber),
            Identifier.CODEC.optionalFieldOf("map_structure_tag").xmap(o -> o.map(identifier -> TagKey.of(RegistryKeys.STRUCTURE, identifier)), o -> o.map(TagKey::id)).forGetter(ConfiguredTrade::mapStructureTag),
            Codec.STRING.optionalFieldOf("map_name_key").forGetter(ConfiguredTrade::mapNameKey),
            Codec.INT.optionalFieldOf("map_icon").xmap(o -> o.map(id -> MapIcon.Type.byId((byte) (int) id)), o -> o.map(icon -> (int) icon.getId())).forGetter(ConfiguredTrade::mapIcon),
            Registries.POTION.getCodec().optionalFieldOf("potion").forGetter(ConfiguredTrade::potion),
            Codec.BOOL.optionalFieldOf("random_dyed").forGetter(ConfiguredTrade::randomDyed),
            Codec.BOOL.optionalFieldOf("apply_item_enchantments").forGetter(ConfiguredTrade::applyItemEnchantments)
            ).apply(instance, ConfiguredTrade::new));
    }

    public record ConfiguredWanderingTraderPool(int count, ConfiguredTrade[] trades) {
        public static final Codec<ConfiguredWanderingTraderPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("count", 2).forGetter(ConfiguredWanderingTraderPool::count),
            Codec.list(ConfiguredTrade.CODEC).xmap(list -> list.toArray(ConfiguredTrade[]::new), array -> Arrays.stream(array).toList()).fieldOf("trades").forGetter(ConfiguredWanderingTraderPool::trades)
        ).apply(instance, ConfiguredWanderingTraderPool::new));
    }

    public record ConfiguredVillagerPool(int count, ConfiguredTrade[] trades) {
        public static final Codec<ConfiguredVillagerPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("count", 1).forGetter(ConfiguredVillagerPool::count),
            Codec.list(ConfiguredTrade.CODEC).xmap(list -> list.toArray(ConfiguredTrade[]::new), array -> Arrays.stream(array).toList()).fieldOf("trades").forGetter(ConfiguredVillagerPool::trades)
        ).apply(instance, ConfiguredVillagerPool::new));
    }

    public record ConfiguredWanderingTraderTrades(boolean enabled, ConfiguredWanderingTraderPool[] pools) {
        public static final Codec<ConfiguredWanderingTraderTrades> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(ConfiguredWanderingTraderTrades::enabled),
            Codec.list(ConfiguredWanderingTraderPool.CODEC).xmap(list -> list.toArray(ConfiguredWanderingTraderPool[]::new), array -> Arrays.stream(array).toList()).fieldOf("pools").forGetter(ConfiguredWanderingTraderTrades::pools)
        ).apply(instance, ConfiguredWanderingTraderTrades::new));
    }

    public record ConfiguredVillagerTrades(boolean enabled, ConfiguredVillagerPool[] pools) {
        public static final Codec<ConfiguredVillagerTrades> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(ConfiguredVillagerTrades::enabled),
            Codec.list(ConfiguredVillagerPool.CODEC).xmap(list -> list.toArray(ConfiguredVillagerPool[]::new), array -> Arrays.stream(array).toList()).fieldOf("pools").forGetter(ConfiguredVillagerTrades::pools)
        ).apply(instance, ConfiguredVillagerTrades::new));
    }

    public record TradesConfig(Optional<Map<VillagerProfession, ConfiguredVillagerTrades>> villagerTrades, Optional<ConfiguredWanderingTraderTrades> wanderingTraderTrades) {
        public static final Codec<TradesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Registries.VILLAGER_PROFESSION.getCodec(), ConfiguredVillagerTrades.CODEC).optionalFieldOf("villagers").forGetter(TradesConfig::villagerTrades),
            ConfiguredWanderingTraderTrades.CODEC.optionalFieldOf("wandering_trader").forGetter(TradesConfig::wanderingTraderTrades)
        ).apply(instance, TradesConfig::new));
    }

    static class SellNbtItemFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int price;
        private final int count;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public SellNbtItemFactory(ItemConvertible item, Function<ItemStack, ItemStack> processor, int price, int count, int maxUses, int experience) {
            this(process(item, processor), price, count, maxUses, experience, 0.05F);
        }

        public SellNbtItemFactory(ItemStack stack, int price, int count, int maxUses, int experience, float multiplier) {
            this.sell = stack;
            this.price = price;
            this.count = count;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack sellStack = new ItemStack(this.sell.getItem());
            sellStack.setNbt(this.sell.getNbt());
            sellStack.setCount(this.count);
            return new TradeOffer(new ItemStack(Items.EMERALD, this.price), sellStack, this.maxUses, this.experience, this.multiplier);
        }
    }

    public static class BuyNbtItemFactory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final int count;
        private final int payment;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public BuyNbtItemFactory(ItemConvertible item, Function<ItemStack, ItemStack> processor, int count, int payment, int maxUses, int experience) {
            this(process(item, processor), count, payment, maxUses, experience);
        }

        public BuyNbtItemFactory(ItemConvertible item, int count, int payment, int maxUses, int experience) {
            this(new ItemStack(item), count, payment, maxUses, experience);
        }

        public BuyNbtItemFactory(ItemStack item, int count, int payment, int maxUses, int experience) {
            this.buy = item;
            this.count = count;
            this.payment = payment;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05F;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack buyStack = new ItemStack(this.buy.getItem());
            buyStack.setNbt(this.buy.getNbt());
            buyStack.setCount(this.count);
            return new TradeOffer(buyStack, new ItemStack(Items.EMERALD, this.payment), this.maxUses, this.experience, this.multiplier);
        }
    }

    public static class TypeAwareBuyNbtItemFactory implements TradeOffers.Factory {
        private final Map<VillagerType, ItemStack> buy;
        private final int count;
        private final int payment;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public TypeAwareBuyNbtItemFactory(ItemConvertible item, BiFunction<VillagerType, ItemStack, ItemStack> processor, int count, int payment, int maxUses, int experience) {
            this(
                    Registries.VILLAGER_TYPE.stream().map(
                            type -> Map.entry(type, process(item, stack -> processor.apply(type, stack)))
                    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), count, payment, maxUses, experience
            );
        }

        public TypeAwareBuyNbtItemFactory(Map<VillagerType, ItemStack> item, int count, int payment, int maxUses, int experience) {
            this.buy = item;
            this.count = count;
            this.payment = payment;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05F;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack buyStack;
            if (entity instanceof VillagerDataContainer villager) {
                buyStack = this.buy.get(villager.getVillagerData().getType()).copy();
            } else {
                buyStack = this.buy.values().stream().findFirst().get().copy();
            }
            buyStack.setCount(this.count);
            return new TradeOffer(buyStack, new ItemStack(Items.EMERALD, this.payment), this.maxUses, this.experience, this.multiplier);
        }
    }
}
