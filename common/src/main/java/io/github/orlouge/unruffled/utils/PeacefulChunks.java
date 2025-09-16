package io.github.orlouge.unruffled.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.*;
import java.util.stream.Collectors;

public class PeacefulChunks extends PersistentState {
    public static final int PEACEFUL_RANGE = 7;
    private final HashMap<ChunkPos, HashSet<UUID>> chunkPlayerMap;
    private final HashMap<UUID, ChunkPos> playerCenterMap;
    public static final Codec<PeacefulChunks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.mapAsListOfPairs(ChunkPos.CODEC, Codec.list(Uuids.STRING_CODEC)).xmap(
            mapOfLists -> new HashMap<>(mapOfLists.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<UUID>(e.getValue())))),
            mapOfSets -> new HashMap<>(mapOfSets.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new ArrayList<>(e.getValue()))))
        ).fieldOf("chunks").forGetter(state -> state.chunkPlayerMap),
        Codec.unboundedMap(Uuids.STRING_CODEC, ChunkPos.CODEC).fieldOf("centers").forGetter(state -> state.playerCenterMap)
    ).apply(instance, PeacefulChunks::new));
    private static final PersistentStateType<PeacefulChunks> TYPE = new PersistentStateType<>(
        UnruffledMod.MOD_ID + "_peaceful_chunks", PeacefulChunks::new, CODEC, null
    );

    public PeacefulChunks() {
        this.chunkPlayerMap = new HashMap<>();
        this.playerCenterMap = new HashMap<>();
    }

    public PeacefulChunks(Map<ChunkPos, HashSet<UUID>> chunks, Map<UUID, ChunkPos> centers) {
        this.chunkPlayerMap = new HashMap<>(chunks);
        this.playerCenterMap = new HashMap<>(centers);
    }

    public void add(UUID uuid, ChunkPos pos, int range) {
        ChunkPos prevPos = playerCenterMap.get(uuid);
        if (prevPos != null) {
            if (prevPos.equals(pos)) return;
            this.removeFromChunkMap(uuid, pos, range);
        }
        range += 1;
        for (int x = pos.x - range; x <= pos.x + range; x++) {
            for (int z = pos.z - range; z <= pos.z + range; z++) {
                if ((x - pos.x) * (x - pos.x) + (z - pos.z) * (z - pos.z) > range * range) continue;
                this.chunkPlayerMap.computeIfAbsent(new ChunkPos(x, z), ignored -> new HashSet<>()).add(uuid);
            }
        }
        playerCenterMap.put(uuid, pos);
        this.markDirty();
    }

    public void remove(UUID uuid, ChunkPos pos, int range) {
        this.removeFromChunkMap(uuid, pos, range);
        ChunkPos prevPos = playerCenterMap.get(uuid);
        if (prevPos != null && !prevPos.equals(pos)) this.removeFromChunkMap(uuid, prevPos, range);
        playerCenterMap.remove(uuid);
        this.markDirty();
    }

    public ChunkPos getCenterPos(UUID uuid) {
        return playerCenterMap.get(uuid);
    }

    private void removeFromChunkMap(UUID uuid, ChunkPos pos, int range) {
        range += 1;
        for (int x = pos.x - range; x <= pos.x + range; x++) {
            for (int z = pos.z - range; z <= pos.z + range; z++) {
                if ((x - pos.x) * (x - pos.x) + (z - pos.z) * (z - pos.z) > range * range) continue;
                this.chunkPlayerMap.computeIfPresent(new ChunkPos(x, z), (ignored, set) -> {set.remove(uuid); return set;});
            }
        }
    }

    public boolean isPeaceful(ChunkPos pos) {
        return Config.INSTANCE.get().mechanicsConfig.peacefulChunks() && !this.chunkPlayerMap.getOrDefault(pos, new HashSet<>()).isEmpty();
    }

    public Set<UUID> peacefulChunkBedOwners(ChunkPos pos) {
        return Config.INSTANCE.get().mechanicsConfig.peacefulChunks() ? this.chunkPlayerMap.getOrDefault(pos, new HashSet<>()) : Collections.emptySet();
    }

    public static PeacefulChunks get(PersistentStateManager persistentStateManager) {
        return persistentStateManager.getOrCreate(TYPE);
    }
}
