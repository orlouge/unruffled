package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin implements Bar {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
    public void renderStamina(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player == null || !(player.getHungerManager() instanceof ExtendedHungerManager)) {
            return;
        }

        int i = this.getCenterX(this.client.getWindow());
        int j = this.getCenterY(this.client.getWindow());
        float stamina = UnruffledModClient.stamina;
        int width = (int) (stamina * 182.0F);
        float regen = UnruffledModClient.lastStaminaRegeneration;
        float travel = UnruffledModClient.lastTravelPenalty;
        int travelColor = (0xFF << 24) | (0xFF << 16) | (Math.round(0xFF * (1 - travel)) << 8) | Math.round(0xFF * (1 - travel));
        int regenColor = (0xFF << 24) | (regen <= 1
            ? (0xFF << 16) | (Math.round(0xFF * regen) << 8) | Math.round(0xFF * regen)
            : ((Math.round(0xFF / regen)) << 16) | (Math.round(0xFF / regen) << 8) | 0xFF);

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ExperienceBar.BACKGROUND, i, j, 182, 5, travelColor);
        if (width > 0) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ExperienceBar.PROGRESS, 182, 5, 0, 0, i, j, width, 5, regenColor);
        }

        ci.cancel();
    }
}
