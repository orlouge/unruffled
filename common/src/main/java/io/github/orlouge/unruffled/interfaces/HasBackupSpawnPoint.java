package io.github.orlouge.unruffled.interfaces;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface HasBackupSpawnPoint {
    BlockPos getBackupSpawnPoint();
    void setBackupSpawnPoint(BlockPos pos);

    RegistryKey<World> getBackupSpawnDimension();
    void setBackupSpawnDimension(RegistryKey<World> dimension);

    float getBackupSpawnAngle();
    void setBackupSpawnAngle(float angle);

    boolean getBackupSpawnForced();
    void setBackupSpawnForced(boolean forced);
}
