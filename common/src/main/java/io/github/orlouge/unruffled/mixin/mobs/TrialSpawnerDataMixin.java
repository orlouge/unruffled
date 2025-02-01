package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrialSpawnerData.class)
public class TrialSpawnerDataMixin {
    @Inject(method = "applyTrialOmen", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;removeStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", shift = At.Shift.BEFORE))
    private static void destroyEvilTotemOnTrialOmenApplied(PlayerEntity player, CallbackInfo ci) {
        if (player.getOffHandStack().isOf(CustomItems.EVIL_TOTEM)) {
            player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }
}
