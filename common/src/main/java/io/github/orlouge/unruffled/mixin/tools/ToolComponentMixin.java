package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToolComponent.class)
public class ToolComponentMixin {
    @Inject(method = "getSpeed", cancellable = true, at = @At("RETURN"))
    public void increaseMiningSpeedMultiplier(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.EFFICIENCY)) {
            float mul = cir.getReturnValue();
            cir.setReturnValue(Math.max(mul * mul / 2, mul));
        }
    }
}
