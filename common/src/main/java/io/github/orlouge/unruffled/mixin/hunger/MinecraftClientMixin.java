package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Redirect(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V"))
    public void onAttackMiss(ClientPlayerEntity instance) {
        UnruffledModClient.onAttackMiss();
    }

    @Inject(method = "doAttack", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"))
    public void beforeAttackingEntity(CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null) {
            if (!ExtendedHungerManager.canAttack(this.player, UnruffledModClient.stamina)) cir.cancel();
        }
    }

    @ModifyConstant(method = "doAttack", constant = @Constant(intValue = 10, ordinal = 1))
    public int decreaseMissCooldown(int cooldown) {
        return cooldown / 5;
    }
}
