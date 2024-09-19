package io.github.orlouge.unruffled.mixin.sleeping;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import io.github.orlouge.unruffled.interfaces.HasBackupSpawnPoint;
import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements HasBackupSpawnPoint {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow @Nullable private BlockPos spawnPointPosition = null;
    @Shadow private RegistryKey<World> spawnPointDimension = null;
    @Shadow private float spawnAngle = 0f;
    @Shadow private boolean spawnForced = false;

    @Shadow public abstract void updateInput(float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking);

    @Inject(method = "wakeUp", at = @At("HEAD"))
    public void updatePeacefulChunksOnWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
        if (skipSleepTimer || updateSleepingPlayers || this.spawnPointPosition == null || !this.isSleeping()) return;
        PeacefulChunks.get(this.getServerWorld().getPersistentStateManager()).add(this.getUuid(), new ChunkPos(this.spawnPointPosition), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"))
    public void updatePeacefulChunksOnSetSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (this.spawnPointPosition == null || (this.spawnPointPosition.equals(pos) && this.spawnPointDimension.equals(dimension))) return;
        PeacefulChunks peacefulChunks = PeacefulChunks.get(this.getServerWorld().getPersistentStateManager());
        ChunkPos centerPos = peacefulChunks.getCenterPos(this.getUuid());
        if (sendMessage && centerPos != null && centerPos.equals(new ChunkPos(this.spawnPointPosition))) {
            this.setBackupSpawnPoint(this.spawnPointPosition);
            this.setBackupSpawnDimension(this.spawnPointDimension);
            this.setBackupSpawnAngle(this.spawnAngle);
            this.setBackupSpawnForced(this.spawnForced);
        }
        peacefulChunks.remove(this.getUuid(), new ChunkPos(this.spawnPointPosition), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readBackupSpawnPoint(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("BackupSpawnX", 99) && nbt.contains("BackupSpawnY", 99) && nbt.contains("BackupSpawnZ", 99)) {
            this.setBackupSpawnPoint(new BlockPos(nbt.getInt("BackupSpawnX"), nbt.getInt("BackupSpawnY"), nbt.getInt("BackupSpawnZ")));
            this.setBackupSpawnForced(nbt.getBoolean("BackupSpawnForced"));
            this.setBackupSpawnAngle(nbt.getFloat("BackupSpawnAngle"));
            if (nbt.contains("BackupSpawnDimension")) {
                DataResult<RegistryKey<World>> decoded = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("BackupSpawnDimension"));
                this.setBackupSpawnDimension(decoded.resultOrPartial((err) -> {}).orElse(World.OVERWORLD));
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeBackupSpawnPoint(NbtCompound nbt, CallbackInfo ci) {
        if (this.getBackupSpawnPoint() != null) {
            nbt.putInt("BackupSpawnX", this.getBackupSpawnPoint().getX());
            nbt.putInt("BackupSpawnY", this.getBackupSpawnPoint().getY());
            nbt.putInt("BackupSpawnZ", this.getBackupSpawnPoint().getZ());
            nbt.putBoolean("BackupSpawnForced", this.getBackupSpawnForced());
            nbt.putFloat("BackupSpawnAngle", this.getBackupSpawnAngle());
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.getBackupSpawnDimension().getValue()).resultOrPartial((err) -> {}).ifPresent((encoded) -> {
                nbt.put("BackupSpawnDimension", encoded);
            });
        }
    }

    private BlockPos unruffled_backupSpawnPoint;
    private RegistryKey<World> unruffled_backupSpawnDimension;
    private float unruffled_backupSpawnAngle;
    private boolean unruffled_backupSpawnForced;

    public BlockPos getBackupSpawnPoint() {
        return unruffled_backupSpawnPoint;
    }

    public void setBackupSpawnPoint(BlockPos backupSpawnPoint) {
        this.unruffled_backupSpawnPoint = backupSpawnPoint;
    }

    @Override
    public RegistryKey<World> getBackupSpawnDimension() {
        return unruffled_backupSpawnDimension;
    }

    @Override
    public void setBackupSpawnDimension(RegistryKey<World> backupSpawnDimension) {
        this.unruffled_backupSpawnDimension = backupSpawnDimension;
    }

    @Override
    public float getBackupSpawnAngle() {
        return unruffled_backupSpawnAngle;
    }

    @Override
    public void setBackupSpawnAngle(float backupSpawnAngle) {
        this.unruffled_backupSpawnAngle = backupSpawnAngle;
    }

    @Override
    public boolean getBackupSpawnForced() {
        return unruffled_backupSpawnForced;
    }

    @Override
    public void setBackupSpawnForced(boolean backupSpawnForced) {
        this.unruffled_backupSpawnForced = backupSpawnForced;
    }
}
