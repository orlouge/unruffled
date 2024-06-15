package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isSkyVisible(Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean noPhantomsOnPeacefulChunks(ServerWorld world, BlockPos pos) {
        if (PeacefulChunks.get(world.toServerWorld().getPersistentStateManager()).isPeaceful(new ChunkPos(pos))) return false;
        return world.isSkyVisible(pos);
    }
}
