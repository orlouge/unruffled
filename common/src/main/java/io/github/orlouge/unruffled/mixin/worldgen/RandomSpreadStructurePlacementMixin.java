package io.github.orlouge.unruffled.mixin.worldgen;

import io.github.orlouge.unruffled.interfaces.StructurePlacementWithSpreadFactor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RandomSpreadStructurePlacement.class)
public abstract class RandomSpreadStructurePlacementMixin extends StructurePlacement implements StructurePlacementWithSpreadFactor {
    @Shadow @Final private int spacing;
    @Shadow @Final private int separation;
    @Shadow @Final private SpreadType spreadType;
    protected float spreadFactor = -1, correctionFactor = -1;

    protected RandomSpreadStructurePlacementMixin(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    @Override
    public void setSpreadFactor(float spreadFactor, float correctionFactor) {
        this.spreadFactor = spreadFactor;
        this.correctionFactor = correctionFactor;
    }

    @Inject(method = "getStartChunk", at = @At("HEAD"), cancellable = true)
    public void applySpreadFactor(long seed, int chunkX, int chunkZ, CallbackInfoReturnable<ChunkPos> cir) {
        if (spreadFactor > 0) {
            float correctedSpreadFactor = this.separation > 0 ? Math.max(1, spreadFactor / (1 + this.separation / this.correctionFactor)) : 1;
            int spacing = (int) (this.spacing * correctedSpreadFactor);
            int separation = (int) (this.separation * correctedSpreadFactor);
            int i = Math.floorDiv(chunkX, spacing);
            int j = Math.floorDiv(chunkZ, spacing);
            ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
            chunkRandom.setRegionSeed(seed, i, j, this.getSalt());
            int k = spacing - separation;
            int l = this.spreadType.get(chunkRandom, k);
            int m = this.spreadType.get(chunkRandom, k);
            cir.setReturnValue(new ChunkPos(i * spacing + l, j * spacing + m));
            cir.cancel();
        }
    }
}
