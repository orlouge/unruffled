package io.github.orlouge.unruffled.interfaces;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public interface HasBackupSpawnPoints {
    SpawnPoint getTopBackupSpawnPoint();
    void deleteBackupSpawnPoint(SpawnPoint pos);
    void addBackupSpawnPoint(SpawnPoint pos);
    Collection<SpawnPoint> getBackupSpawnPoints();
    void setBackupSpawnPoints(Collection<SpawnPoint> spawnPoints);

    record SpawnPoint(BlockPos pos, RegistryKey<World> dimension, float yaw, float pitch, boolean forced) {
        public static Codec<SpawnPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("Position").forGetter(SpawnPoint::pos),
            RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("Dimension").forGetter(SpawnPoint::dimension),
            Codec.FLOAT.fieldOf("yaw").forGetter(SpawnPoint::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(SpawnPoint::pitch),
            Codec.BOOL.fieldOf("forced").forGetter(SpawnPoint::forced)
        ).apply(instance, SpawnPoint::new));

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
