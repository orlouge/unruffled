package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantedBookItem.class)
public class EnchantedBookItemMixin {
    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    private static void disableEnchantments(ItemStack stack, EnchantmentLevelEntry entry, CallbackInfo ci) {
        if (Config.INSTANCE.get().disabledEnchantments.contains(entry.enchantment)) ci.cancel();
    }

    @Inject(method = "hasGlint", at = @At("RETURN"), cancellable = true)
    public void disableGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
