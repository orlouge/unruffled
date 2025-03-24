package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin {
    @Shadow ItemStack book;

    @Inject(method = "hasBook", at = @At("HEAD"), cancellable = true)
    public void hasBookCodex(CallbackInfoReturnable<Boolean> cir) {
        if (this.book.isOf(CustomItems.ANCIENT_CODEX)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
    private static void resolveBookCodex(ItemStack book, CallbackInfoReturnable<Integer> cir) {
        if (book.isOf(CustomItems.ANCIENT_CODEX) && book.contains(AncientCodexItem.NUMBER)) {
            int number = book.get(AncientCodexItem.NUMBER);
            if (number > 0 && number <= AncientCodexItem.CONTENTS.size()) {
                cir.setReturnValue(AncientCodexItem.CONTENTS.get(number - 1).size());
                cir.cancel();
            }
        }
    }
}
