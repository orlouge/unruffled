package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.TeleporterEntity;
import io.github.orlouge.unruffled.potions.TeleportEffect;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.StreamSupport;

@Mixin(ConsumableComponent.class)
public class ConsumableComponentMixin {
    @Inject(method = "finishConsumption", at = @At("HEAD"), cancellable = true)
    public void checkTeleport(World world, LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (StreamSupport.stream(stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects().spliterator(), false)
            .anyMatch(eff -> eff.equals(UnruffledMod.TELEPORTATION_EFFECT))) {
            if (world.isClient) {
                cir.setReturnValue(stack);
                cir.cancel();
            }
            if (user instanceof TeleporterEntity teleporter && teleporter.getTeleportCooldown() > 0 && user instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.translatable("effect." + UnruffledMod.MOD_ID + ".teleport.cooldown"), true);
                cir.setReturnValue(stack);
                cir.cancel();
            } else if (user instanceof ServerPlayerEntity player && TeleportEffect.getTeleportPos(player).isEmpty()) {
                cir.setReturnValue(stack);
                cir.cancel();
            } else {
                if (user instanceof TeleporterEntity teleporter) teleporter.setTeleportCooldown(50);
            }
        }
    }
}
