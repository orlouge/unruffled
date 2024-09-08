package io.github.orlouge.unruffled.mixin.sleeping;

import io.github.orlouge.unruffled.interfaces.HasBackupSpawnPoint;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
    public Optional<Vec3d> findSpawnOrBackupSpawn(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, ServerPlayerEntity player, boolean alive2) {
        return PlayerEntity.findRespawnPosition(world, pos, angle, forced, alive).or(() -> {
            if (player instanceof HasBackupSpawnPoint player2 && player2.getBackupSpawnPoint() != null) {
                ServerWorld backupWorld = this.server.getWorld(player2.getBackupSpawnDimension());
                System.out.println("Recovering backup spawn point " + player2.getBackupSpawnPoint());
                if (backupWorld == null) return Optional.empty();
                return PlayerEntity.findRespawnPosition(backupWorld, player2.getBackupSpawnPoint(), player2.getBackupSpawnAngle(), player2.getBackupSpawnForced(), alive);
            }
            return Optional.empty();
        });
    }
}
