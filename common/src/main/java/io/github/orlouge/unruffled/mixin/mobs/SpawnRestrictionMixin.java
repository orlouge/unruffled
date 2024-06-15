package io.github.orlouge.unruffled.mixin.mobs;

import net.minecraft.entity.SpawnRestriction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {
    /*
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void dontSpawnInPeacefulChunks(EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (spawnReason == SpawnReason.NATURAL && world.getDimension().bedWorks() && pos.getY() > 62 && !type.getSpawnGroup().isPeaceful() && PeacefulChunks.get(world.toServerWorld().getPersistentStateManager()).isPeaceful(new ChunkPos(pos))) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
     */
}
