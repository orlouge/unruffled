package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Redirect(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;resetLastAttackedTicks()V"))
    public void onAttackResetCooldown(PlayerEntity player) {
        if (!ExtendedHungerManager.canAttack(player, UnruffledModClient.stamina)) return;
        player.resetLastAttackedTicks();
    }

    @Redirect(method = "cancelBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V"))
    public void sendMissAttackOnBlockBreak(ClientPlayerEntity instance) {
        UnruffledModClient.onAttackMiss(instance, true);
    }
}
