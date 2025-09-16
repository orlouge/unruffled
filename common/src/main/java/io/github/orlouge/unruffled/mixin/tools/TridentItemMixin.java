package io.github.orlouge.unruffled.mixin.tools;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @ModifyVariable(method = "<init>", at = @At("LOAD"), argsOnly = true)
    private static Item.Settings repairWithDiamond(Item.Settings settings) {
        return settings.repairable(Items.DIAMOND);
    }
}
