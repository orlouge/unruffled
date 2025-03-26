package io.github.orlouge.unruffled.mixin.potions;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "occludeVibrationSignals", at = @At("HEAD"), cancellable = true)
    public void occludeIfSilenceEffect(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity entity && entity.hasStatusEffect(UnruffledMod.SILENCE_EFFECT)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
