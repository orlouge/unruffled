package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ImpalingEnchantment;
import net.minecraft.entity.EntityGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ImpalingEnchantment.class)
public class ImpalingEnchantmentMixin {
    @Inject(method = "getAttackDamage", at = @At("HEAD"), cancellable = true)
    public void nullifyImpalingIfDisabled(int level, EntityGroup group, CallbackInfoReturnable<Float> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.IMPALING)) {
            cir.setReturnValue(0f);
            cir.cancel();
        }
    }
}
