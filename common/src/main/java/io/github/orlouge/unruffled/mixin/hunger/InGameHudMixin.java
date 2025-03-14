package io.github.orlouge.unruffled.mixin.hunger;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow private long heartJumpEndTick;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void redirectRenderXpBar(DrawContext context, int x, CallbackInfo ci) {
        this.client.getProfiler().push("expBar");
        ClientPlayerEntity player = this.client.player;
        if (player != null) {
            if (player.getHealth() >= player.getMaxHealth()) {
                this.heartJumpEndTick = 0;
            }
            if (!(player.getHungerManager() instanceof ExtendedHungerManager)) {
                return;
            }
        }
        float stamina = UnruffledModClient.stamina;
        if (stamina > 0) {
            int y = context.getScaledWindowHeight() - 32 + 3;
            int width = (int) (stamina * 182.0F);
            float regen = UnruffledModClient.lastStaminaRegeneration;
            float travel = UnruffledModClient.lastTravelPenalty;
            Sprite backgroundSprite = context.guiAtlasManager.getSprite(InGameHud.EXPERIENCE_BAR_BACKGROUND_TEXTURE);
            Sprite progressSprite = context.guiAtlasManager.getSprite(InGameHud.EXPERIENCE_BAR_PROGRESS_TEXTURE);
            //context.drawTexture(ICONS, x, y, 0, 64, 182, 5);
            context.drawTexturedQuad(backgroundSprite.getAtlasId(), x, x + 182, y, y + 5, 0, backgroundSprite.getMinU(), backgroundSprite.getMaxU(), backgroundSprite.getMinV(), backgroundSprite.getMaxV(), 1, 1 - travel, 1 - travel, 1);
            if (width > 0) {
                //context.drawTexture(ICONS, x, y, 0, 69, width, 5);
                if (regen <= 1) {
                    context.drawTexturedQuad(progressSprite.getAtlasId(), x, x + width, y, y + 5, 0, progressSprite.getMinU(), progressSprite.getFrameU((float) width / 182f), progressSprite.getMinV(), progressSprite.getMaxV(), 1, regen, regen, 1);
                } else {
                    context.drawTexturedQuad(progressSprite.getAtlasId(), x, x + width, y, y + 5, 0, progressSprite.getMinU(), progressSprite.getFrameU((float) width / 182f), progressSprite.getMinV(), progressSprite.getMaxV(), 1 / regen, 1 / regen, 1, 1);
                }
            }
        }
        this.client.getProfiler().pop();
        ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    public void disableXpLevelRendering(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!Config.INSTANCE.get().enchantmentsConfig.showLevelNumber()) ci.cancel();
    }

    /*
    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    public float redirectRenderCrosshairCooldown(ClientPlayerEntity player, float base) {
        float cooldownProgress = player.getAttackCooldownProgress(base);
        if (player.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            float requiredStamina = extendedHungerManager.getAttackExhaustion(player, cooldownProgress) * extendedHungerManager.getStaminaDepletionRate();
            return Math.min(1, UnruffledModClient.stamina / requiredStamina);
        } else {
            return cooldownProgress;
        }
    }
     */
}
