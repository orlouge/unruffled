package io.github.orlouge.unruffled.mixin.worldgen;

import io.github.orlouge.unruffled.interfaces.StructurePlacementWithSpreadFactor;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @Inject(method = "setStructureStarts", at = @At("HEAD"))
    public void modifyStructurePlacement(DynamicRegistryManager registryManager, StructurePlacementCalculator placementCalculator, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager, CallbackInfo ci) {
        for (RegistryEntry<StructureSet> entry : placementCalculator.getStructureSets()) {
            if (entry.value().placement() instanceof StructurePlacementWithSpreadFactor structurePlacementWithSpreadFactor) {
                structurePlacementWithSpreadFactor.setSpreadFactor(2.5f, 16);
            }
        }
    }
}
