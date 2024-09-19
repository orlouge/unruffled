package io.github.orlouge.unruffled.mixin.potions;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    @Shadow protected abstract void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source);

    @Shadow public abstract ItemStack eatFood(World world, ItemStack stack);

    @Shadow protected abstract void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source);

    @Shadow public abstract boolean canHaveStatusEffect(StatusEffectInstance effect);

    TeleporterEntity lastTeleporter = null;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void rememberTeleportingPlayer(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (!effect.getEffectType().equals(UnruffledMod.TELEPORTATION_EFFECT)) return;
        if (!this.canHaveStatusEffect(effect)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
        StatusEffectInstance previousEffect = this.activeStatusEffects.get(effect.getEffectType());
        if (source == null && this instanceof TeleporterEntity teleporter) {
            teleporter.setTeleporting();
        } else if (source instanceof TeleporterEntity teleporter) {
            if (source == this) {
                teleporter.setTeleporting();
            } else if (Config.INSTANCE.get().mechanicsConfig.canTeleportMobs() && this.canStartRiding(source)) {
                if (lastTeleporter != null) {
                    lastTeleporter.removeTeleportTarget(this);
                }
                teleporter.addTeleportTarget(this);
                lastTeleporter = teleporter;
            }
        }
        boolean applied = false;
        if (previousEffect == null) {
            this.activeStatusEffects.put(effect.getEffectType(), effect);
            this.onStatusEffectApplied(effect, source);
            applied = true;
        } else if (previousEffect.upgrade(effect)) {
            this.onStatusEffectUpgraded(effect, false, source);
            applied = true;
        }
        cir.setReturnValue(applied);
        cir.cancel();
    }
}
