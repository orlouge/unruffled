package io.github.orlouge.unruffled.utils;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.*;

public class PeacefulChunks extends PersistentState {
    public static final int PEACEFUL_RANGE = 7;
    private final Map<ChunkPos, Set<UUID>> chunkPlayerMap = new HashMap<>();
    private final Map<UUID, ChunkPos> playerCenterMap = new HashMap<>();

    public PeacefulChunks() {}

    public PeacefulChunks(NbtCompound nbt) {
        for (NbtElement chunkElement : nbt.getList("chunks", NbtElement.COMPOUND_TYPE)) {
            NbtCompound chunkEntry = (NbtCompound) chunkElement;
            ChunkPos pos = new ChunkPos(chunkEntry.getInt("x"), chunkEntry.getInt("z"));
            Set<UUID> uuids = new HashSet<>();
            for (NbtElement uuidElement : chunkEntry.getList("uuids", NbtElement.INT_ARRAY_TYPE)) {
                uuids.add(NbtHelper.toUuid(uuidElement));
            }
            chunkPlayerMap.put(pos, uuids);
        }

        for (NbtElement centerElement : nbt.getList("centers", NbtElement.COMPOUND_TYPE)) {
            NbtCompound centerEntry = (NbtCompound) centerElement;
            ChunkPos pos = new ChunkPos(centerEntry.getInt("x"), centerEntry.getInt("z"));
            playerCenterMap.put(centerEntry.getUuid("uuid"), pos);
        }
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
        return Config.INSTANCE.get().mechanicsConfig.peacefulChunks() && !this.chunkPlayerMap.getOrDefault(pos, Collections.emptySet()).isEmpty();
    }

    public Set<UUID> peacefulChunkBedOwners(ChunkPos pos) {
        return Config.INSTANCE.get().mechanicsConfig.peacefulChunks() ? this.chunkPlayerMap.getOrDefault(pos, Collections.emptySet()) : Collections.emptySet();
    }

    public static PeacefulChunks get(PersistentStateManager persistentStateManager) {
        return persistentStateManager.getOrCreate(PeacefulChunks::new, PeacefulChunks::new, UnruffledMod.MOD_ID + "_peaceful_chunks");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList chunkList = new NbtList();
        for (Map.Entry<ChunkPos, Set<UUID>> entry : this.chunkPlayerMap.entrySet()) {
            if (entry.getValue().size() == 0) continue;
            NbtCompound chunkEntry = new NbtCompound();
            chunkEntry.putInt("x", entry.getKey().x);
            chunkEntry.putInt("z", entry.getKey().z);
            NbtList uuidList = new NbtList();
            for (UUID uuid : entry.getValue()) {
                uuidList.add(NbtHelper.fromUuid(uuid));
            }
            chunkEntry.put("uuids", uuidList);
            chunkList.add(chunkEntry);
        }
        nbt.put("chunks", chunkList);

        NbtList centerList = new NbtList();
        for (Map.Entry<UUID, ChunkPos> entry : this.playerCenterMap.entrySet()) {
            NbtCompound centerEntry = new NbtCompound();
            centerEntry.putUuid("uuid", entry.getKey());
            centerEntry.putInt("x", entry.getValue().x);
            centerEntry.putInt("z", entry.getValue().z);
            centerList.add(centerEntry);
        }
        nbt.put("centers", centerList);
        return nbt;
    }
}
