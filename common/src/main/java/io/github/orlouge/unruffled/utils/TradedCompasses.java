package io.github.orlouge.unruffled.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.HasAttachedLodestone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
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
    public final Map<UUID, LinkedHashMap<GlobalPos, ItemStack>> availableForBuy = new HashMap<>();
    public final Map<UUID, List<Compass>> availableForSell = new HashMap<>();
    public final Map<Text, Integer> usedNames = new HashMap<>();
    public final Map<Text, Integer> usedLores = new HashMap<>();
    private final Map<GlobalPos, Lodestone> lodestoneStatus = new HashMap<>();

    public static final int MAX_BUY_PER_PLAYER = 10;
    public static final int MAX_SELL_PER_PLAYER = 30;

    public TradedCompasses() {}

    public static TradedCompasses get(PersistentStateManager persistentStateManager) {
        return persistentStateManager.getOrCreate(TradedCompasses::new, TradedCompasses::new, UnruffledMod.MOD_ID + "_traded_compasses");
    }

    public void addBuy(PlayerEntity player, ItemStack compass) {
        compass = compass.copy();
        compass.setCount(1);
        LinkedHashMap<GlobalPos, ItemStack> buy = availableForBuy.computeIfAbsent(player.getUuid(), k -> new LinkedHashMap<>());
        Optional<Compass> compassData = getCompass(compass);
        if (compassData.isEmpty()) return;
        GlobalPos pos = GlobalPos.create(compassData.get().dimension, compassData.get().lodestonePos());
        Lodestone status = lodestoneStatus.get(pos);
        if (status != null && status.sellTrades > 0 && !buy.containsKey(pos)) return;
        buy.remove(pos);
        while (buy.size() >= MAX_BUY_PER_PLAYER) {
            buy.remove(buy.keySet().iterator().next());
        }
        buy.put(pos, compass);
        markDirty();
    }

    public void addSell(ItemStack compassStack, PlayerEntity customer, List<? extends PlayerEntity> possibleSellers) {
        if (customer == null) {
            return;
        }
        Optional<Compass> soldCompass = getCompass(compassStack);
        if (soldCompass.isPresent()) {
            GlobalPos pos = GlobalPos.create(soldCompass.get().dimension, soldCompass.get().lodestonePos);
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
                Compass removedCompass = sell.remove(0);
                GlobalPos removedCompassPos = GlobalPos.create(removedCompass.dimension, removedCompass.lodestonePos);
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
        LinkedHashMap<GlobalPos, ItemStack> buyMap = availableForBuy.getOrDefault(player.getUuid(), new LinkedHashMap<>());
        List<Map.Entry<GlobalPos, ItemStack>> buy = buyMap.entrySet().stream().toList();
        for (int attempts = 0; !buy.isEmpty() && attempts < buy.size(); attempts++) {
            Map.Entry<GlobalPos, ItemStack> entry = buy.get(buy.size() - 1 - attempts);
            ItemStack stack = entry.getValue();
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
            buyMap.remove(entry.getKey());
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
            if (i < maxAttempts - 2 && posToAvoid.distanceTo(compass.lodestonePos.toCenterPos()) * 0.003 + random.nextFloat() < 1) {
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
                GlobalPos pos = GlobalPos.create(compass.dimension, compass.lodestonePos);
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
                if (lore != null) {
                    NbtCompound displayNbt = new NbtCompound();
                    NbtList loreNbt = new NbtList();
                    lore.forEach(t -> loreNbt.add(NbtString.of(Text.Serializer.toJson(t))));
                    displayNbt.put("Lore", loreNbt);
                    stack.setSubNbt("display", displayNbt);
                }
                if (name != null) stack.setCustomName(name);
                return stack;
            }
        }
        return null;
    }

    private void invalidateCompass(UUID uuid, Compass compass) {
        List<Compass> sell = availableForSell.getOrDefault(uuid, Collections.emptyList());
        GlobalPos pos = GlobalPos.create(compass.dimension, compass.lodestonePos);
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
        markDirty();
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
            DisplayEntity.TextDisplayEntity display = EntityType.TEXT_DISPLAY.create(world);
            if (display instanceof HasAttachedLodestone attachedLodestone) {
                attachedLodestone.setAttachedLodestone(blockPos.getPos(), name);
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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList buyList = new NbtList();
        for (Map.Entry<UUID, LinkedHashMap<GlobalPos, ItemStack>> entry : this.availableForBuy.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            NbtCompound playerEntry = new NbtCompound();
            playerEntry.putUuid("uuid", entry.getKey());
            NbtList stackList = new NbtList();
            for (Map.Entry<GlobalPos, ItemStack> compassEntry : entry.getValue().entrySet()) {
                stackList.add(compassEntry.getValue().getNbt());
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

        NbtList loreList = new NbtList();
        for (Map.Entry<Text, Integer> entry : this.usedLores.entrySet()) {
            if (entry.getValue() == 0) continue;
            NbtCompound loreEntry = new NbtCompound();
            loreEntry.putString("lore", Text.Serializer.toJson(entry.getKey()));
            loreEntry.putInt("count", entry.getValue());
            loreList.add(loreEntry);
        }
        nbt.put("lores", loreList);

        NbtList lodestoneNameList = new NbtList();
        for (Map.Entry<GlobalPos, Lodestone> entry : this.lodestoneStatus.entrySet()) {
            if (entry.getValue() == null) continue;
            NbtCompound lodestoneEntry = new NbtCompound();
            lodestoneEntry.put("pos", NbtHelper.fromBlockPos(entry.getKey().getPos()));
            lodestoneEntry.put("dimension", World.CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey().getDimension()).result().get());
            lodestoneEntry.put("status", entry.getValue().toNbt());
            lodestoneNameList.add(lodestoneEntry);
        }
        nbt.put("lodestones", lodestoneNameList);

        return nbt;
    }

    public TradedCompasses(NbtCompound nbt) {
        for (NbtElement playerElement : nbt.getList("buy", NbtElement.COMPOUND_TYPE)) {
            NbtCompound playerEntry = (NbtCompound) playerElement;
            UUID player = playerEntry.getUuid("uuid");
            LinkedHashMap<GlobalPos, ItemStack> compasses = new LinkedHashMap<>();
            for (NbtElement compassEntry : playerEntry.getList("compasses", NbtElement.COMPOUND_TYPE)) {
                ItemStack stack = new ItemStack(Items.COMPASS);
                stack.setNbt((NbtCompound) compassEntry);
                Optional<Compass> compassData = getCompass(stack);
                if (compassData.isEmpty()) continue;
                GlobalPos pos = GlobalPos.create(compassData.get().dimension, compassData.get().lodestonePos);
                compasses.put(pos, stack);
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

        for (NbtElement loreElement : nbt.getList("lores", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nameEntry = (NbtCompound) loreElement;
            Text name = Text.Serializer.fromJson(nameEntry.getString("lore"));
            if (name != null) usedLores.put(name, nameEntry.getInt("count"));
        }

        if (nbt.contains("lodestones", NbtElement.LIST_TYPE)) {
            for (NbtElement lodestoneElement : nbt.getList("lodestones", NbtElement.COMPOUND_TYPE)) {
                NbtCompound lodestoneEntry = (NbtCompound) lodestoneElement;
                Optional<RegistryKey<World>> dimension = World.CODEC.parse(NbtOps.INSTANCE, lodestoneEntry.get("dimension")).result();
                dimension.ifPresent(worldRegistryKey -> Optional.ofNullable(NbtHelper.toBlockPos(lodestoneEntry.getCompound("pos"))).ifPresent(pos ->
                    lodestoneStatus.put(GlobalPos.create(worldRegistryKey, pos), Lodestone.fromNbt(lodestoneEntry.getCompound("status")))
                ));
            }
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
                return Optional.of(new Compass(
                    stack.hasCustomName() ? Optional.of(stack.getName()) : Optional.empty(),
                    Optional.ofNullable(stack.getNbt())
                        .filter(nbt -> nbt.contains("display", NbtElement.COMPOUND_TYPE))
                        .map(nbt -> nbt.getCompound("display"))
                        .filter(nbt -> nbt.contains("Lore", NbtElement.LIST_TYPE))
                        .flatMap(nbt -> nbt.getList("Lore", NbtElement.STRING_TYPE).stream().findFirst())
                        .map(l -> Text.Serializer.fromJson(l.asString())),
                    lodestonePos, lodestoneDimension.get()));
            }
        }
        return Optional.empty();
    }


    public record Lodestone(Optional<Text> name, int sellTrades, Optional<UUID> displayEntityUUID) {
        public static Lodestone fromNbt(NbtCompound nbt) {
            Optional<Text> name = Optional.empty();
            if (nbt.contains("name")) {
                name = Optional.ofNullable(Text.Serializer.fromJson(nbt.getString("name")));
            }
            int hasSellTrade = nbt.getShort("trades");
            Optional<UUID> displayEntity = nbt.contains("display_entity") ? Optional.of(nbt.getUuid("display_entity")) : Optional.empty();
            return new Lodestone(name, hasSellTrade, displayEntity);
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            name.ifPresent(text -> nbt.putString("name", Text.Serializer.toJson(text)));
            nbt.putShort("trades", (short) sellTrades);
            displayEntityUUID.ifPresent(uuid -> nbt.putUuid("display_entity", uuid));
            return nbt;
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

        public static Compass fromNbt(NbtCompound nbt) {
            Optional<RegistryKey<World>> dimension = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("dimension")).result();
            if (dimension.isPresent()) {
                Optional<Text> text = Optional.empty(), lore = Optional.empty();
                if (nbt.contains("name")) {
                    text = Optional.ofNullable(Text.Serializer.fromJson(nbt.getString("name")));
                }
                if (nbt.contains("lore")) {
                    lore = Optional.ofNullable(Text.Serializer.fromJson(nbt.getString("lore")));
                }
                return new Compass(text, lore, NbtHelper.toBlockPos(nbt.getCompound("pos")), dimension.get());
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
