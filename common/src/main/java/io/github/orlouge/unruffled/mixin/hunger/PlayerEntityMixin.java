package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private float attackExhaustion;
    private boolean isOnPath = false;
    private BlockPos lastSprintBlockPos = new BlockPos(0, 0, 0);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract void addExhaustion(float exhaustion);

    @Shadow
    public abstract HungerManager getHungerManager();

    @Shadow
    public abstract float getAttackCooldownProgress(float baseTime);

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void onAttackHead(Entity target, CallbackInfo ci) {
        attackExhaustion = 0.1f;
        if (this.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            attackExhaustion = getAttackExhaustion((PlayerEntity) (Object) this);
            float stamina;
            if (this.getWorld().isClient) {
                stamina = UnruffledModClient.stamina;
            } else {
                stamina = extendedHungerManager.getStamina();
            }
            if (this.getAttackCooldownProgress(0.5f) < 1f && stamina < attackExhaustion * extendedHungerManager.getStaminaDepletionRate()) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"))
    public void onAttackExhaustion(PlayerEntity instance, float exhaustion) {
        if (((Object) this) instanceof ServerPlayerEntity serverPlayer && this.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            float stamina = extendedHungerManager.getStamina();
            if (stamina > 0.35f && stamina - attackExhaustion * extendedHungerManager.getStaminaDepletionRate() < 0.1f) {
                UnruffledMod.FAST_ATTACK_CRITERION.trigger(serverPlayer);
            }
        }
        instance.addExhaustion(attackExhaustion);
        // instance.addExhaustion(getAttackExhaustion(instance));
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F"))
    public float onAttackCooldownPenalty(PlayerEntity instance, float baseTime) {
        return 1;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V", ordinal = 0), index = 0)
    public double increaseKnockbackIfSprintJumping(double strength) {
        if (this.isSprinting() && this.fallDistance <= 1F && !this.isOnGround() && !this.isClimbing() && !this.isTouchingWater() && !this.hasVehicle()) {
            if (((Object) this) instanceof ServerPlayerEntity serverPlayer) {
                UnruffledMod.KNOCKBACK_CRITERION.trigger(serverPlayer);
            }
            return strength + 0.5;
        } else {
            return strength;
        }
    }

    @ModifyConstant(method = "increaseTravelMotionStats", constant = @Constant(floatValue = 0.01f, ordinal = 0))
    public float increaseSwimmingExhaustion(float constant) {
        return constant * 4f;
    }

    @ModifyConstant(method = "increaseTravelMotionStats", constant = @Constant(floatValue = 0.1f, ordinal = 0))
    public float decreaseSprintingExhaustionOnPaths(float constant) {
        if (!this.lastSprintBlockPos.equals(this.getBlockPos())) {
            this.lastSprintBlockPos = this.getBlockPos();
            this.isOnPath = this.getWorld().getBlockState(this.getVelocityAffectingPos()).isIn(UnruffledMod.STEADY);
        }
        return this.isOnPath ? constant * 0.5f : constant;
    }


    private static float getAttackExhaustion(PlayerEntity player) {
        if (player.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            return extendedHungerManager.getAttackExhaustion(player, player.getAttackCooldownProgress(0.5f));
        } else {
            return 0.05f;
        }
    }

    @Inject(method = "addExperience", at = @At("HEAD"))
    public void experienceToStamina(int experience, CallbackInfo ci) {
        if (this.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            extendedHungerManager.addStamina((float) experience / 10f);
        }
    }

    @ModifyConstant(method = "updateTurtleHelmet", constant = @Constant(intValue = 200))
    public int increaseTurtleHelmetEffectTime(int time) {
        return time * 6;
    }

    @Redirect(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAquaAffinity(Lnet/minecraft/entity/LivingEntity;)Z"))
    public boolean waterBreathingIsAquaAffinity(LivingEntity entity) {
        if (EnchantmentHelper.hasAquaAffinity(entity)) return true;
        if (StatusEffectUtil.hasWaterBreathing(entity)) {
            if (entity instanceof ServerPlayerEntity player) {
                UnruffledMod.AQUA_AFFINITY_CRITERION.trigger(player);
            }
            return true;
        }
        return false;
    }

    @Inject(method = "wakeUp", at = @At("HEAD"))
    public void resetWearinessOnWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
        if (this.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            extendedHungerManager.resetWeariness();
        }
    }

    @Inject(method = "damageShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    public void consumeStaminaOnShieldHit(float amount, CallbackInfo ci) {
        if (this.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            extendedHungerManager.addStamina(Math.min(0.33f, -amount / 30f));
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setHealth(F)V"))
    public void addWearinessOnDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (this.getHungerManager() instanceof ExtendedHungerManager extendedHungerManager) {
            extendedHungerManager.addWeariness(amount / 1000f);
        }
    }
}