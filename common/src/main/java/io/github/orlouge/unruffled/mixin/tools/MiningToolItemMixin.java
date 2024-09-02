package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MiningToolItem.class)
public class MiningToolItemMixin {
    @Inject(method = "getMiningSpeedMultiplier", cancellable = true, at = @At("RETURN"))
    public void increaseMiningSpeedMultiplier(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        if (Config.INSTANCE.get().disabledEnchantments.contains(Enchantments.EFFICIENCY)) {
            float mul = cir.getReturnValue();
            cir.setReturnValue(Math.max(mul * mul / 2, mul));
        }
    }
}
