package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Redirect(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"))
    public int quickChargeTick(Enchantment enchantment, ItemStack stack) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(enchantment)) {
            return 2;
        } else {
            return EnchantmentHelper.getLevel(enchantment, stack);
        }
    }

    @Redirect(method = "getPullTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"))
    private static int quickChargePull(Enchantment enchantment, ItemStack stack) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(enchantment)) {
            return 2;
        } else {
            return EnchantmentHelper.getLevel(enchantment, stack);
        }
    }
}
