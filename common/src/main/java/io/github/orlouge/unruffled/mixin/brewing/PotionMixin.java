package io.github.orlouge.unruffled.mixin.brewing;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;

@Mixin(Potion.class)
public class PotionMixin {
    private ImmutableList<StatusEffectInstance> actualEffects = null;

    @Inject(method = "getEffects", at = @At("RETURN"), cancellable = true)
    public void getLongerEffects(CallbackInfoReturnable<List<StatusEffectInstance>> cir) {
        if (this.actualEffects == null) {
            List<StatusEffectInstance> effects = new LinkedList<>();
            for (StatusEffectInstance effect : cir.getReturnValue()) {
                if (!effect.getEffectType().isInstant() && !effect.isInfinite()) {
                    StatusEffectInstance longerEffect = new StatusEffectInstance(effect);
                    longerEffect.duration = longerEffect.mapDuration(duration -> duration * 2);
                    effects.add(longerEffect);
                } else {
                    effects.add(effect);
                }
            }
            this.actualEffects = ImmutableList.copyOf(effects);
        }
        cir.setReturnValue(this.actualEffects);
    }
}
