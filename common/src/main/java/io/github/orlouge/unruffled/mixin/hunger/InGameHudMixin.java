package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    private static final Identifier ICONS = new Identifier("textures/gui/icons.png");

    @Shadow @Final private MinecraftClient client;

    @Shadow private int scaledHeight;

    @Shadow public abstract void renderExperienceBar(DrawContext context, int x);

    @Shadow private long heartJumpEndTick;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderExperienceBar(Lnet/minecraft/client/gui/DrawContext;I)V"))
    public void redirectRenderXpBar(InGameHud instance, DrawContext context, int x) {
        ClientPlayerEntity player = ((InGameHudMixin) (Object) instance).client.player;
        if (player != null) {
            if (player.getHealth() >= player.getMaxHealth()) {
                this.heartJumpEndTick = 0;
            }
            if (!(player.getHungerManager() instanceof ExtendedHungerManager hungerManager)) {
                instance.renderExperienceBar(context, x);
                return;
            }
        }
        float stamina = UnruffledModClient.stamina;
        if (stamina > 0) {
            int y = ((InGameHudMixin) (Object) instance).scaledHeight - 32 + 3;
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
