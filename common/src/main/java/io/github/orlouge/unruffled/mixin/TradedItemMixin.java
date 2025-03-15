package io.github.orlouge.unruffled.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TradedItem.class)
public class TradedItemMixin {
    private static Long randomSeed = null;

    @ModifyReturnValue(method = "createDisplayStack", at = @At("RETURN"))
    private static ItemStack ancientCodexDisplay(ItemStack stack) {
        if (stack.isOf(CustomItems.ANCIENT_CODEX) && stack.contains(AncientCodexItem.NUMBER)) {
            if (randomSeed == null) randomSeed = RandomSeed.getSeed();
            stack = AncientCodexItem.setExcerpt(stack, Random.create(randomSeed + stack.get(AncientCodexItem.NUMBER)));
        }
        return stack;
    }
}
