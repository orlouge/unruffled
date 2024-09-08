package io.github.orlouge.unruffled.mixin.sleeping;

import io.github.orlouge.unruffled.utils.PeacefulChunks;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @Inject(method = "onBreak", at = @At("HEAD"))
    public void resetPeacefulChunksOnBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
            BedPart bedPart = state.get(BedBlock.PART);
            if (bedPart == BedPart.FOOT) {
                pos = pos.offset(state.get(BedBlock.FACING));
            }
            if (serverPlayer.getSpawnPointPosition() != null && serverPlayer.getSpawnPointPosition().equals(pos)) {
                PeacefulChunks.get(serverWorld.getPersistentStateManager()).remove(serverPlayer.getUuid(), new ChunkPos(pos), PeacefulChunks.PEACEFUL_RANGE);
                if (serverPlayer.getSpawnPointDimension().equals(world.getRegistryKey())) {
                    serverPlayer.setSpawnPoint(null, null, 0, false, false);
                }
            }
        }
    }
}
