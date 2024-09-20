package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public class RaidMixin {
    @Inject(method = "start", at = @At("HEAD"))
    public void destroyEvilTotemOnRaidStart(PlayerEntity player, CallbackInfo ci) {
        if (player.getOffHandStack().isOf(CustomItems.EVIL_TOTEM)) {
            player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }
}
