package io.github.orlouge.unruffled.mixin.hunger;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import io.github.orlouge.unruffled.interfaces.HasFireImmunitySetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    @Inject(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    public void reduceWearinessOnHeal(float amount, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player && player.getHungerManager() instanceof ExtendedHungerManager ext) {
            ext.addWeariness(-amount * 0.001f);
        }
    }

    @ModifyConstant(method = "createItemEntity", constant = @Constant(floatValue = 0.5f, ordinal = 0))
    public float modifyDropSpread(float velocity) {
        return velocity * Config.INSTANCE.get().mechanicsConfig.dropSpreadFactor();
    }

    @Inject(method = "createItemEntity", at = @At(value = "RETURN"))
    public void makeDeathDropsFireImmune(ItemStack stack, boolean atSelf, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity drop = cir.getReturnValue();
        if (atSelf && !retainOwnership && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && drop instanceof HasFireImmunitySetting immuneDrop) {
            immuneDrop.setFireImmune(true);
        }
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getDamageBlockedAmount(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)F"))
    public float consumeStaminaOnShieldBlock(float amount) {
        if ((Object) this instanceof PlayerEntity player && player.getHungerManager() instanceof ExtendedHungerManager ext) {
            ext.addStamina(Math.min(0.33f, -amount / 30f));
        }
        return amount;
    }
}
