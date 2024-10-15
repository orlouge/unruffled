package io.github.orlouge.unruffled.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.*;
import java.util.stream.Stream;

public class TradedCompasses extends PersistentState {
    public final Map<UUID, List<ItemStack>> availableForBuy = new HashMap<>();
    public final Map<UUID, List<Compass>> availableForSell = new HashMap<>();
    public final Map<Text, Integer> usedNames = new HashMap<>();

    public static final int MAX_BUY_PER_PLAYER = 10;
    public static final int MAX_SELL_PER_PLAYER = 30;

    public TradedCompasses() {}

    public static TradedCompasses get(PersistentStateManager persistentStateManager) {
        return persistentStateManager.getOrCreate(TradedCompasses::new, TradedCompasses::new, UnruffledMod.MOD_ID + "_traded_compasses");
    }

    public void addBuy(PlayerEntity player, ItemStack compass) {
        compass = compass.copy();
        compass.setCount(1);
        List<ItemStack> buy = availableForBuy.computeIfAbsent(player.getUuid(), k -> new LinkedList<>());
        while (buy.size() >= MAX_BUY_PER_PLAYER) {
            buy.remove(0);
        }
        buy.add(compass);
        markDirty();
    }

    public void addSell(ItemStack compassStack, PlayerEntity customer, List<? extends PlayerEntity> possibleSellers) {
        if (customer == null) {
            return;
        }
        Optional<Compass> compass = getCompass(compassStack);
        if (compass.isPresent()) {
            Iterable<UUID> keys = possibleSellers == null ? availableForBuy.keySet() : Stream.concat(Stream.of(customer), possibleSellers.stream()).map(PlayerEntity::getUuid).toList();
            for (UUID uuid : keys) {
                List<ItemStack> buy = availableForBuy.getOrDefault(uuid, Collections.emptyList());
                if (!buy.isEmpty()) {
                    int index = buy.size() - 1;
                    Optional<Compass> topCompass = getCompass(buy.get(index));
                    if (topCompass.isPresent() && topCompass.get().isClone(compass.get())) {
                        buy.remove(index);
                    }
                }
            }
            List<Compass> sell = availableForSell.computeIfAbsent(customer.getUuid(), k -> new LinkedList<>());
            while (sell.size() >= MAX_SELL_PER_PLAYER) {
                sell.remove(0);
            }
            sell.add(compass.get());
            compass.get().name.ifPresent(text -> usedNames.merge(text, 1, (k, v) -> v + 1));
            markDirty();
        }
    }

    public ItemStack getBuy(ServerWorld world, PlayerEntity player) {
        List<ItemStack> buy = availableForBuy.getOrDefault(player.getUuid(), Collections.emptyList());
        if (!buy.isEmpty()) {
            int index = buy.size() - 1;
            ItemStack stack = buy.get(index);
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
            buy.remove(index);
            markDirty();
        }
        return null;
    }

    public ItemStack getRandomSell(int attempts, ServerWorld world, Random random, Vec3d posToAvoid) {
        List<UUID> players = new ArrayList<>(availableForSell.keySet());
        if (players.isEmpty()) return null;
        int maxAttempts = attempts;
        for (int i = 0; i < maxAttempts; i++) {
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
                compass = sell.get(0);
            } else {
                sell = new ArrayList<>(sell);
                Compass last = sell.get(sell.size() - 1);
                for (int j = 0; j < sell.size() / 4; j++) {
                    sell.add(last);
                }
                compass = sell.get(random.nextBetweenExclusive(0, sell.size()));
            }
            if (i < maxAttempts - 2 && posToAvoid.distanceTo(compass.lodestonePos.toCenterPos()) < 128) {
                if (i < attempts) maxAttempts++;
                continue;
            }
            if (!compass.isValid(world.getServer())) {
                invalidateCompass(uuid, compass);
                continue;
            }
            if (Items.COMPASS instanceof CompassItem compassItem) {
                ItemStack stack = new ItemStack(Items.COMPASS);
                NbtCompound nbt = stack.getOrCreateNbt();
                compassItem.writeNbt(compass.dimension, compass.lodestonePos, nbt);
                if (compass.name.isPresent()) {
                    Text name = compass.name.get();
                    if (usedNames.getOrDefault(name, 0) > 1) {
                        Optional<GameProfile> profile = Optional.ofNullable(world.getServer().getUserCache()).flatMap(cache -> cache.getByUuid(uuid));
                        if (profile.isPresent()) {
                            name = Text.literal(name.asTruncatedString(10) + " (" + profile.get().getName() + ")");
                        }
                    }
                    stack.setCustomName(name);
                }
                return stack;
            }
        }
        return null;
    }

    private void invalidateCompass(UUID uuid, Compass compass) {
        List<Compass> sell = availableForSell.getOrDefault(uuid, Collections.emptyList());
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
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList buyList = new NbtList();
        for (Map.Entry<UUID, List<ItemStack>> entry : this.availableForBuy.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            NbtCompound playerEntry = new NbtCompound();
            playerEntry.putUuid("uuid", entry.getKey());
            NbtList stackList = new NbtList();
            for (ItemStack stack : entry.getValue()) {
                stackList.add(stack.getNbt());
            }
            playerEntry.put("compasses", stackList);
            buyList.add(playerEntry);
        }
        nbt.put("buy", buyList);

        NbtList sellList = new NbtList();
        for (Map.Entry<UUID, List<Compass>> entry : this.availableForSell.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            NbtCompound playerEntry = new NbtCompound();
            playerEntry.putUuid("uuid", entry.getKey());
            NbtList stackList = new NbtList();
            for (Compass compass : entry.getValue()) {
                NbtCompound compassNbt = compass.toNbt();
                if (compassNbt != null) {
                    stackList.add(compassNbt);
                }
            }
            playerEntry.put("compasses", stackList);
            sellList.add(playerEntry);
        }
        nbt.put("sell", sellList);

        NbtList nameList = new NbtList();
        for (Map.Entry<Text, Integer> entry : this.usedNames.entrySet()) {
            if (entry.getValue() == 0) continue;
            NbtCompound nameEntry = new NbtCompound();
            nameEntry.putString("name", Text.Serializer.toJson(entry.getKey()));
            nameEntry.putInt("count", entry.getValue());
            nameList.add(nameEntry);
        }
        nbt.put("names", nameList);

        return nbt;
    }

    public TradedCompasses(NbtCompound nbt) {
        for (NbtElement playerElement : nbt.getList("buy", NbtElement.COMPOUND_TYPE)) {
            NbtCompound playerEntry = (NbtCompound) playerElement;
            UUID player = playerEntry.getUuid("uuid");
            LinkedList<ItemStack> compasses = new LinkedList<>();
            for (NbtElement compassEntry : playerEntry.getList("compasses", NbtElement.COMPOUND_TYPE)) {
                ItemStack stack = new ItemStack(Items.COMPASS);
                stack.setNbt((NbtCompound) compassEntry);
                compasses.add(stack);
            }
            availableForBuy.put(player, compasses);
        }

        for (NbtElement playerElement : nbt.getList("sell", NbtElement.COMPOUND_TYPE)) {
            NbtCompound playerEntry = (NbtCompound) playerElement;
            UUID player = playerEntry.getUuid("uuid");
            LinkedList<Compass> compasses = new LinkedList<>();
            for (NbtElement compassEntry : playerEntry.getList("compasses", NbtElement.COMPOUND_TYPE)) {
                Compass compass = Compass.fromNbt((NbtCompound) compassEntry);
                if (compass != null) compasses.add(compass);
            }
            availableForSell.put(player, compasses);
        }

        for (NbtElement nameElement : nbt.getList("names", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nameEntry = (NbtCompound) nameElement;
            Text name = Text.Serializer.fromJson(nameEntry.getString("name"));
            if (name != null) usedNames.put(name, nameEntry.getInt("count"));
        }
    }

    public static Optional<Compass> getCompass(ItemStack stack) {
        if (stack.isOf(Items.COMPASS) && CompassItem.hasLodestone(stack)) {
            NbtCompound compassNbt = stack.getOrCreateNbt();
            if (!compassNbt.contains("LodestonePos") || !compassNbt.contains("LodestoneTracked") || !compassNbt.getBoolean("LodestoneTracked")) {
                return Optional.empty();
            }

            Optional<RegistryKey<World>> lodestoneDimension = CompassItem.getLodestoneDimension(compassNbt);
            if (lodestoneDimension.isPresent()) {
                BlockPos lodestonePos = NbtHelper.toBlockPos(compassNbt.getCompound("LodestonePos"));
                return Optional.of(new Compass(stack.hasCustomName() ? Optional.of(stack.getName()) : Optional.empty(), lodestonePos, lodestoneDimension.get()));
            }
        }
        return Optional.empty();
    }


    public record Compass(Optional<Text> name, BlockPos lodestonePos, RegistryKey<World> dimension) {
        public boolean isValid(MinecraftServer server) {
            ServerWorld targetWorld = server.getWorld(dimension);
            if (targetWorld == null) return false;
            return targetWorld.isInBuildLimit(lodestonePos) && targetWorld.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, lodestonePos);
        }

        public boolean isClone(Compass other) {
            return lodestonePos.equals(other.lodestonePos) && dimension.equals(other.dimension);
        }

        public static Compass fromNbt(NbtCompound nbt) {
            Optional<RegistryKey<World>> dimension = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("dimension")).result();
            if (dimension.isPresent()) {
                Optional<Text> text = Optional.empty();
                if (nbt.contains("name")) {
                    text = Optional.ofNullable(Text.Serializer.fromJson(nbt.getString("name")));
                }
                return new Compass(text, NbtHelper.toBlockPos(nbt.getCompound("pos")), dimension.get());
            }
            return null;
        }

        public NbtCompound toNbt() {
            DataResult<NbtElement> encodedDimension = World.CODEC.encodeStart(NbtOps.INSTANCE, dimension);
            Optional<NbtElement> dimension = encodedDimension.resultOrPartial(s -> {});
            if (dimension.isPresent()) {
                NbtCompound nbt = new NbtCompound();
                name.ifPresent(text -> nbt.putString("name", Text.Serializer.toJson(text)));
                nbt.put("pos", NbtHelper.fromBlockPos(lodestonePos));
                nbt.put("dimension", dimension.get());
                return nbt;
            }
            return null;
        }
    }
}
