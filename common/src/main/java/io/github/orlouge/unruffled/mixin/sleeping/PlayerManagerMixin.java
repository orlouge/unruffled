package io.github.orlouge.unruffled.mixin.sleeping;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.HasBackupSpawnPoints;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;
    private Pair<HasBackupSpawnPoints.SpawnPoint, Optional<Vec3d>> foundSpawnPoint = null;

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removePlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity$RemovalReason;)V", shift = At.Shift.AFTER))
    public void findBackupSpawnIfNeeded(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        this.foundSpawnPoint = null;
        if (!(player instanceof HasBackupSpawnPoints backupSpawnPoints)) return;
        HasBackupSpawnPoints.SpawnPoint originalPoint = new HasBackupSpawnPoints.SpawnPoint(player.getSpawnPointPosition(), player.getSpawnPointDimension(), player.getSpawnAngle(), player.isSpawnForced());
        HasBackupSpawnPoints.SpawnPoint point = originalPoint;
        while (point != null) {
            ServerWorld backupWorld = this.server.getWorld(point.dimension());
            if (point.pos() != null && point.dimension() != null && backupWorld != null) {
                Optional<Vec3d> respawnPos = PlayerEntity.findRespawnPosition(backupWorld, point.pos(), point.angle(), point.forced(), alive);
                if (respawnPos.isPresent()) {
                    this.foundSpawnPoint = new Pair<>(point, respawnPos);
                    return;
                }
            }
            backupSpawnPoints.deleteBackupSpawnPoint(point);
            point = backupSpawnPoints.getTopBackupSpawnPoint();
        }
        this.foundSpawnPoint = new Pair<>(originalPoint, Optional.empty());
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;"))
    public BlockPos replaceSpawnPointPos(ServerPlayerEntity instance) {
        return foundSpawnPoint != null ? foundSpawnPoint.getLeft().pos() : instance.getSpawnPointPosition();
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointDimension()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> replaceSpawnPointDimension(ServerPlayerEntity instance) {
        return foundSpawnPoint != null ? foundSpawnPoint.getLeft().dimension() : instance.getSpawnPointDimension();
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnAngle()F"))
    public float replaceSpawnAngle(ServerPlayerEntity instance) {
        return foundSpawnPoint != null ? foundSpawnPoint.getLeft().angle() : instance.getSpawnAngle();
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpawnForced()Z"))
    public boolean replaceSpawnForced(ServerPlayerEntity instance) {
        return foundSpawnPoint != null ? foundSpawnPoint.getLeft().forced() : instance.isSpawnForced();
    }

    @Inject(method = "respawnPlayer", at = @At(value = "RETURN"))
    public void clearSpawnPoint(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        this.foundSpawnPoint = null;
        UnruffledMod.sendLockedDeathPosition(player);
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
    public Optional<Vec3d> findSpawnOrBackupSpawn(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, ServerPlayerEntity player, boolean alive2) {
        return this.foundSpawnPoint != null ? this.foundSpawnPoint.getRight() : PlayerEntity.findRespawnPosition(world, pos, angle, forced, alive);
    }

    @Inject(method = "sendPlayerStatus", at = @At("TAIL"))
    public void updateLockedDeathPosOnPlayerStatus(ServerPlayerEntity player, CallbackInfo ci) {
        UnruffledMod.sendLockedDeathPosition(player);
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getResourcePackProperties()Ljava/util/Optional;"))
    public void updateLockedDeathPosOnConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        UnruffledMod.sendLockedDeathPosition(player);
    }
}
