package io.github.orlouge.unruffled.mixin.enchanting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    public void removeEnchantmentGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
