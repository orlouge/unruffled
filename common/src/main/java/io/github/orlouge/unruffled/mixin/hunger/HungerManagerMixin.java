package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.Packets;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin implements ExtendedHungerManager {
    @Shadow private int foodLevel;
    @Shadow private int foodTickTimer;
    @Shadow private int prevFoodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustion;
    private int inventoryWeightTimer = 100;
    private float inventoryWeight = 1f;
    private float stamina = 1f;
    private float lastStamina = 0f;
    private float lastStaminaRegeneration = 1f;
    private float foodToConsume = 0f;
    private Vec3d averagePos = null;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void onUpdate(PlayerEntity player, CallbackInfo ci) {
        Vec3d playerPos = player.getPos(), travelAmount = new Vec3d(0, 0, 0);
        if (this.averagePos == null) {
            this.averagePos = playerPos;
        } else {
            travelAmount = playerPos.subtract(this.averagePos);
            this.averagePos = playerPos.multiply(0.0002).add(this.averagePos.multiply(0.9998));
        }
        this.prevFoodLevel = this.foodLevel;
        if (this.inventoryWeightTimer++ >= 50) {
            this.inventoryWeight = this.calculateWeight(player);
            this.inventoryWeightTimer = 0;
            if (this.inventoryWeight > 1.5 && player instanceof ServerPlayerEntity serverPlayer) {
                UnruffledMod.HEAVY_INVENTORY_CRITERION.trigger(serverPlayer);
            }
        }
        if (player.hasStatusEffect(StatusEffects.HUNGER)) {
            float hungerExhaustion = player.getStatusEffect(StatusEffects.HUNGER).getAmplifier() * 0.005f;
            if (this.stamina > Math.max(this.minStaminaToAttack(player), this.getStaminaRegenerationRate(20f) * 2)) {
                float exhaustionFraction = hungerExhaustion * this.stamina;
                this.stamina = Math.max(0f, stamina - exhaustionFraction  * this.getStaminaDepletionRate());
                hungerExhaustion -= exhaustionFraction;
            }
            foodToConsume += hungerExhaustion;
            this.exhaustion -= hungerExhaustion;
        }
        float staminaHungerMultiplier = this.getStaminaHungerMultiplier(travelAmount);
        foodToConsume += 0.0001f * Math.max(0f, staminaHungerMultiplier - 0.3f);
        if (this.exhaustion > 0) {
            this.stamina = Math.max(0f, stamina - this.exhaustion * this.getStaminaDepletionRate());
            this.exhaustion = 0;
        }
        if (this.stamina > 0.05 && player.getHealth() < player.getMaxHealth() && player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 200 / Math.min(this.stamina, 0.4)) {
                player.heal(1f);
                this.foodTickTimer = 0;
            }
        }
        float staminaRegeneration = this.getStaminaRegenerationRate(this.foodLevel);
        if (player.hasStatusEffect(StatusEffects.SPEED)) {
            staminaRegeneration *= 1.5f + 0.5f * (float) player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
        }
        if (player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            staminaRegeneration = Math.max(
                    this.getStaminaRegenerationRate(0),
                    staminaRegeneration / (1.5f + 0.5f * (float) player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier())
            );
        }
        if (this.stamina < 1) {
            float restoredStamina = Math.min(1.0f, this.stamina + staminaRegeneration);
            float consumedFood = (restoredStamina - stamina) * staminaHungerMultiplier / (4f * this.getStaminaDepletionRate());
            stamina = restoredStamina;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - consumedFood, 0.0F);
            } else /* if (difficulty != Difficulty.PEACEFUL) */ {
                foodToConsume += consumedFood;
            }
        }
        if (foodToConsume > 1f) {
            this.foodLevel = Math.max(this.foodLevel - (int) foodToConsume, 0);
            foodToConsume = Math.max(0, foodToConsume - (int) foodToConsume);
        }
        if ((this.stamina != this.lastStamina || staminaRegeneration != this.lastStaminaRegeneration) && player instanceof ServerPlayerEntity serverPlayer) {
            new Packets.ExtendedHungerUpdate(
                    this.stamina,
                    staminaRegeneration / 0.0028f,
                    (staminaHungerMultiplier - 0.2f) / 3.8f
            ).sendToPlayer(serverPlayer);
            this.lastStamina = this.stamina;
            this.lastStaminaRegeneration = staminaRegeneration;
        }
        ci.cancel();
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("stamina", stamina);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    public void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("stamina")) {
            this.stamina = nbt.getFloat("stamina");
        }
    }

    /*
    @Inject(method = "addExhaustion", at = @At("RETURN"))
    public void onAddExhaustion(float exhaustion, CallbackInfo ci) {
        this.stamina = Math.max(0f, stamina - exhaustion * this.getStaminaExhaustionMultiplier());
    }
     */

    @Override
    public float getStamina() {
        return stamina;
    }

    @Override
    public float getWeight() {
        return this.inventoryWeight;
    }

    @Override
    public void addStamina(float diff) {
        this.stamina = Math.max(0, Math.min(1, stamina + diff));
    }
}
