package io.github.orlouge.unruffled.mixin.worldgen;

import net.minecraft.world.biome.source.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TheEndBiomeSource.class)
public class TheEndBiomeSourceMixin {
    /*
    @ModifyConstant(method = "getBiome", constant = @Constant(longValue = 4096L))
    public long reduceVoidRingSize(long ringSize) {
        return ringSize / 16;
    }
     */
}
