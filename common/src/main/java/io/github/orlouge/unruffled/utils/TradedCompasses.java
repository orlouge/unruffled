package io.github.orlouge.unruffled.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
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
    public final Map<Text, Integer> usedLores = new HashMap<>();
    private final Map<BlockPos, Text> lodestoneNames = new HashMap<>();
    private static final Type<TradedCompasses> TYPE = new Type<>(
        TradedCompasses::new, TradedCompasses::new, null
    );

    public static final int MAX_BUY_PER_PLAYER = 50;
    public static final int MAX_SELL_PER_PLAYER = 30;

    public TradedCompasses() {}

    public static TradedCompasses get(PersistentStateManager persistentStateManager) {
        return persistentStateManager.getOrCreate(TYPE, UnruffledMod.MOD_ID + "_traded_compasses");
    }

    public void addBuy(PlayerEntity player, ItemStack compass) {
        compass = compass.copy();
        compass.setCount(1);
        List<ItemStack> buy = availableForBuy.computeIfAbsent(player.getUuid(), k -> new LinkedList<>());
        if (!buy.isEmpty() && buy.getLast().equals(compass)) return;
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
            compass.get().lore.ifPresent(text -> usedLores.merge(text, 1, (k, v) -> v + 1));
            markDirty();
        }
    }

    public ItemStack getBuy(ServerWorld world, PlayerEntity player) {
        List<ItemStack> buy = availableForBuy.getOrDefault(player.getUuid(), Collections.emptyList());
        for (int attempts = 0; !buy.isEmpty() && attempts < 10; attempts++) {
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
            if (Items.COMPASS instanceof CompassItem) {
                ItemStack stack = new ItemStack(Items.COMPASS);
                stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(new GlobalPos(compass.dimension, compass.lodestonePos)), true));
                Text name = null;
                List<Text> lore = null;
                boolean duplicateName = true, duplicateLore = true;
                if (compass.name.isPresent()) {
                    name = compass.name.get();
                    duplicateName = usedNames.getOrDefault(name, 0) > 1;
                }
                if (compass.lore.isPresent()) {
                    Text compassLore = compass.lore.get();
                    lore = new ArrayList<>(List.of(compassLore));
                    duplicateLore = usedLores.getOrDefault(compassLore, 0) > 1;
                }
                if (duplicateName && duplicateLore) {
                    Optional<GameProfile> profile = Optional.ofNullable(world.getServer().getUserCache()).flatMap(cache -> cache.getByUuid(uuid));
                    if (profile.isPresent()) {
                        if (lore == null) lore = new ArrayList<>();
                        lore.addAll(Text.of(profile.get().getName()).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
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

    public void storeLodestoneName(BlockPos blockPos, Text name) {
        if (name == null) return;
        lodestoneNames.put(blockPos, name);
        markDirty();
    }

    public void deleteLodestoneName(BlockPos blockPos) {
        lodestoneNames.remove(blockPos);
        markDirty();
    }

    public Text getLodestoneName(BlockPos blockPos) {
        return lodestoneNames.get(blockPos);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList buyList = new NbtList();
        for (Map.Entry<UUID, List<ItemStack>> entry : this.availableForBuy.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            NbtCompound playerEntry = new NbtCompound();
            playerEntry.putUuid("uuid", entry.getKey());
            NbtList stackList = new NbtList();
            for (ItemStack stack : entry.getValue()) {
                stackList.add(LodestoneTrackerComponent.CODEC.encodeStart(NbtOps.INSTANCE, stack.get(DataComponentTypes.LODESTONE_TRACKER)).getOrThrow());
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
                NbtCompound compassNbt = compass.toNbt(lookup);
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
            nameEntry.putString("name", Text.Serialization.toJsonString(entry.getKey(), lookup));
            nameEntry.putInt("count", entry.getValue());
            nameList.add(nameEntry);
        }
        nbt.put("names", nameList);

        NbtList loreList = new NbtList();
        for (Map.Entry<Text, Integer> entry : this.usedLores.entrySet()) {
            if (entry.getValue() == 0) continue;
            NbtCompound loreEntry = new NbtCompound();
            loreEntry.putString("lore", Text.Serialization.toJsonString(entry.getKey(), lookup));
            loreEntry.putInt("count", entry.getValue());
            loreList.add(loreEntry);
        }
        nbt.put("lores", loreList);

        NbtList lodestoneNameList = new NbtList();
        for (Map.Entry<BlockPos, Text> entry : this.lodestoneNames.entrySet()) {
            if (entry.getValue() == null) continue;
            NbtCompound lodestoneEntry = new NbtCompound();
            lodestoneEntry.put("pos", NbtHelper.fromBlockPos(entry.getKey()));
            lodestoneEntry.putString("name", Text.Serialization.toJsonString(entry.getValue(), lookup));
            lodestoneNameList.add(lodestoneEntry);
        }
        nbt.put("lodestoneNames", lodestoneNameList);

        return nbt;
    }

    public TradedCompasses(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        for (NbtElement playerElement : nbt.getList("buy", NbtElement.COMPOUND_TYPE)) {
            NbtCompound playerEntry = (NbtCompound) playerElement;
            UUID player = playerEntry.getUuid("uuid");
            LinkedList<ItemStack> compasses = new LinkedList<>();
            for (NbtElement compassEntry : playerEntry.getList("compasses", NbtElement.COMPOUND_TYPE)) {
                ItemStack stack = new ItemStack(Items.COMPASS);
                stack.set(DataComponentTypes.LODESTONE_TRACKER, LodestoneTrackerComponent.CODEC.decode(NbtOps.INSTANCE, compassEntry).getOrThrow().getFirst());
                compasses.add(stack);
            }
            availableForBuy.put(player, compasses);
        }

        for (NbtElement playerElement : nbt.getList("sell", NbtElement.COMPOUND_TYPE)) {
            NbtCompound playerEntry = (NbtCompound) playerElement;
            UUID player = playerEntry.getUuid("uuid");
            LinkedList<Compass> compasses = new LinkedList<>();
            for (NbtElement compassEntry : playerEntry.getList("compasses", NbtElement.COMPOUND_TYPE)) {
                Compass compass = Compass.fromNbt((NbtCompound) compassEntry, lookup);
                if (compass != null) compasses.add(compass);
            }
            availableForSell.put(player, compasses);
        }

        for (NbtElement nameElement : nbt.getList("names", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nameEntry = (NbtCompound) nameElement;
            Text name = Text.Serialization.fromJson(nameEntry.getString("name"), lookup);
            if (name != null) usedNames.put(name, nameEntry.getInt("count"));
        }

        for (NbtElement loreElement : nbt.getList("lores", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nameEntry = (NbtCompound) loreElement;
            Text name = Text.Serialization.fromJson(nameEntry.getString("lore"), lookup);
            if (name != null) usedLores.put(name, nameEntry.getInt("count"));
        }

        if (nbt.contains("lodestoneNames", NbtElement.LIST_TYPE)) {
            for (NbtElement lodestoneElement : nbt.getList("lodestoneNames", NbtElement.COMPOUND_TYPE)) {
                NbtCompound lodestoneEntry = (NbtCompound) lodestoneElement;
                NbtHelper.toBlockPos(lodestoneEntry, "pos").ifPresent(pos ->
                    lodestoneNames.put(pos, Text.Serialization.fromJson(lodestoneEntry.getString("name"), lookup))
                );
            }
        }
    }

    public record Compass(Optional<Text> name, Optional<Text> lore, BlockPos lodestonePos, RegistryKey<World> dimension) {
        public boolean isValid(MinecraftServer server) {
            ServerWorld targetWorld = server.getWorld(dimension);
            if (targetWorld == null) return false;
            return targetWorld.isInBuildLimit(lodestonePos) && targetWorld.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, lodestonePos);
        }

        public boolean isClone(Compass other) {
            return lodestonePos.equals(other.lodestonePos) && dimension.equals(other.dimension);
        }

        public static Compass fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            Optional<RegistryKey<World>> dimension = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("dimension")).result();
            if (dimension.isPresent()) {
                Optional<Text> name = Optional.empty();
                if (nbt.contains("name")) {
                    name = Optional.ofNullable(Text.Serialization.fromJson(nbt.getString("name"), lookup));
                }
                Optional<Text> lore = Optional.empty();
                if (nbt.contains("lore")) {
                    lore = Optional.ofNullable(Text.Serialization.fromJson(nbt.getString("lore"), lookup));
                }
                Optional<BlockPos> pos = NbtHelper.toBlockPos(nbt, "pos");
                return pos.isEmpty() ? null : new Compass(name, lore, pos.get(), dimension.get());
            }
            return null;
        }

        public NbtCompound toNbt(RegistryWrapper.WrapperLookup lookup) {
            DataResult<NbtElement> encodedDimension = World.CODEC.encodeStart(NbtOps.INSTANCE, dimension);
            Optional<NbtElement> dimension = encodedDimension.resultOrPartial(s -> {});
            if (dimension.isPresent()) {
                NbtCompound nbt = new NbtCompound();
                name.ifPresent(text -> nbt.putString("name", Text.Serialization.toJsonString(text, lookup)));
                lore.ifPresent(text -> nbt.putString("lore", Text.Serialization.toJsonString(text, lookup)));
                nbt.put("pos", NbtHelper.fromBlockPos(lodestonePos));
                nbt.put("dimension", dimension.get());
                return nbt;
            }
            return null;
        }
    }
}
