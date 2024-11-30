package io.github.orlouge.unruffled.interfaces;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public interface HasBackupSpawnPoints {
    SpawnPoint getTopBackupSpawnPoint();
    void deleteBackupSpawnPoint(SpawnPoint pos);
    void addBackupSpawnPoint(SpawnPoint pos);
    Collection<SpawnPoint> getBackupSpawnPoints();
    void setBackupSpawnPoints(Collection<SpawnPoint> spawnPoints);

    record SpawnPoint(BlockPos pos, RegistryKey<World> dimension, float angle, boolean forced) {
        public static SpawnPoint fromNbt(NbtCompound nbt) {
            BlockPos pos = new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
            boolean forced = nbt.getBoolean("Forced");
            float angle = nbt.getFloat("Angle");
            DataResult<RegistryKey<World>> decoded = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("Dimension"));
            RegistryKey<World> dimension = decoded.resultOrPartial((err) -> {}).orElse(World.OVERWORLD);
            return new SpawnPoint(pos, dimension, angle, forced);
        }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("X", pos.getX());
            nbt.putInt("Y", pos.getY());
            nbt.putInt("Z", pos.getZ());
            nbt.putBoolean("Forced", forced);
            nbt.putFloat("Angle", angle);
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, dimension.getValue()).resultOrPartial((err) -> {}).ifPresent((encoded) -> {
                nbt.put("Dimension", encoded);
            });
            return nbt;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SpawnPoint other && other.pos.equals(pos) && other.dimension.equals(dimension);
        }

        @Override
        public int hashCode() {
            return 31 * pos.hashCode() + dimension.hashCode();
        }
    }
}
