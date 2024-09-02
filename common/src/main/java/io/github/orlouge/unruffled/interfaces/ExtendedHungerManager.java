package io.github.orlouge.unruffled.interfaces;

import io.github.orlouge.unruffled.Config;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

public interface ExtendedHungerManager {
    default float getAttackExhaustion(PlayerEntity player, float cooldownProgress) {
        double attackSpeed = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
        cooldownProgress *= cooldownProgress;
        return (float) Math.min(6f, Math.max(0.02, Config.INSTANCE.get().hungerConfig.attackExhaustionFactor() *  20 * getStaminaRegenerationRate(20f) / (cooldownProgress * attackSpeed * getStaminaDepletionRate())));
    }

    static boolean canAttack(PlayerEntity player, float stamina) {
        if (player.getHungerManager() instanceof ExtendedHungerManager ext) {
            float cooldown = player.getAttackCooldownProgress(0.5f);
            return (cooldown >= 1f && Config.INSTANCE.get().hungerConfig.attackAlwaysAllowInTime()) || stamina > ext.getAttackExhaustion(player, cooldown) * ext.getStaminaDepletionRate();
        }
        return true;
    }

    default float getStaminaRegenerationRate(float foodLevel) {
        return Math.max(2, Math.min(10 / this.getWeight(), Math.min(foodLevel + 2, 10))) * Config.INSTANCE.get().hungerConfig.staminaRegenerationRate();
    }

    float getStamina();

    default float getStaminaDepletionRate() {
        return Config.INSTANCE.get().hungerConfig.staminaDepletionRate();
    }

    default float getStaminaHungerMultiplier(Vec3d travelAmount) {
        float travelPenalty = Math.min((float) travelAmount.length() * Config.INSTANCE.get().hungerConfig.travelPenaltyFactor(), Config.INSTANCE.get().hungerConfig.maxTravelPenalty());
        return Config.INSTANCE.get().hungerConfig.hungerDepletionRate() + travelPenalty;
    }

    void addStamina(float diff);

    default void addStaminaIfCanAttack(float diff, PlayerEntity player) {
        if (diff >= 0) {
            addStamina(diff);
            return;
        }
        float allowedSubtraction = Math.min(0f, minStaminaToAttack(player) - this.getStamina());
        addStamina(Math.max(diff, allowedSubtraction));
    }

    default float minStaminaToAttack(PlayerEntity player) {
        return getAttackExhaustion(player, 1f) * getStaminaDepletionRate();
    }

    float getWeight();


    default float calculateWeight(PlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        Pair<Float, Boolean> inventoryWeight = getInventoryWeight(inv);
        float weight = inventoryWeight.getLeft() / inv.size();
        if (inventoryWeight.getRight()) {
            Inventory inv2 = player.getEnderChestInventory();
            weight += getInventoryWeight(inv2).getLeft() / inv2.size();
        }
        return Math.max(1f, weight * weight * Config.INSTANCE.get().hungerConfig.inventoryWeightPenaltyFactor());
    }

    private static Pair<Float, Boolean> getInventoryWeight(Inventory inventory) {
        float weight = 0;
        boolean hasContainers = false, hasEnderChest = false;
        for(int j = 0; j < inventory.size(); ++j) {
            ItemStack item = inventory.getStack(j);
            Pair<Float, Boolean> itemWeight = getItemWeight(item);
            weight += itemWeight.getLeft();
            if (itemWeight.getLeft() > 1) hasContainers = true;
            hasEnderChest |= itemWeight.getRight();
        }
        return new Pair<>(hasContainers ? weight : 0f, hasEnderChest);
    }

    private static Pair<Float, Boolean> getNbtWeight(NbtCompound nbt) {
        float weight = 0;
        boolean hasEnderChest = false;
        for (String key : nbt.getKeys()) {
            if (key.equals("Items") && nbt.getType("Items") == NbtElement.LIST_TYPE) {
                NbtList itemList = nbt.getList("Items", 10);
                for (NbtElement el : itemList) {
                    if (el.getType() != NbtElement.COMPOUND_TYPE) continue;
                    ItemStack itemStack = ItemStack.fromNbt((NbtCompound) el);
                    if (itemStack == null) continue;
                    Pair<Float, Boolean> subWeight = getItemWeight(itemStack);
                    weight += subWeight.getLeft();
                    hasEnderChest |= subWeight.getRight();
                }
            } else {
                NbtElement sub = nbt.get(key);
                if (sub != null && sub.getType() == NbtElement.COMPOUND_TYPE) {
                    Pair<Float, Boolean> subWeight = getNbtWeight((NbtCompound) sub);
                    weight += subWeight.getLeft();
                    hasEnderChest |= subWeight.getRight();
                }
            }
        }
        return new Pair<>(weight, hasEnderChest);
    }

    private static Pair<Float, Boolean> getItemWeight(ItemStack itemStack) {
        boolean hasEnderChest = itemStack.isOf(Items.ENDER_CHEST);
        float weight = (float) itemStack.getCount() / itemStack.getMaxCount();
        if (itemStack.hasNbt()) {
            Pair<Float, Boolean> containerWeight = getNbtWeight(itemStack.getNbt());
            if (containerWeight.getLeft() > 0) {
                weight += containerWeight.getLeft() + 1;
            }
            hasEnderChest |= containerWeight.getRight();
        }
        return new Pair<>(weight, hasEnderChest);
    }
}
