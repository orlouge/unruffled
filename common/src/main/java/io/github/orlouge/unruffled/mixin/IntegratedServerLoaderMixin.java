package io.github.orlouge.unruffled.mixin;

import net.minecraft.server.integrated.IntegratedServerLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {
    @ModifyVariable(method = "checkBackupAndStart", at = @At("STORE"), ordinal = 1)
    public boolean disableExperimentalSettingsAnnoyance(boolean value) {
        return false;
    }
}
