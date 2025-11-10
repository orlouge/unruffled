package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.gui.hud.debug.BiomeDebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BiomeDebugHudEntry.class)
public abstract class BiomeDebugEntryMixin implements DebugHudEntry {
    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return Config.INSTANCE.get().navigationConfig.reducedDebugBiome() || !reducedDebugInfo;
    }
}
