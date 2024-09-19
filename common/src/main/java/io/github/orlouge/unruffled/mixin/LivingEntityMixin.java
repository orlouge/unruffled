package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.Config;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.9900000095367432))
    public double decreaseHorizontalElytraSpeed(double speed) {
        return speed * Config.INSTANCE.get().elytraConfig.horizontalGlidingSpeedFactor();
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.9800000190734863, ordinal = 0))
    public double decreaseVerticalElytraSpeed(double speed) {
        return speed * Config.INSTANCE.get().elytraConfig.verticalGlidingSpeedFactor();
    }

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    public void disableTotemAttempt(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.get().mechanicsConfig.disableTotemOfUndying()) cir.cancel();
    }
}
