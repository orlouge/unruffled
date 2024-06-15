package io.github.orlouge.unruffled.mixin.worldgen;

import net.minecraft.structure.StrongholdGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StrongholdGenerator.class)
public class StrongholdGeneratorMixin {
    @Mixin(targets = "net.minecraft.structure.StrongholdGenerator$PortalRoom")
    public static class PortalRoomMixin {
        @ModifyConstant(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)V", constant = @Constant(floatValue = 0.9f))
        public float increaseEyeChance(float chance) {
            return 0.33f;
        }
    }
}
