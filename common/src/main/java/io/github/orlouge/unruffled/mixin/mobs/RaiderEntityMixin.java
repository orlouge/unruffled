package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.Config;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RaiderEntity.class)
public class RaiderEntityMixin {
    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean noBadOmenFromBanner(ItemStack left, ItemStack right) {
        if (Config.INSTANCE.get().mechanicsConfig.badOmenFromCaptain()) return ItemStack.areEqual(left, right);
        return false;
    }
}
