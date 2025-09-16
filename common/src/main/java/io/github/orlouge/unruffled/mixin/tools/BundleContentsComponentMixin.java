package io.github.orlouge.unruffled.mixin.tools;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.orlouge.unruffled.config.Config;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BundleContentsComponent.class)
public class BundleContentsComponentMixin {
    @Inject(method = "getOccupancy(Lnet/minecraft/item/ItemStack;)Lorg/apache/commons/lang3/math/Fraction;", at = @At("RETURN"), cancellable = true)
    private static void modifyOccupancy(ItemStack stack, CallbackInfoReturnable<Fraction> cir) {
        cir.setReturnValue(cir.getReturnValue().multiplyBy(Fraction.getFraction(64, Config.INSTANCE.get().mechanicsConfig.bundleSize())));
    }

    @Mixin(BundleContentsComponent.Builder.class)
    public static class BuilderMixin {
        @Shadow @Final private List<ItemStack> stacks;

        @ModifyExpressionValue(method = "getInsertionIndex", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areItemsAndComponentsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
        public boolean preventStackingAbove99(boolean original, ItemStack stack, @Local int slot) {
            return original && (this.stacks.get(slot).getCount() + stack.getCount() <= 99);
        }
        /*
        @ModifyVariable(method = "getMaxAllowed", at = @At("STORE"))
        public Fraction spaceLeft(Fraction fraction) {
            return Fraction.getFraction(Config.INSTANCE.get().mechanicsConfig.bundleSize() - 64, 64).add(fraction);
        }

         */
    }
}
