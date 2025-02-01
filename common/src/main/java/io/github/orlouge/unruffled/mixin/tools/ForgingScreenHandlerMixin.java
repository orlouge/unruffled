package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgingScreenHandler.class)
public abstract class ForgingScreenHandlerMixin extends ScreenHandler {
    protected ForgingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ForgingScreenHandler;insertItem(Lnet/minecraft/item/ItemStack;IIZ)Z", ordinal = 0))
    public void addCompassTradeOnQuickTakeOutput(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = this.slots.get(slot).getStack();
        if (player instanceof ServerPlayerEntity serverPlayer && stack.contains(DataComponentTypes.LODESTONE_TRACKER)) {
            ItemStack buyStack = stack.copyComponentsToNewStack(Items.COMPASS, 1);
            TradedCompasses.get(serverPlayer.getServerWorld().getPersistentStateManager()).addBuy(serverPlayer, buyStack);
        }
    }
}
