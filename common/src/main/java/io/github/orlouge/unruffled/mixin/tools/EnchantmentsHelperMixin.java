package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentsHelperMixin {
    @Inject(method = "onTargetDamaged(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private static void evilTotemThornsEffect(ServerWorld serverWorld, Entity user, DamageSource damageSource, ItemStack itemStack, CallbackInfo ci) {
        Entity attacker = damageSource.getAttacker();
        if (attacker != null && user instanceof LivingEntity livingUser && livingUser.getEquippedStack(EquipmentSlot.OFFHAND).isOf(CustomItems.EVIL_TOTEM)) {
            attacker.damage(user.getDamageSources().thorns(attacker), 1 + user.getRandom().nextInt(4));
            if (attacker instanceof LivingEntity livingAttacker) {
                livingAttacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0, false, true, true));
                livingAttacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, false, false, true));
            }
        }
    }

    @Inject(method = "getCrossbowChargeTime", at = @At("RETURN"), cancellable = true)
    private static void alwaysQuickCharge(ItemStack stack, LivingEntity user, float baseCrossbowChargeTime, CallbackInfoReturnable<Float> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.QUICK_CHARGE)) {
            cir.setReturnValue(Math.min(cir.getReturnValue(), baseCrossbowChargeTime - 0.5f));
        }
    }
}
