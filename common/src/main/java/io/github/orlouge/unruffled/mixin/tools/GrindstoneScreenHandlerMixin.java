package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {
    @Inject(method = "grind", at = @At("HEAD"), cancellable = true)
    public void keepItemEnchantments(ItemStack item, int damage, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (ItemEnchantmentsHelper.hasItemEnchantments(item)) {
            ItemStack stack = item.copyWithCount(amount);
            if (damage > 0) {
                stack.setDamage(damage);
            } else {
                stack.removeSubNbt("Damage");
            }
            cir.setReturnValue(stack);
            cir.cancel();
        }
    }

    @Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
    public static class ResultSlotMixin {
        @Inject(method = "getExperience(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
        public void removeExperienceFromItemEnchantments(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            if (ItemEnchantmentsHelper.hasItemEnchantments(stack)) {
                cir.setReturnValue(0);
                cir.cancel();
            }
        }
    }
}
