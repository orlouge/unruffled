package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WrittenBookItem.class)
public class WrittenBookItemMixin {
    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
    private static void resolveBookCodex(ItemStack book, CallbackInfoReturnable<Integer> cir) {
        if (book.isOf(CustomItems.ANCIENT_CODEX) && book.hasNbt() && book.getOrCreateNbt().contains("number", NbtElement.INT_TYPE)) {
            int number = book.getOrCreateNbt().getInt("number");
            if (number > 0 && number <= AncientCodexItem.CONTENTS.size()) {
                cir.setReturnValue(AncientCodexItem.CONTENTS.get(number - 1).size());
                cir.cancel();
            }
        }
    }
}
