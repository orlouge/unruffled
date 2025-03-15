package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    public void removeEnchantmentGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isOf(Items.ENCHANTED_BOOK) && !stack.isOf(Items.RECOVERY_COMPASS) && Config.INSTANCE.get().enchantmentsConfig.disableGlint()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void nameTagLodestone(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if ((Object) this instanceof NameTagItem && context.getWorld() instanceof ServerWorld serverWorld && serverWorld.getBlockState(context.getBlockPos()).isOf(Blocks.LODESTONE)) {
            TradedCompasses compassDB = TradedCompasses.get(serverWorld.getPersistentStateManager());
            if (context.getStack().contains(DataComponentTypes.CUSTOM_NAME)) {
                Text newLore = context.getStack().get(DataComponentTypes.CUSTOM_NAME).getWithStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)).get(0);
                compassDB.storeLodestoneName(context.getBlockPos(), newLore);
            } else {
                compassDB.deleteLodestoneName(context.getBlockPos());
            }
            cir.setReturnValue(ActionResult.SUCCESS_NO_ITEM_USED);
            cir.cancel();
        }
    }
}
