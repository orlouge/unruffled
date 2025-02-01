package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.effect.BadOmenStatusEffect")
public class BadOmenStatusEffectMixin {
    @Inject(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
    public void destroyEvilTotemOnRaidOmenApplied(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getOffHandStack().isOf(CustomItems.EVIL_TOTEM)) {
            entity.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }
}
