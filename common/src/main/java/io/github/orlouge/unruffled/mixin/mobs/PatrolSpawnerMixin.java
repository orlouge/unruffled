package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.spawner.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {
    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isNearOccupiedPointOfInterest(Lnet/minecraft/util/math/BlockPos;I)Z"))
    public boolean noPatrolsOnPeacefulChunks(ServerWorld world, BlockPos pos, int maxDistance) {
        if (PeacefulChunks.get(world.getPersistentStateManager()).isPeaceful(new ChunkPos(pos))) return true;
        return world.isNearOccupiedPointOfInterest(pos, maxDistance);
    }
}
