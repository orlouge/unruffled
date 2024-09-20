package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
public class EnchantmentsHelperMixin {
    @Inject(method = "onUserDamaged", at = @At("HEAD"))
    private static void evilTotemThornsEffect(LivingEntity user, Entity attacker, CallbackInfo ci) {
        if (user.getEquippedStack(EquipmentSlot.OFFHAND).isOf(CustomItems.EVIL_TOTEM)) {
            attacker.damage(user.getDamageSources().thorns(attacker), ThornsEnchantment.getDamageAmount(2, user.getRandom()));
            if (attacker instanceof LivingEntity livingAttacker) {
                livingAttacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0, false, true, true));
                livingAttacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, false, false, true));
            }
        }
    }
}
