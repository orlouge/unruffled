package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    public void reduceWearinessOnHeal(float amount, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player && player.getHungerManager() instanceof ExtendedHungerManager ext) {
            ext.addWeariness(-amount * 0.001f);
        }
    }
}
