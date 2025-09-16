package io.github.orlouge.unruffled.mixin.hunger;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @ModifyExpressionValue(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasExperienceBar()Z"))
    public boolean disableXpLevelRendering(boolean original) {
        if (!Config.INSTANCE.get().enchantmentsConfig.showLevelNumber()) return false;
        return original;
    }
}
