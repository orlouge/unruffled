package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    public void disableEnchantmentsRandomLoot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ItemEnchantmentsHelper.isDisabled((Enchantment) (Object) this, stack)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "isAvailableForRandomSelection", at = @At("HEAD"), cancellable = true)
    public void disableEnchantmentsRandomSelection(CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.unobtainableEnchantments().contains((Enchantment) (Object) this)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "isAvailableForEnchantedBookOffer", at = @At("HEAD"), cancellable = true)
    public void disableEnchantmentsBookOffer(CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.unobtainableEnchantments().contains((Enchantment) (Object) this)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
