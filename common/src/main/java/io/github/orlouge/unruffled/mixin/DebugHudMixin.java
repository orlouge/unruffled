package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.config.Config;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract World getWorld();

    @Shadow protected abstract WorldChunk getClientChunk();

    @Shadow
    private static String getBiomeString(RegistryEntry<Biome> biome) {
        return null;
    }

    @Shadow @Nullable private ChunkPos pos;

    @Shadow public abstract void resetChunk();

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    public void addExtraDebugInfoIfReduced(CallbackInfoReturnable<List<String>> cir) {
        if (this.client.hasReducedDebugInfo() && Config.INSTANCE.get().navigationConfig.forceReducedDebugInfo()) {
            List<String> extra = new LinkedList<>(cir.getReturnValue());

            Entity entity = this.client.getCameraEntity();
            if (entity != null) {
                if (Config.INSTANCE.get().navigationConfig.reducedDebugFacing()) {
                    Direction direction = entity.getHorizontalFacing();
                    String textDirection = switch (direction) {
                        case NORTH -> "Towards negative Z";
                        case SOUTH -> "Towards positive Z";
                        case WEST -> "Towards negative X";
                        case EAST -> "Towards positive X";
                        default -> "Invalid";
                    };
                    extra.add(
                        String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, textDirection, MathHelper.wrapDegrees(entity.getYaw()), MathHelper.wrapDegrees(entity.getPitch()))
                    );
                }

                ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
                if (!Objects.equals(this.pos, chunkPos)) {
                    this.pos = chunkPos;
                    this.resetChunk();
                }
            }

            World world = this.getWorld();
            LongSet forcedChunkSet = (world instanceof ServerWorld ? ((ServerWorld)world).getForcedChunks() : LongSets.EMPTY_SET);
            extra.add(7, this.client.world.getRegistryKey().getValue() + " FC: " + forcedChunkSet.size());

            if (entity != null) {
                WorldChunk worldChunk = this.getClientChunk();
                if (worldChunk.isEmpty()) {
                    extra.add("Waiting for chunk...");
                } else {
                    BlockPos blockPos = this.client.getCameraEntity().getBlockPos();
                    int light = this.client.world.getChunkManager().getLightingProvider().getLight(blockPos, 0);
                    int sky = this.client.world.getLightLevel(LightType.SKY, blockPos);
                    int block = this.client.world.getLightLevel(LightType.BLOCK, blockPos);
                    extra.add("Client Light: " + light + " (" + sky + " sky, " + block + " block)");

                    if (Config.INSTANCE.get().navigationConfig.reducedDebugBiome()) {
                        if (blockPos.getY() >= this.client.world.getBottomY() && blockPos.getY() < this.client.world.getTopY()) {
                            extra.add("Biome: " + getBiomeString(this.client.world.getBiome(blockPos)));
                        }
                    }
                }
            }

            cir.setReturnValue(new ArrayList<>(extra));
        }
    }
}
