package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerScreenHandler.class)
public class PlayerScreenHandlerMixin {
    @Mixin(targets = "net.minecraft.screen.PlayerScreenHandler$2")
    public static class OffhandSlotMixin extends Slot {
        public OffhandSlotMixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            ItemStack stack = this.getStack();
            return (stack.isEmpty() || playerEntity.isCreative() || !stack.isOf(CustomItems.EVIL_TOTEM)) && super.canTakeItems(playerEntity);
        }
    }
}
