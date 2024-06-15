package io.github.orlouge.unruffled;

import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import io.github.orlouge.unruffled.items.AncientCodexItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Trades {
    public static final TradeOffers.Factory[] WANDERING_TRADER_BUY = new TradeOffers.Factory[] {
            new TradeOffers.BuyForOneEmeraldFactory(Items.DIAMOND, 1, 64, 12),
            new BuyNbtItemFactory(Items.DIAMOND, 1, 2, 32, 12),
            new BuyNbtItemFactory(Items.DIAMOND, 1, 3, 32, 12),
            new BuyNbtItemFactory(CustomItems.EVIL_TOTEM, 1, 64, 12, 12)
    };
    public static final TradeOffers.Factory[] WANDERING_TRADER_CODEX = IntStream.rangeClosed(1, 50).mapToObj(
            number -> new BuyNbtItemFactory(CustomItems.ANCIENT_CODEX, stack -> AncientCodexItem.setNumber(stack, number), 1, 32, 12, 12)
    ).toArray(TradeOffers.Factory[]::new);
    public static final TradeOffers.Factory[] WANDERING_TRADER_DECORATION = new TradeOffers.Factory[] {
            new TradeOffers.SellItemFactory(Items.TERRACOTTA, 1, 16, 64, 1),
            new TradeOffers.SellItemFactory(Items.OCHRE_FROGLIGHT, 1, 8, 64, 1),
            new TradeOffers.SellItemFactory(Items.VERDANT_FROGLIGHT, 1, 8, 64, 1),
            new TradeOffers.SellItemFactory(Items.PEARLESCENT_FROGLIGHT, 1, 8, 64, 1),
            new TradeOffers.SellItemFactory(Items.QUARTZ_BLOCK, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.PRISMARINE_CRYSTALS, 1, 8, 64, 1),
            new TradeOffers.SellItemFactory(Items.CANDLE, 1, 16, 64, 1),
    };
    public static final TradeOffers.Factory[] WANDERING_TRADER_GLAZED_TERRACOTTA = new TradeOffers.Factory[] {
            new TradeOffers.SellItemFactory(Items.RED_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.ORANGE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.YELLOW_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.LIME_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.GREEN_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.CYAN_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.BLUE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.WHITE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.PINK_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.MAGENTA_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.PURPLE_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.BROWN_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.BLACK_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.GRAY_GLAZED_TERRACOTTA, 1, 12, 64, 1),
            new TradeOffers.SellItemFactory(Items.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 12, 64, 1)
    };
    public static final TradeOffers.Factory[] WANDERING_TRADER_ASSORTED = new TradeOffers.Factory[] {
            new TradeOffers.SellItemFactory(Items.FEATHER, 1, 16, 12, 1),
            new TradeOffers.SellItemFactory(Items.BUNDLE, 1, 1, 12, 1),
            new TradeOffers.SellItemFactory(Items.SHULKER_SHELL, 8, 1, 2, 1),
            new TradeOffers.SellItemFactory(Items.OBSIDIAN, 1, 5, 12, 1),
            new TradeOffers.SellItemFactory(Items.SPONGE, 1, 8, 6, 1),
            new TradeOffers.SellItemFactory(Items.AMETHYST_SHARD, 1, 5, 12, 1),
            new TradeOffers.SellItemFactory(Items.PHANTOM_MEMBRANE, 4, 1, 4, 1),
            new TradeOffers.SellItemFactory(Items.RECOVERY_COMPASS, 16, 1, 1, 1),
            new TradeOffers.SellItemFactory(Items.ENDER_EYE, 1, 1, 12, 1)
    };
    public static final TradeOffers.Factory[] WANDERING_TRADER_POTIONS = new TradeOffers.Factory[] {
            new SellNbtItemFactory(Items.SPLASH_POTION, stack -> PotionUtil.setPotion(stack, Potions.STRONG_HEALING), 1, 1, 16, 1),
            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.STRONG_HEALING), 1, 1, 16, 1),
            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.REGENERATION), 1, 1, 16, 1),
            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.STRONG_REGENERATION), 2, 1, 16, 1),
            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.LONG_REGENERATION), 2, 1, 16, 1)
    };
    public static final Map<VillagerProfession, List<TradeOffers.Factory[]>> VILLAGER_TRADES = Map.ofEntries(
            Map.entry(VillagerProfession.ARMORER, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_HELMET), 1, 1, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_BOOTS), 1, 1, 12, 1, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_CHESTPLATE), 2, 1, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_LEGGINGS), 2, 1, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 3, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 2, 1, 12, 4, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_LEGGINGS), 2, 1, 12, 4, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_HORSE_ARMOR), 2, 1, 12, 5, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GOLDEN_HORSE_ARMOR), 4, 1, 12, 5, 0.2F),
                    }
            )),
            Map.entry(VillagerProfession.BUTCHER, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.FEATHER), 1, 24, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CHICKEN),  1, 16, 12, 1, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BEEF), 2, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.LEATHER), 1, 16, 12, 1, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.PORKCHOP), 1, 12, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BONE), 1, 16, 12, 1, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.RABBIT), 1, 8, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.RABBIT_HIDE), 1, 8, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.MUTTON), 1, 16, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.RABBIT_FOOT), 1, 8, 12, 4, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.CARTOGRAPHER, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.COMPASS), 1, 4, 12, 1, 0.2F),
                            new BuyNbtItemFactory(CustomItems.ANCIENT_CODEX, stack -> AncientCodexItem.setNumber(stack, 10), 1, 64, 1, 10)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellMapFactory(6, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapIcon.Type.MONUMENT, 12, 5),
                            new BuyNbtItemFactory(CustomItems.ANCIENT_CODEX, stack -> AncientCodexItem.setNumber(stack, 20), 1, 64, 1, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellMapFactory(8, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapIcon.Type.MANSION, 12, 10),
                            new BuyNbtItemFactory(CustomItems.ANCIENT_CODEX, stack -> AncientCodexItem.setNumber(stack, 30), 1, 64, 1, 10)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.LODESTONE),  4, 1, 12, 10, 0.2F),
                            new BuyNbtItemFactory(CustomItems.ANCIENT_CODEX, stack -> AncientCodexItem.setNumber(stack, 40), 1, 64, 1, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.RECOVERY_COMPASS), 16, 1, 12, 1, 0.2F),
                            new BuyNbtItemFactory(CustomItems.ANCIENT_CODEX, stack -> AncientCodexItem.setNumber(stack, 50), 1, 64, 1, 10)
                    }
            )),
            Map.entry(VillagerProfession.CLERIC, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GLOWSTONE), 1, 16, 12, 1, 0.2F),
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.STRONG_HEALING), 2, 1, 12, 2),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.NETHER_WART),  4, 16, 12, 2, 0.2F),
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.LONG_FIRE_RESISTANCE), 1, 1, 12, 5),
                    },
                    new TradeOffers.Factory[] {
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.LONG_REGENERATION), 2, 1, 12, 10),
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.LONG_WATER_BREATHING), 1, 1, 12, 10),
                    },
                    new TradeOffers.Factory[] {
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.STRONG_REGENERATION), 2, 1, 12, 10),
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.LONG_NIGHT_VISION), 2, 1, 12, 10),
                    },
                    new TradeOffers.Factory[] {
                            new SellNbtItemFactory(Items.POTION, stack -> PotionUtil.setPotion(stack, Potions.LONG_SLOW_FALLING), 1, 1, 12, 10),
                            new BuyNbtItemFactory(CustomItems.EVIL_TOTEM, 1, 32, 12, 10),
                    }
            )),
            Map.entry(VillagerProfession.FARMER, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.WHEAT_SEEDS), 1, 24, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.WHEAT),  1, 16, 12, 1, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.POTATO), 1, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.SUGAR_CANE), 1, 16, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CARROT), 1, 16, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BONE_MEAL), 1, 8, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BEETROOT), 1, 8, 12, 4, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BEETROOT_SEEDS), 1, 16, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.COCOA_BEANS), 1, 8, 12, 5, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.EGG), 1, 8, 12, 5, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.FISHERMAN, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.COD), 1, 8, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.INK_SAC),  1, 8, 12, 1, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.SALMON), 2, 12, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.PUFFERFISH), 2, 8, 12, 3, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.TROPICAL_FISH), 2, 8, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.COD_BUCKET), 1, 1, 12, 10, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.SALMON_BUCKET), 1, 1, 12, 10, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.PUFFERFISH_BUCKET), 1, 1, 12, 10, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GLOW_INK_SAC), 2, 8, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.NAUTILUS_SHELL), 1, 3, 12, 1, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.FLETCHER, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.ARROW), 1, 16, 12, 1, 0.2F),
                            new SellNbtItemFactory(Items.TIPPED_ARROW, stack -> PotionUtil.setPotion(stack, Potions.STRONG_SLOWNESS), 1, 12, 12, 2),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.SPECTRAL_ARROW), 1, 12, 12, 2, 0.2F),
                            new SellNbtItemFactory(Items.TIPPED_ARROW, stack -> PotionUtil.setPotion(stack, Potions.STRONG_HARMING), 1, 8, 12, 3),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(CustomItems.PIERCING_ARROW), 1, 12, 12, 3, 0.2F),
                            new SellNbtItemFactory(Items.TIPPED_ARROW, stack -> PotionUtil.setPotion(stack, Potions.LONG_WEAKNESS), 1, 12, 12, 3),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(CustomItems.IGNITING_ARROW), 1, 12, 12, 4, 0.2F),
                            new SellNbtItemFactory(Items.TIPPED_ARROW, stack -> PotionUtil.setPotion(stack, Potions.STRONG_POISON), 1, 12, 12, 4),
                    },
                    new TradeOffers.Factory[] {
                            new SellNbtItemFactory(Items.TIPPED_ARROW, stack -> PotionUtil.setPotion(stack, Potions.STRONG_TURTLE_MASTER), 1, 8, 12, 5),
                            new SellNbtItemFactory(Items.TIPPED_ARROW, stack -> PotionUtil.setPotion(stack, Potions.STRONG_HEALING), 1, 12, 12, 5),
                    }
            )),
            Map.entry(VillagerProfession.LEATHERWORKER, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellDyedArmorFactory(Items.LEATHER_HELMET, 1, 12, 1),
                            new TradeOffers.SellDyedArmorFactory(Items.LEATHER_BOOTS, 1, 12, 1)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellDyedArmorFactory(Items.LEATHER_CHESTPLATE, 1, 12, 2),
                            new TradeOffers.SellDyedArmorFactory(Items.LEATHER_LEGGINGS, 1, 12, 2),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellDyedArmorFactory(Items.LEATHER_HORSE_ARMOR, 1, 12, 5),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.SADDLE), 1, 1, 12, 3, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.ITEM_FRAME), 1, 32, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GLOW_ITEM_FRAME), 1, 16, 12, 3, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BUNDLE), 2, 1, 12, 5, 0.2F),
                            new TradeOffers.BuyForOneEmeraldFactory(Items.PHANTOM_MEMBRANE, 1, 12, 10)
                    }
            )),
            Map.entry(VillagerProfession.LIBRARIAN, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(1), 1, 64, 1, 10),
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(2), 1, 64, 1, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(3), 1, 64, 1, 10),
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(4), 1, 64, 1, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(5), 1, 64, 1, 10),
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(6), 1, 64, 1, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(7), 1, 64, 1, 10),
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(8), 1, 64, 1, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(9), 1, 64, 1, 10),
                            new TypeAwareBuyNbtItemFactory(CustomItems.ANCIENT_CODEX, setCodexBasedOnBiome(10), 1, 64, 1, 10),
                    }
            )),
            Map.entry(VillagerProfession.MASON, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BRICKS), 1, 16, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_BRICKS),  1, 16, 12, 1, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.POLISHED_ANDESITE), 1, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.POLISHED_DIORITE), 1, 16, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.POLISHED_GRANITE), 1, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CUT_SANDSTONE), 1, 16, 12, 3, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.DEEPSLATE_BRICKS), 1, 16, 12, 5, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.POLISHED_DEEPSLATE), 1, 16, 12, 5, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.QUARTZ_BRICKS), 1, 12, 16, 10, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.DEEPSLATE_TILES), 1, 16, 16, 5, 0.2F)
                    }
            )),
            Map.entry(VillagerProfession.SHEPHERD, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.STRING), 1, 16, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.WHITE_WOOL),  1, 16, 12, 1, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BROWN_WOOL),  1, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BLACK_WOOL),  1, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GRAY_WOOL),  1, 16, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.LIGHT_GRAY_WOOL),  1, 16, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.RED_WOOL),  1, 12, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.PINK_WOOL),  1, 12, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.PURPLE_WOOL),  1, 12, 12, 3, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.MAGENTA_WOOL),  1, 12, 12, 3, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.BLUE_WOOL),  1, 12, 12, 4, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.LIGHT_BLUE_WOOL),  1, 12, 12, 4, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CYAN_WOOL),  1, 12, 12, 4, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GREEN_WOOL),  1, 12, 12, 5, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.LIME_WOOL),  1, 12, 12, 5, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.YELLOW_WOOL),  1, 12, 12, 5, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.ORANGE_WOOL),  1, 12, 12, 5, 0.2F),
                    }
            )),
            Map.entry(VillagerProfession.TOOLSMITH, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_PICKAXE), 1, 1, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_SHOVEL), 1, 1, 12, 1, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_AXE), 1, 1, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_HOE), 1, 1, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new SellNbtItemFactory(CustomItems.IRON_BOLSTER, ItemEnchantmentsHelper::setItemEnchantments, 1, 1, 12, 2),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GOLDEN_PICKAXE), 2, 1, 12, 5, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GOLDEN_AXE), 2, 1, 12, 5, 0.2F),
                            new TradeOffers.BuyForOneEmeraldFactory(Items.DIAMOND, 1, 12, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.CHIPPED_ANVIL), 4, 1, 12, 10, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.DIAMOND_PICKAXE), 16, 1, 12, 10, 0.2F),
                    }
            )),
            Map.entry(VillagerProfession.WEAPONSMITH, List.of(
                    new TradeOffers.Factory[] {},
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_SWORD), 1, 1, 12, 1, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_SWORD), 1, 1, 12, 2, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_AXE), 1, 1, 12, 2, 0.2F),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GOLDEN_SWORD), 1, 1, 12, 4, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.GOLDEN_AXE), 2, 1, 12, 4, 0.2F)
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.SHIELD), 1, 1, 12, 2, 0.2F),
                            new TradeOffers.BuyForOneEmeraldFactory(Items.DIAMOND, 1, 12, 10),
                    },
                    new TradeOffers.Factory[] {
                            new TradeOffers.SellItemFactory(new ItemStack(Items.DIAMOND_SWORD), 16, 1, 12, 10, 0.2F),
                            new TradeOffers.SellItemFactory(new ItemStack(Items.DIAMOND_AXE), 16, 1, 12, 10, 0.2F),
                    }
            ))
    );


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
