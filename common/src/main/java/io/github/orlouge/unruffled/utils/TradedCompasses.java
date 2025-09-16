package io.github.orlouge.unruffled.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.HasAttachedLodestone;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.*;
import java.util.stream.Stream;

public class TradedCompasses extends PersistentState {
    public final HashMap<UUID, LinkedHashMap<GlobalPos, ItemStack>> availableForBuy;
    public final Map<UUID, List<Compass>> availableForSell;
    public final Map<Text, Integer> usedNames;
    public final Map<Text, Integer> usedLores;
    private final Map<GlobalPos, Lodestone> lodestoneStatus;
    public static final Codec<TradedCompasses> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Uuids.STRING_CODEC, ExtraCodecs.orderedMapAsListOfPairs(GlobalPos.CODEC, ItemStack.CODEC)).fieldOf("buy").forGetter(state -> state.availableForBuy),
        Codec.unboundedMap(Uuids.STRING_CODEC, Codec.list(Compass.CODEC)).fieldOf("sell").forGetter(state -> state.availableForSell),
        ExtraCodecs.mapAsListOfPairs(TextCodecs.CODEC, ExtraCodecs.wrapInRecord(Codec.INT)).fieldOf("names").forGetter(state -> state.usedNames),
        ExtraCodecs.mapAsListOfPairs(TextCodecs.CODEC, ExtraCodecs.wrapInRecord(Codec.INT)).fieldOf("lores").forGetter(state -> state.usedLores),
        ExtraCodecs.mapAsListOfPairs(GlobalPos.CODEC, Lodestone.CODEC).fieldOf("lodestones").forGetter(state -> state.lodestoneStatus)
    ).apply(instance, TradedCompasses::new));
    private static final PersistentStateType<TradedCompasses> TYPE = new PersistentStateType<>(
        UnruffledMod.MOD_ID + "_traded_compasses", TradedCompasses::new, CODEC, null
    );

    public static final int MAX_BUY_PER_PLAYER = 50;
    public static final int MAX_SELL_PER_PLAYER = 30;

    public TradedCompasses() {
        availableForBuy = new HashMap<>();
        availableForSell = new HashMap<>();
        usedNames = new HashMap<>();
        usedLores = new HashMap<>();
        lodestoneStatus = new HashMap<>();
    }

    public TradedCompasses(Map<UUID, LinkedHashMap<GlobalPos, ItemStack>> availableForBuy, Map<UUID, List<Compass>> availableForSell, Map<Text, Integer> usedNames, Map<Text, Integer> usedLores, Map<GlobalPos, Lodestone> lodestoneStatus) {
        this.availableForBuy = new HashMap<>(availableForBuy);
        this.availableForSell = new HashMap<>(availableForSell);
        this.usedNames = new HashMap<>(usedNames);
        this.usedLores =new HashMap<>(usedLores);
        this.lodestoneStatus = new HashMap<>(lodestoneStatus);
    }

    public static TradedCompasses get(PersistentStateManager persistentStateManager) {
        return persistentStateManager.getOrCreate(TYPE);
    }

    public void addBuy(PlayerEntity player, ItemStack compass) {
        compass = compass.copy();
        compass.setCount(1);
        LinkedHashMap<GlobalPos, ItemStack> buy = availableForBuy.computeIfAbsent(player.getUuid(), k -> new LinkedHashMap<>());
        Optional<Compass> compassData = getCompass(compass);
        if (compassData.isEmpty()) return;
        GlobalPos pos = new GlobalPos(compassData.get().dimension, compassData.get().lodestonePos());
        Lodestone status = lodestoneStatus.get(pos);
        if (status != null && status.sellTrades > 0 && !buy.containsKey(pos)) return;
        buy.remove(pos);
        while (buy.size() >= MAX_BUY_PER_PLAYER) {
            buy.pollFirstEntry();
        }
        buy.putLast(pos, compass);
        markDirty();
    }

    public void addSell(ItemStack compassStack, PlayerEntity customer, List<? extends PlayerEntity> possibleSellers) {
        if (customer == null) {
            return;
        }
        Optional<Compass> soldCompass = getCompass(compassStack);
        if (soldCompass.isPresent()) {
            GlobalPos pos = new GlobalPos(soldCompass.get().dimension, soldCompass.get().lodestonePos);
            Lodestone lodestone = lodestoneStatus.get(pos);
            Compass compass;
            if (soldCompass.get().lore.isPresent() && lodestone.name().equals(soldCompass.get().lore())) {
                compass = new Compass(soldCompass.get().name, Optional.empty(), soldCompass.get().lodestonePos, soldCompass.get().dimension);
            } else {
                compass = soldCompass.get();
            }
            Iterable<UUID> keys = possibleSellers == null ? availableForBuy.keySet() : Stream.concat(Stream.of(customer), possibleSellers.stream()).map(PlayerEntity::getUuid).toList();
            for (UUID uuid : keys) {
                LinkedHashMap<GlobalPos, ItemStack> buy = availableForBuy.getOrDefault(uuid, new LinkedHashMap<>());
                if (!buy.isEmpty()) {
                    buy.remove(pos);
                }
            }
            List<Compass> sell = availableForSell.computeIfAbsent(customer.getUuid(), k -> new LinkedList<>());
            while (sell.size() >= MAX_SELL_PER_PLAYER) {
                Compass removedCompass = sell.removeFirst();
                GlobalPos removedCompassPos = new GlobalPos(removedCompass.dimension, removedCompass.lodestonePos);
                Lodestone removedCompassLodestone = lodestoneStatus.get(removedCompassPos);
                if (removedCompassLodestone != null) lodestoneStatus.put(removedCompassPos, new Lodestone(removedCompassLodestone.name, removedCompassLodestone.sellTrades - 1, removedCompassLodestone.displayEntityUUID));
            }
            sell.add(compass);
            lodestoneStatus.put(pos, lodestone == null ? new Lodestone(Optional.empty(), 1, Optional.empty()) : new Lodestone(lodestone.name, lodestone.sellTrades + 1, lodestone.displayEntityUUID));
            compass.name.ifPresent(text -> usedNames.merge(text, 1, (k, v) -> v + 1));
            compass.lore.ifPresent(text -> usedLores.merge(text, 1, (k, v) -> v + 1));
            markDirty();
        }
    }

    public ItemStack getBuy(ServerWorld world, PlayerEntity player) {
        LinkedHashMap<GlobalPos, ItemStack> buy = availableForBuy.getOrDefault(player.getUuid(), new LinkedHashMap<>());
        for (int attempts = 0; !buy.isEmpty() && attempts < 10; attempts++) {
            ItemStack stack = buy.lastEntry().getValue();
            Optional<Compass> topCompass = getCompass(stack);
            if (topCompass.isPresent() && topCompass.get().isValid(world.getServer())) {
                List<Compass> sell = availableForSell.getOrDefault(player.getUuid(), Collections.emptyList());
                for (Compass soldCompass : sell) {
                    if (soldCompass.isClone(topCompass.get())) {
                        stack = null;
                        break;
                    }
                }
                if (stack != null) {
                    return stack;
                }
            }
            buy.pollLastEntry();
            markDirty();
        }
        return null;
    }

    public ItemStack getRandomSell(int attempts, ServerWorld world, Random random, Vec3d posToAvoid) {
        List<UUID> players = new ArrayList<>(availableForSell.keySet());
        int maxAttempts = attempts;
        for (int i = 0; i < maxAttempts; i++) {
            if (players.isEmpty()) return null;
            int index = players.size() == 1 ? 0 : random.nextBetweenExclusive(0, players.size());
            UUID uuid = players.get(index);
            List<Compass> sell = availableForSell.getOrDefault(uuid, Collections.emptyList());
            if (sell.isEmpty()) {
                players.remove(index);
                if (i < 100) maxAttempts++;
                continue;
            }
            Compass compass;
            if (sell.size() == 1) {
                compass = sell.getFirst();
            } else {
                sell = new ArrayList<>(sell);
                Compass last = sell.getLast();
                for (int j = 0; j < sell.size() / 4; j++) {
                    sell.add(last);
                }
                compass = sell.get(random.nextBetweenExclusive(0, sell.size()));
            }
            if (i < maxAttempts - 2 && posToAvoid.distanceTo(compass.lodestonePos.toCenterPos()) * 0.003 + random.nextFloat() < 1) {
                if (i < attempts) maxAttempts++;
                continue;
            }
            if (!compass.isValid(world.getServer())) {
                invalidateCompass(uuid, compass);
                continue;
            }
            if (Items.COMPASS instanceof CompassItem) {
                ItemStack stack = new ItemStack(Items.COMPASS);
                GlobalPos pos = new GlobalPos(compass.dimension, compass.lodestonePos);
                stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(pos), true));
                Text name = null;
                List<Text> lore = null;
                boolean duplicateName = true, duplicateLore = true;
                if (compass.name.isPresent()) {
                    name = compass.name.get();
                    duplicateName = usedNames.getOrDefault(name, 0) > 1;
                }
                Text compassLore = null;
                if (compass.lore.isPresent()) {
                    compassLore = compass.lore.get();
                } else {
                    Lodestone lodestone = lodestoneStatus.get(pos);
                    if (lodestone != null && lodestone.name.isPresent()) compassLore = lodestone.name.get();
                }
                if (compassLore != null) {
                    lore = new ArrayList<>(List.of(compassLore));
                    duplicateLore = usedLores.getOrDefault(compassLore, 0) > 1;
                }
                if (duplicateName && duplicateLore) {
                    Optional<GameProfile> profile = Optional.ofNullable(world.getServer().getUserCache()).flatMap(cache -> cache.getByUuid(uuid));
                    if (profile.isPresent()) {
                        if (lore == null) lore = new ArrayList<>();
                        lore.addAll(Text.of(String.format("%s / %04d", profile.get().getName(), Math.abs(pos.hashCode()) % 10000)).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                    }
                }
                if (name != null) stack.set(DataComponentTypes.CUSTOM_NAME, name);
                if (lore != null) stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
                return stack;
            }
        }
        return null;
    }

    private void invalidateCompass(UUID uuid, Compass compass) {
        List<Compass> sell = availableForSell.getOrDefault(uuid, Collections.emptyList());
        GlobalPos pos = new GlobalPos(compass.dimension, compass.lodestonePos);
        Lodestone lodestoneStatus = this.lodestoneStatus.get(pos);
        if (lodestoneStatus != null) this.lodestoneStatus.put(pos, new Lodestone(lodestoneStatus.name, lodestoneStatus.sellTrades - 1, lodestoneStatus.displayEntityUUID));
        sell = new ArrayList<>(sell.stream().filter(other -> !other.isClone(compass)).toList());
        if (sell.isEmpty()) {
            availableForSell.remove(uuid);
        } else {
            availableForSell.put(uuid, sell);
        }
        if (compass.name.isPresent()) {
            Text name = compass.name.get();
            Integer used = usedNames.getOrDefault(name, 0);
            if (used > 1) {
                usedNames.put(name, used - 1);
            } else if (used == 1) {
                usedNames.remove(name);
            }
        }
        if (compass.lore.isPresent()) {
            Text lore = compass.lore.get();
            Integer used = usedLores.getOrDefault(lore, 0);
            if (used > 1) {
                usedLores.put(lore, used - 1);
            } else if (used == 1) {
                usedLores.remove(lore);
            }
        }
        markDirty();
    }

    public static Optional<Compass> getCompass(ItemStack stack) {
        if (stack.isOf(Items.COMPASS) && stack.contains(DataComponentTypes.LODESTONE_TRACKER)) {
            LodestoneTrackerComponent lodestone = stack.get(DataComponentTypes.LODESTONE_TRACKER);

            if (lodestone == null || lodestone.target().isEmpty()) {
                return Optional.empty();
            }

            RegistryKey<World> lodestoneDimension = lodestone.target().get().dimension();
            BlockPos lodestonePos = lodestone.target().get().pos();
            return Optional.of(new Compass(
                stack.contains(DataComponentTypes.CUSTOM_NAME) && !stack.get(DataComponentTypes.CUSTOM_NAME).asTruncatedString(10).isEmpty() ? Optional.of(stack.getName()) : Optional.empty(),
                stack.contains(DataComponentTypes.LORE) ? Optional.of(stack.get(DataComponentTypes.LORE).lines()).filter(s -> !s.isEmpty()).map(s -> s.get(0)) : Optional.empty(),
                lodestonePos, lodestoneDimension));
        }
        return Optional.empty();
    }

    public void storeLodestoneName(GlobalPos blockPos, Text name, ServerWorld world) {
        int sellTrades = 0;
        Lodestone oldStatus = lodestoneStatus.get(blockPos);
        if (oldStatus != null) {
            sellTrades = oldStatus.sellTrades;
            if (oldStatus.name.isPresent()) {
                Integer used = usedLores.getOrDefault(oldStatus.name.get(), 0);
                if (used > 1) {
                    usedLores.put(oldStatus.name.get(), used - 1);
                } else if (used == 1) {
                    usedLores.remove(oldStatus.name.get());
                }
            }
            oldStatus.displayEntityUUID.flatMap(uuid -> Optional.ofNullable(world.getEntity(uuid))).ifPresent(Entity::discard);
        }
        Optional<UUID> displayUuid = Optional.empty();
        if (name != null) {
            usedLores.compute(name, (k, cnt) -> cnt == null ? 1 : cnt + 1);
            DisplayEntity.TextDisplayEntity display = EntityType.TEXT_DISPLAY.create(world, SpawnReason.LOAD);
            if (display instanceof HasAttachedLodestone attachedLodestone) {
                attachedLodestone.setAttachedLodestone(blockPos.pos(), name);
                world.spawnEntity(display);
                displayUuid = Optional.of(display.getUuid());
            }
        }
        lodestoneStatus.put(blockPos, new Lodestone(Optional.ofNullable(name), sellTrades, displayUuid));
        markDirty();
    }

    public void deleteLodestoneName(GlobalPos blockPos, ServerWorld world) {
        storeLodestoneName(blockPos, null, world);
        //lodestoneStatus.compute(blockPos, (k, v) -> new Lodestone(Optional.empty(), v == null ? 0 : v.sellTrades));
        markDirty();
    }

    public Text getLodestoneName(GlobalPos blockPos) {
        return Optional.ofNullable(lodestoneStatus.get(blockPos)).flatMap(Lodestone::name).orElse(null);
    }


    public record Lodestone(Optional<Text> name, int sellTrades, Optional<UUID> displayEntityUUID) {
        public static final Codec<Lodestone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.optionalFieldOf("name").forGetter(Lodestone::name),
            Codec.INT.fieldOf("trades").forGetter(Lodestone::sellTrades),
            Uuids.STRING_CODEC.optionalFieldOf("display_entity").forGetter(Lodestone::displayEntityUUID)
        ).apply(instance, Lodestone::new));
    }

    public record Compass(Optional<Text> name, Optional<Text> lore, BlockPos lodestonePos, RegistryKey<World> dimension) {
        public static final Codec<Compass> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.optionalFieldOf("name").forGetter(Compass::name),
            TextCodecs.CODEC.optionalFieldOf("lore").forGetter(Compass::lore),
            BlockPos.CODEC.fieldOf("pos").forGetter(Compass::lodestonePos),
            RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("dimension").forGetter(Compass::dimension)
        ).apply(instance, Compass::new));

        public boolean isValid(MinecraftServer server) {
            ServerWorld targetWorld = server.getWorld(dimension);
            if (targetWorld == null) return false;
            return targetWorld.isInBuildLimit(lodestonePos) && targetWorld.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, lodestonePos);
        }

        public boolean isClone(Compass other) {
            return lodestonePos.equals(other.lodestonePos) && dimension.equals(other.dimension);
        }
    }
}
