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
    public interface ContentsMixin {
        @Inject(method = "create", at = @At("HEAD"), cancellable = true)
        private static void openAncientCodex(ItemStack stack, CallbackInfoReturnable<BookScreen.Contents> cir) {
            if (stack.isOf(CustomItems.ANCIENT_CODEX) && stack.hasNbt() && stack.getNbt().contains("number")) {
                int number = stack.getNbt().getInt("number");
                if (number > 0 && number <= AncientCodexItem.CONTENTS.size()) {
                    cir.setReturnValue(new AncientCodexItem.Contents(AncientCodexItem.CONTENTS.get(number - 1)));
                    cir.cancel();
                }
            }
        }
    }
}
