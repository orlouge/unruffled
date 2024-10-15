package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;
    private boolean isRenaming = false;

    public AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 0))
    public int calculateRepairAmount(int dmg, int maxDmg4) {
        return dmg;
    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/screen/AnvilScreenHandler;sendContentUpdates()V"))
    public void modifyResult(CallbackInfo ci) {
        if (!Config.INSTANCE.get().enchantmentsConfig.disableEnchantingTable()) return;
        this.levelCost.set(0);
    }

    @Inject(method = "getNextCost", at = @At("HEAD"), cancellable = true)
    private static void removeRepairCost(int cost, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
        cir.cancel();
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void alwaysTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    public void checkIfRenaming(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        this.isRenaming = this.input.getStack(1).isEmpty();

        if (player instanceof ServerPlayerEntity serverPlayer && CompassItem.hasLodestone(stack)) {
            ItemStack buyStack = new ItemStack(Items.COMPASS);
            buyStack.setNbt(stack.getNbt().copy());
            TradedCompasses.get(serverPlayer.getServerWorld().getPersistentStateManager()).addBuy(serverPlayer, buyStack);
        }
    }

    @Redirect(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    public void dontDamageAnvilOnRename(ScreenHandlerContext context, BiConsumer<World, BlockPos> function) {
        if (this.isRenaming) {
            this.isRenaming = false;
            context.run((world, pos) -> world.syncWorldEvent(1030, pos, 0));
        } else {
            context.run(function);
        }
    }
}
