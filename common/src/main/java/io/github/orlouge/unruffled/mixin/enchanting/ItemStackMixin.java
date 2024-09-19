package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    public void disableEnchantments(Enchantment enchantment, int level, CallbackInfo ci) {
        if (ItemEnchantmentsHelper.isDisabled(enchantment, (ItemStack) (Object) this)) ci.cancel();
    }
}
