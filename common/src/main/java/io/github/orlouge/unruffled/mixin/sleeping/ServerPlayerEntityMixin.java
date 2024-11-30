package io.github.orlouge.unruffled.mixin.sleeping;

import com.mojang.authlib.GameProfile;
import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.interfaces.HasBackupSpawnPoints;
import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements HasBackupSpawnPoints {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow @Nullable private BlockPos spawnPointPosition = null;
    @Shadow private RegistryKey<World> spawnPointDimension = null;
    @Shadow private float spawnAngle = 0f;
    @Shadow private boolean spawnForced = false;

    @Shadow public abstract void updateInput(float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking);

    @Shadow public abstract void setSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage);

    @Inject(method = "wakeUp", at = @At("HEAD"))
    public void updatePeacefulChunksOnWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
        if (skipSleepTimer || updateSleepingPlayers || this.spawnPointPosition == null || !this.isSleeping()) return;
        PeacefulChunks.get(this.getServerWorld().getPersistentStateManager()).add(this.getUuid(), new ChunkPos(this.spawnPointPosition), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"))
    public void updatePeacefulChunksOnSetSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (this.spawnPointPosition == null || (this.spawnPointPosition.equals(pos) && this.spawnPointDimension.equals(dimension))) return;
        SpawnPoint currentSpawnPos = new SpawnPoint(this.spawnPointPosition, this.spawnPointDimension, this.spawnAngle, this.spawnForced);
        //if (pos == null) this.deleteBackupSpawnPoint(currentSpawnPos);
        PeacefulChunks peacefulChunks = PeacefulChunks.get(this.getServerWorld().getPersistentStateManager());
        ChunkPos centerPos = peacefulChunks.getCenterPos(this.getUuid());
        if (/* sendMessage && */ centerPos != null && centerPos.equals(new ChunkPos(this.spawnPointPosition))) {
            this.addBackupSpawnPoint(currentSpawnPos);
        }
        peacefulChunks.remove(this.getUuid(), new ChunkPos(this.spawnPointPosition), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void copyBackupSpawnPoints(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof HasBackupSpawnPoints backupSpawnPoints) {
            this.setBackupSpawnPoints(backupSpawnPoints.getBackupSpawnPoints());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readBackupSpawnPoint(NbtCompound nbt, CallbackInfo ci) {
        this.unruffled_backupSpawnPoints = new LinkedList<>();
        if (nbt.contains("BackupSpawn", NbtElement.LIST_TYPE)) {
            NbtList spawnList = nbt.getList("BackupSpawn", NbtElement.COMPOUND_TYPE);
            for (NbtElement spawnPointNbt : spawnList) {
                unruffled_backupSpawnPoints.add(SpawnPoint.fromNbt((NbtCompound) spawnPointNbt));
            }
        }
        System.out.println("Read backup spawn points: " + unruffled_backupSpawnPoints);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeBackupSpawnPoint(NbtCompound nbt, CallbackInfo ci) {
        if (this.unruffled_backupSpawnPoints != null) {
            NbtList spawnList = new NbtList();
            for (SpawnPoint point : this.unruffled_backupSpawnPoints) {
                spawnList.add(point.toNbt());
            }
            nbt.put("BackupSpawn", spawnList);
        }
    }

    private LinkedList<SpawnPoint> unruffled_backupSpawnPoints = new LinkedList<>();

    @Override
    public SpawnPoint getTopBackupSpawnPoint() {
        return !unruffled_backupSpawnPoints.isEmpty() ? unruffled_backupSpawnPoints.getFirst() : null;
    }

    @Override
    public void addBackupSpawnPoint(SpawnPoint pos) {
        deleteBackupSpawnPoint(pos);
        while (unruffled_backupSpawnPoints.size() >= Config.INSTANCE.get().mechanicsConfig.backupSpawnPoints()) {
            unruffled_backupSpawnPoints.removeLast();
        }
        unruffled_backupSpawnPoints.addFirst(pos);
    }

    @Override
    public void deleteBackupSpawnPoint(SpawnPoint pos) {
        unruffled_backupSpawnPoints.remove(pos);
    }

    @Override
    public void setBackupSpawnPoints(Collection<SpawnPoint> spawnPoints) {
        this.unruffled_backupSpawnPoints = new LinkedList<>(spawnPoints);
    }

    @Override
    public Collection<SpawnPoint> getBackupSpawnPoints() {
        return this.unruffled_backupSpawnPoints;
    }
}
