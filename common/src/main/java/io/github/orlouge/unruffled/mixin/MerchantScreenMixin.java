package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.client.gui.screen.ingame.MerchantScreen$WidgetButtonPage")
public class MerchantScreenMixin {
    private static Long randomSeed = null;

    @ModifyArg(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemTooltip(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"), index = 1)
    public ItemStack ancientCodexDisplay(ItemStack stack) {
        if (stack.isOf(CustomItems.ANCIENT_CODEX) && stack.hasNbt() && stack.getNbt().contains("number", NbtElement.INT_TYPE)) {
            stack = stack.copy();
            if (randomSeed == null) randomSeed = RandomSeed.getSeed();
            stack = AncientCodexItem.setExcerpt(stack, Random.create(randomSeed + stack.getOrCreateNbt().getInt("number")));
        }
        return stack;
    }
}
