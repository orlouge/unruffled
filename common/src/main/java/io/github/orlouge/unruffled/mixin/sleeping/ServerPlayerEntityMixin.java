package io.github.orlouge.unruffled.mixin.sleeping;

import com.mojang.authlib.GameProfile;
import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.entity.player.PlayerEntity;
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

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow @Nullable private BlockPos spawnPointPosition;

    @Inject(method = "wakeUp", at = @At("HEAD"))
    public void updatePeacefulChunksOnWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
        if (skipSleepTimer || updateSleepingPlayers || this.spawnPointPosition == null || !this.isSleeping()) return;
        PeacefulChunks.get(this.getServerWorld().getPersistentStateManager()).add(this.getUuid(), new ChunkPos(this.spawnPointPosition), PeacefulChunks.PEACEFUL_RANGE);
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"))
    public void updatePeacefulChunksOnSetSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (this.spawnPointPosition == null || this.spawnPointPosition.equals(pos)) return;
        PeacefulChunks.get(this.getServerWorld().getPersistentStateManager()).remove(this.getUuid(), new ChunkPos(this.spawnPointPosition), PeacefulChunks.PEACEFUL_RANGE);
    }
}
