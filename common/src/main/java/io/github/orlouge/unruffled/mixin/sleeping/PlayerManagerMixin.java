package io.github.orlouge.unruffled.mixin.sleeping;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.HasBackupSpawnPoints;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
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
    public void findBackupSpawnIfNeeded(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        this.foundSpawnPoint = null;
        if (!(player instanceof HasBackupSpawnPoints backupSpawnPoints)) return;
        HasBackupSpawnPoints.SpawnPoint originalPoint = new HasBackupSpawnPoints.SpawnPoint(player.getSpawnPointPosition(), player.getSpawnPointDimension(), player.getSpawnAngle(), player.isSpawnForced());
        HasBackupSpawnPoints.SpawnPoint point = originalPoint;
        while (point != null) {
            ServerWorld backupWorld = this.server.getWorld(point.dimension());
            if (point.pos() != null && point.dimension() != null && backupWorld != null) {
                Optional<ServerPlayerEntity.RespawnPos> respawnPos = ServerPlayerEntity.findRespawnPosition(backupWorld, point.pos(), point.angle(), point.forced(), alive);
                if (respawnPos.isPresent()) {
                    this.foundSpawnPoint = new Pair<>(point, Optional.of(respawnPos.get().pos));
                    return;
                }
            }
            backupSpawnPoints.deleteBackupSpawnPoint(point);
            point = backupSpawnPoints.getTopBackupSpawnPoint();
        }
        this.foundSpawnPoint = originalPoint.pos() == null ? null : new Pair<>(originalPoint, Optional.empty());
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"))
    public TeleportTarget replaceTeleportPos(ServerPlayerEntity instance, boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        if (foundSpawnPoint == null) return instance.getRespawnTarget(alive, postDimensionTransition);
        return new TeleportTarget(instance.server.getWorld(foundSpawnPoint.getLeft().dimension()), foundSpawnPoint.getRight().orElse(Vec3d.ofCenter(foundSpawnPoint.getLeft().pos())), Vec3d.ZERO, foundSpawnPoint.getLeft().angle(), 0.0F, postDimensionTransition);
    }

    @Inject(method = "respawnPlayer", at = @At(value = "RETURN"))
    public void clearSpawnPoint(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        this.foundSpawnPoint = null;
        UnruffledMod.sendLockedDeathPosition(player);
    }

    @Inject(method = "sendPlayerStatus", at = @At("TAIL"))
    public void updateLockedDeathPosOnPlayerStatus(ServerPlayerEntity player, CallbackInfo ci) {
        UnruffledMod.sendLockedDeathPosition(player);
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendStatusEffects(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    public void updateLockedDeathPosOnConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        UnruffledMod.sendLockedDeathPosition(player);
    }
}
