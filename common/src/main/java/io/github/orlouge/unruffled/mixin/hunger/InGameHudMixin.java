package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    private static final Identifier ICONS = new Identifier("textures/gui/icons.png");

    @Shadow @Final private MinecraftClient client;

    @Shadow private int scaledHeight;

    @Shadow public abstract void renderExperienceBar(DrawContext context, int x);

    @Shadow private long heartJumpEndTick;

    @Shadow private int scaledWidth;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void redirectRenderXpBar(DrawContext context, int x, CallbackInfo ci) {
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
            int y = this.scaledHeight - 32 + 3;
            int width = (int) (stamina * 183.0F);
            float regen = UnruffledModClient.lastStaminaRegeneration;
            float travel = UnruffledModClient.lastTravelPenalty;
            //context.drawTexture(ICONS, x, y, 0, 64, 182, 5);
            context.drawTexturedQuad(ICONS, x, x + 182, y, y + 5, 0, 0f, 182f / 256f, 64f / 256f, (64f + 5f) / 256f, 1, 1 - travel, 1 - travel, 1);
            if (width > 0) {
                //context.drawTexture(ICONS, x, y, 0, 69, width, 5);
                if (regen <= 1) {
                    context.drawTexturedQuad(ICONS, x, x + width, y, y + 5, 0, 0f, (float) width / 256f, 69f / 256f, (69f + 5f) / 256f, 1, regen, regen, 1);
                } else {
                    context.drawTexturedQuad(ICONS, x, x + width, y, y + 5, 0, 0f, (float) width / 256f, 69f / 256f, (69f + 5f) / 256f, 1 / regen, 1 / regen, 1, 1);
                }
            }
        }
        if (Config.INSTANCE.get().enchantmentsConfig.showLevelNumber()) {
            if (this.client.player.experienceLevel > 0) {
                String levelString = "" + this.client.player.experienceLevel;
                int k = (this.scaledWidth - this.getTextRenderer().getWidth(levelString)) / 2;
                int l = this.scaledHeight - 31 - 4;
                context.drawText(this.getTextRenderer(), levelString, k + 1, l, 0, false);
                context.drawText(this.getTextRenderer(), levelString, k - 1, l, 0, false);
                context.drawText(this.getTextRenderer(), levelString, k, l + 1, 0, false);
                context.drawText(this.getTextRenderer(), levelString, k, l - 1, 0, false);
                context.drawText(this.getTextRenderer(), levelString, k, l, 8453920, false);
            }
        }
        ci.cancel();
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
