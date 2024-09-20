package io.github.orlouge.unruffled.mixin.hunger;

import io.github.orlouge.unruffled.Config;
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
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin implements ExtendedHungerManager {
    @Shadow private int foodLevel;
    @Shadow private int foodTickTimer;
    @Shadow private int prevFoodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustion;

    @Shadow public abstract void add(int food, float saturationModifier);

    private int inventoryWeightTimer = 100;
    private float inventoryWeight = 1f;
    private float stamina = 1f;
    private float lastStamina = 0f;
    private float lastStaminaRegeneration = 1f;
    private float baseWeariness = 0f;
    private float amortizedWeariness = 0f;
    private float foodToConsume = 0f;
    private float foodCooldown = 0f;
    private Vec3d averagePos = null;
    private DimensionType dimensionType = null;
    private boolean isUnderground = false, isNight = false;
    private float health = 20f;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void onUpdate(PlayerEntity player, CallbackInfo ci) {
        Vec3d playerPos = player.getPos();

        if (player.getVehicle() != null && new Vec3d(player.getVelocity().getX(), 0, player.getVelocity().getZ()).length() < 0.01f) {
            baseWeariness = baseWeariness * 0.999f;
        } else {
            baseWeariness = baseWeariness * 0.9998f;
        }

        DimensionType currentDimension = player.getWorld().getDimension();
        if (currentDimension != dimensionType) {
            averagePos = playerPos;
        }
        dimensionType = currentDimension;
        health = player.getHealth();
        isNight = player.getWorld().isNight();
        isUnderground = player.getPos().getY() < player.getWorld().getSeaLevel() * 0.9 || player.isSubmergedInWater();
        float targetWeariness = this.getTargetWeariness();
        amortizedWeariness = Math.min(1f, 0.997f * amortizedWeariness + 0.003f * targetWeariness);

        foodCooldown = Math.max(0f, foodCooldown - 0.001f);
        Vec3d travelAmount = new Vec3d(0, 0, 0);
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
            if (this.stamina > Math.max(this.minStaminaToAttack(player), this.getStaminaRegenerationRate(1) * 2)) {
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
            float consumedStamina = this.exhaustion * this.getStaminaDepletionRate();
            this.foodToConsume += Config.INSTANCE.get().hungerConfig.hungerDepletionRate() * Math.max(0f, consumedStamina - stamina) / this.getStaminaDepletionRate();
            this.exhaustion = 0;
            this.stamina = Math.max(0f, stamina - consumedStamina);
            this.addWeariness(consumedStamina * (Config.INSTANCE.get().hungerConfig.wearinessIncreaseFactor() * (0.1f + this.amortizedWeariness)));
        }
        if (this.stamina > 0.05 && player.getHealth() < player.getMaxHealth() && player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 200 / Math.min(this.stamina, 0.4)) {
                player.heal(1f);
                this.foodTickTimer = 0;
            }
        }
        float staminaRegeneration = this.getStaminaRegenerationRate(this.amortizedWeariness);
        //System.out.println("W " + baseWeariness + " " + amortizedWeariness + " " + targetWeariness + " " + staminaRegeneration);
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
            //this.addWeariness((restoredStamina - stamina) * (0.01f + 0.14f * this.amortizedWeariness));
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
        if ((this.stamina != this.lastStamina || Math.abs(staminaRegeneration - this.lastStaminaRegeneration) > 0.0001f) && player instanceof ServerPlayerEntity serverPlayer) {
            new Packets.ExtendedHungerUpdate(
                    this.stamina,
                    staminaRegeneration / Config.INSTANCE.get().hungerConfig.staminaRegenerationRate(),
                    (staminaHungerMultiplier - 0.2f) / 3.8f
            ).sendToPlayer(serverPlayer);
            this.lastStamina = this.stamina;
            this.lastStaminaRegeneration = staminaRegeneration;
        }
        ci.cancel();
    }

    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
    public void addFoodWithCooldown(HungerManager instance, int food, float saturationModifier) {
        food = Math.round(food * (1f - foodCooldown));
        this.add(food, Math.round(saturationModifier * (1f - foodCooldown)));
        if (food > 0) {
            foodCooldown = Math.min(0.9f, foodCooldown + Config.INSTANCE.get().hungerConfig.eatCooldownFactor());
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("stamina", stamina);
        nbt.putFloat("baseWeariness", baseWeariness);
        nbt.putFloat("amortizedWeariness", amortizedWeariness);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    public void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("stamina")) {
            this.stamina = nbt.getFloat("stamina");
        }
        if (nbt.contains("baseWeariness")) {
            this.baseWeariness = nbt.getFloat("baseWeariness");
        }
        if (nbt.contains("amortizedWeariness")) {
            this.amortizedWeariness = nbt.getFloat("amortizedWeariness");
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


    @Override
    public DimensionType getDimension() {
        return this.dimensionType;
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    @Override
    public float getFoodLevel() {
        return this.foodLevel;
    }

    @Override
    public float getBaseWeariness() {
        return this.baseWeariness;
    }

    @Override
    public float getAmortizedWeariness() {
        return this.amortizedWeariness;
    }

    @Override
    public void addWeariness(float diff) {
        this.baseWeariness = Math.max(0, Math.min(1, this.baseWeariness + diff));
    }

    @Override
    public void resetWeariness() {
        this.baseWeariness = 0;
        this.amortizedWeariness = 0;
    }

    @Override
    public boolean isNight() {
        return isNight;
    }

    @Override
    public boolean isUnderground() {
        return isUnderground;
    }
}
