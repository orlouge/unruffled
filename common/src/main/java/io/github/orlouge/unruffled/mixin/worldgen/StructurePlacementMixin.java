package io.github.orlouge.unruffled.mixin.worldgen;

import io.github.orlouge.unruffled.interfaces.StructurePlacementWithSpreadFactor;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructurePlacement.class)
public class StructurePlacementMixin implements StructurePlacementWithSpreadFactor {
    @Override
    public void setSpreadFactor(float spreadFactor, float correctionFactor) {
    }
}
