package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Redirect(method = "swingHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;resetLastAttackedTicks()V"))
    public void onSwingHandAttackCooldown(ServerPlayerEntity player) {
        if (player.getHungerManager() instanceof ExtendedHungerManager ext) {
            if (!ExtendedHungerManager.canAttack(player, ext.getStamina())) return;
        }
        player.resetLastAttackedTicks();
    }
}
