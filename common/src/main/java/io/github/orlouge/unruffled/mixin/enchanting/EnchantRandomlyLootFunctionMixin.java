package io.github.orlouge.unruffled.mixin.enchanting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantRandomlyLootFunction.class)
public class EnchantRandomlyLootFunctionMixin {
    @Inject(method = "process", at = @At("HEAD"), cancellable = true)
    public void removeBookEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isOf(Items.BOOK)) {
            cir.setReturnValue(stack);
            cir.cancel();
        }
    }
}
