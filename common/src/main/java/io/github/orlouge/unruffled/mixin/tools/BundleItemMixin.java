package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin extends Item {
    public BundleItemMixin(Settings settings) {
        super(settings);
    }

    /*
    @Inject(method = "addToBundle", at = @At("HEAD"), cancellable = true)
    private static void disableBundleNesting(ItemStack bundle, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.isOf(Items.BUNDLE)) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
     */

    @Override
    public boolean canBeNested() {
        return Config.INSTANCE.get().mechanicsConfig.bundleSize() <= 64;
    }
}
