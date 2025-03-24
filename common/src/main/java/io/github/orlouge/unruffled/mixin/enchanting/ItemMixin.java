package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.GlobalPos;
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
            GlobalPos pos = GlobalPos.create(serverWorld.getRegistryKey(), context.getBlockPos());
            if (context.getStack().hasCustomName()) {
                Text newLore = context.getStack().getName().getWithStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)).get(0);
                if (context.getPlayer() instanceof ServerPlayerEntity player) UnruffledMod.NAME_LODESTONE_CRITERION.trigger(player);
                compassDB.storeLodestoneName(pos, newLore);
            } else {
                compassDB.deleteLodestoneName(pos);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
    }
}
