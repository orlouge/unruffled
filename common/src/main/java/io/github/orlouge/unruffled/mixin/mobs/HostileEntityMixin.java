package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {
    @Inject(method = "canSpawnInDark", cancellable = true, at = @At("RETURN"))
    private static void dontSpawnInPeacefulChunks(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && spawnReason == SpawnReason.NATURAL && world.getDimension().bedWorks() && pos.getY() > 61) {
            Set<UUID> players = PeacefulChunks.get(world.toServerWorld().getPersistentStateManager()).peacefulChunkBedOwners(new ChunkPos(pos));
            if (!players.isEmpty()) {
                if (world.toServerWorld().isNight()) {
                    for (UUID playerUuid : players) {
                        if (world.getPlayerByUuid(playerUuid) instanceof ServerPlayerEntity serverPlayer) {
                            if (serverPlayer.getBlockPos().getY() >= world.getSeaLevel() - 5 && serverPlayer.getBlockPos().isWithinDistance(pos, PeacefulChunks.PEACEFUL_RANGE * 12)) {
                                UnruffledMod.PEACEFUL_CHUNK_CRITERION.trigger(serverPlayer);
                            }
                        }
                    }
                }
                cir.setReturnValue(false);
            }
        }
    }
}
