package io.github.orlouge.unruffled.mixin.worldgen;

import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DensityFunctionTypes.class)
public class DensityFunctionTypesMixin {
    @Mixin(targets = "net.minecraft.world.gen.densityfunction.DensityFunctionTypes$EndIslands")
    public static class EndIslandsMixin {
        /*
        @ModifyConstant(method = "Lnet/minecraft/world/gen/densityfunction/DensityFunctionTypes$EndIslands;sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F", constant = @Constant(longValue = 4096L))
        private static long decreaseVoidRingSize(long size) {
            return size / 16;
        }
         */
    }
}
