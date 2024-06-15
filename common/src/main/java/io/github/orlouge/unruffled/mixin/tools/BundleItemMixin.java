package io.github.orlouge.unruffled.mixin.tools;

import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin extends Item {
    private static final int BUNDLE_MAX_STORAGE = 256;

    public BundleItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyConstant(method = "getAmountFilled", constant = @Constant(floatValue = 64f))
    private static float modifyGetAmountFilled(float max) {
        return (float) BUNDLE_MAX_STORAGE;
    }

    @ModifyConstant(method = "onStackClicked", constant = @Constant(intValue = 64))
    public int modifyOnStackClicked(int max) {
        return BUNDLE_MAX_STORAGE;
    }

    @ModifyConstant(method = "getItemBarStep", constant = @Constant(intValue = 64))
    public int modifyGetItemBarStep(int max) {
        return BUNDLE_MAX_STORAGE;
    }

    @ModifyConstant(method = "addToBundle", constant = @Constant(intValue = 64))
    private static int modifyAddToBundle(int max) {
        return BUNDLE_MAX_STORAGE;
    }

    @ModifyConstant(method = "appendTooltip", constant = @Constant(intValue = 64))
    public int modifyAppendTooltip(int max) {
        return BUNDLE_MAX_STORAGE;
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
        return false;
    }

    @ModifyVariable(method = "addToBundle", at = @At("STORE"))
    private static Optional<NbtCompound> checkStackSize(Optional<NbtCompound> mergedStack, ItemStack bundle, ItemStack stack) {
        if (mergedStack.isPresent() && ItemStack.fromNbt(mergedStack.get()).getCount() + stack.getCount() > stack.getMaxCount()) {
            return Optional.empty();
        } else {
            return mergedStack;
        }
    }
}
