package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private int unruffled_lastBadOmenCheckTicks = 0;
    private int unruffled_accumulatedBadOmenTicks = 0;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;updateTurtleHelmet()V", shift = At.Shift.AFTER))
    public void updateBadOmenEvilTotem(CallbackInfo ci) {
        unruffled_lastBadOmenCheckTicks++;
        if (unruffled_lastBadOmenCheckTicks > 15) {
            if (this.getEquippedStack(EquipmentSlot.OFFHAND).isOf(CustomItems.EVIL_TOTEM) && Config.INSTANCE.get().mechanicsConfig.badOmenFromEvoker()) {
                unruffled_accumulatedBadOmenTicks += unruffled_lastBadOmenCheckTicks;
                if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
                    if (serverPlayer.getServerWorld().getRaidAt(this.getBlockPos()) == null) {
                        this.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, -1, MathHelper.clamp(unruffled_accumulatedBadOmenTicks / 6000, 0, 4), false, false, true));
                    } else {
                        this.removeStatusEffect(StatusEffects.BAD_OMEN);
                    }
                }
            } else {
                unruffled_accumulatedBadOmenTicks = 0;
            }
            unruffled_lastBadOmenCheckTicks = 0;
        }
    }
}
