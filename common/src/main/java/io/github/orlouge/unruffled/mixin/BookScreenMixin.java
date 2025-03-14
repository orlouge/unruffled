package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookScreen.class)
public class BookScreenMixin {
    @Mixin(BookScreen.Contents.class)
    public static class ContentsMixin {
        @Inject(method = "create", at = @At("HEAD"), cancellable = true)
        private static void openAncientCodex(ItemStack stack, CallbackInfoReturnable<BookScreen.Contents> cir) {
            if (stack.isOf(CustomItems.ANCIENT_CODEX) && stack.contains(AncientCodexItem.NUMBER)) {
                int number = stack.get(AncientCodexItem.NUMBER);
                if (number > 0 && number <= AncientCodexItem.CONTENTS.size()) {
                    cir.setReturnValue(new BookScreen.Contents(AncientCodexItem.CONTENTS.get(number - 1)));
                    cir.cancel();
                }
            }
        }
    }
}
