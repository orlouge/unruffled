package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompassItem.class)
public class CompassItemMixin {
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"))
    public void addTradeIfNamed(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer && stack.contains(DataComponentTypes.LODESTONE_TRACKER) && stack.contains(DataComponentTypes.CUSTOM_NAME) && !stack.get(DataComponentTypes.CUSTOM_NAME).asTruncatedString(10).isEmpty()) {
            ItemStack buyStack = stack.copyComponentsToNewStack(Items.COMPASS, 1);
            TradedCompasses.get(serverPlayer.getServerWorld().getPersistentStateManager()).addBuy(serverPlayer, buyStack);
        }
    }
}
