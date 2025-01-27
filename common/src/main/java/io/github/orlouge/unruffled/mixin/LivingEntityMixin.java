package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow protected abstract boolean isOnSoulSpeedBlock();

    @Shadow @Nullable public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute attribute);

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.9900000095367432))
    public double decreaseHorizontalElytraSpeed(double speed) {
        return speed * Config.INSTANCE.get().elytraConfig.horizontalGlidingSpeedFactor();
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.9800000190734863, ordinal = 0))
    public double decreaseVerticalElytraSpeed(double speed) {
        return speed * Config.INSTANCE.get().elytraConfig.verticalGlidingSpeedFactor();
    }

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    public void disableTotemAttempt(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.get().mechanicsConfig.disableTotemOfUndying()) cir.cancel();
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDepthStrider(Lnet/minecraft/entity/LivingEntity;)I"))
    public int increaseSwimmingSpeedIfWaterBreathing(LivingEntity entity) {
        return Math.max(StatusEffectUtil.hasWaterBreathing(entity) ? 3 : 0, EnchantmentHelper.getDepthStrider(entity));
    }

    @Inject(method = "addSoulSpeedBoostIfNeeded", at = @At("HEAD"), cancellable = true)
    public void addSoulSpeedBoostWithNetherite(CallbackInfo ci) {
        if (!this.getLandingBlockState().isAir() && this.getEquippedStack(EquipmentSlot.FEET).isOf(Items.NETHERITE_BOOTS) && this.isOnSoulSpeedBlock()) {
            EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (entityAttributeInstance == null) {
                return;
            }
            entityAttributeInstance.addTemporaryModifier(new EntityAttributeModifier(LivingEntity.SOUL_SPEED_BOOST_ID, "Soul speed boost", 0.0615f, EntityAttributeModifier.Operation.ADDITION));
            ci.cancel();
        }
    }

    @Inject(method = "getVelocityMultiplier", at = @At("HEAD"), cancellable = true)
    public void setVelocityMultiplierWithNetheriteBoots(CallbackInfoReturnable<Float> cir) {
        if (this.isOnSoulSpeedBlock() && this.getEquippedStack(EquipmentSlot.FEET).isOf(Items.NETHERITE_BOOTS)) {
            cir.setReturnValue(1f);
            cir.cancel();
        }
    }
}
