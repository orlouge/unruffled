package io.github.orlouge.unruffled.mixin.potions;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BrewingStandScreenHandler.class)
public class BrewingStandScreenHandlerMixin {
    @Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$PotionSlot")
    public static class PotionSlotMixin extends Slot {
        public PotionSlotMixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        /*
        @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", cancellable = true, at = @At("RETURN"))
        public void canInsertCheck(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(cir.getReturnValue() && this.inventory.getStack(this.getIndex()).isEmpty());
        }

         */

    }
}
