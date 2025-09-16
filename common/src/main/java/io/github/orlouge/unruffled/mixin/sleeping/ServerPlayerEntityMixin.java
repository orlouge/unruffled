package io.github.orlouge.unruffled.mixin.sleeping;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.HasBackupSpawnPoints;
import io.github.orlouge.unruffled.interfaces.HasLockedDeathPosition;
import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements HasBackupSpawnPoints, HasLockedDeathPosition {


    private Optional<GlobalPos> unruffled_lockedDeathPos = Optional.empty();

    @Shadow @Nullable private ServerPlayerEntity.Respawn respawn;

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow public abstract ServerWorld getWorld();

    @Inject(method = "wakeUp", at = @At("HEAD"))
    public void updatePeacefulChunksOnWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
        if (skipSleepTimer || updateSleepingPlayers || this.respawn == null || this.respawn.pos() == null || !this.isSleeping()) return;
        PeacefulChunks.get(this.getWorld().getPersistentStateManager()).add(this.getUuid(), new ChunkPos(this.respawn.pos()), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"))
    public void updatePeacefulChunksOnSetSpawnPoint(ServerPlayerEntity.Respawn respawn, boolean sendMessage, CallbackInfo ci) {
        if (this.respawn == null || this.respawn.pos() == null || (this.respawn.pos().equals(respawn == null ? null : respawn.pos()) && this.respawn.dimension().equals(respawn.dimension()))) return;
        SpawnPoint currentSpawnPos = new SpawnPoint(this.respawn.pos(), this.respawn.dimension(), this.respawn.angle(), this.respawn.forced());
        //if (pos == null) this.deleteBackupSpawnPoint(currentSpawnPos);
        PeacefulChunks peacefulChunks = PeacefulChunks.get(this.getWorld().getPersistentStateManager());
        ChunkPos centerPos = peacefulChunks.getCenterPos(this.getUuid());
        if (/* sendMessage && */ centerPos != null && centerPos.equals(new ChunkPos(this.respawn.pos()))) {
            this.addBackupSpawnPoint(currentSpawnPos);
        }
        peacefulChunks.remove(this.getUuid(), new ChunkPos(this.respawn.pos()), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void copyBackupSpawnPoints(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof HasBackupSpawnPoints backupSpawnPoints) {
            this.setBackupSpawnPoints(backupSpawnPoints.getBackupSpawnPoints());
        }
        if (oldPlayer instanceof HasLockedDeathPosition lockedDeathPosition) {
            this.unruffled_lockedDeathPos = lockedDeathPosition.getLockedDeathPosition();
        }
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    public void readBackupSpawnPoint(ReadView view, CallbackInfo ci) {
        this.unruffled_backupSpawnPoints = new LinkedList<>();
        Optional<ReadView.TypedListReadView<SpawnPoint>> spawnList = view.getOptionalTypedListView("BackupSpawn", SpawnPoint.CODEC);
        spawnList.ifPresent(spawnPoints -> unruffled_backupSpawnPoints.addAll(spawnPoints.stream().toList()));

        this.unruffled_lockedDeathPos = view.read("LockedDeathLocation", GlobalPos.CODEC);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    public void writeBackupSpawnPoint(WriteView view, CallbackInfo ci) {
        if (this.unruffled_backupSpawnPoints != null) {
            WriteView.ListAppender<SpawnPoint> spawnList = view.getListAppender("BackupSpawn", SpawnPoint.CODEC);
            for (SpawnPoint point : this.unruffled_backupSpawnPoints) {
                spawnList.add(point);
            }
        }
        this.unruffled_lockedDeathPos.ifPresent(pos -> view.put("LockedDeathLocation", GlobalPos.CODEC, pos));
    }

    private LinkedList<SpawnPoint> unruffled_backupSpawnPoints = new LinkedList<>();

    @Override
    public SpawnPoint getTopBackupSpawnPoint() {
        return !unruffled_backupSpawnPoints.isEmpty() ? unruffled_backupSpawnPoints.getFirst() : null;
    }

    @Override
    public void addBackupSpawnPoint(SpawnPoint pos) {
        if (io.github.orlouge.unruffled.config.Config.INSTANCE.get().mechanicsConfig.backupSpawnPoints() == 0) {
            unruffled_backupSpawnPoints.clear();
            return;
        }
        deleteBackupSpawnPoint(pos);
        while (unruffled_backupSpawnPoints.size() >= io.github.orlouge.unruffled.config.Config.INSTANCE.get().mechanicsConfig.backupSpawnPoints()) {
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

    @Override
    public Optional<GlobalPos> getLockedDeathPosition() {
        return this.unruffled_lockedDeathPos;
    }

    @Override
    public void setLockedDeathPosition() {
        this.unruffled_lockedDeathPos = this.getLastDeathPos();
        UnruffledMod.sendLockedDeathPosition((ServerPlayerEntity) (Object) this, this.unruffled_lockedDeathPos);
    }
}
