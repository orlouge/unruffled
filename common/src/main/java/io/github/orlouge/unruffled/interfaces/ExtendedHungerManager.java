package io.github.orlouge.unruffled.interfaces;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;

public interface ExtendedHungerManager {
    default float getAttackExhaustion(PlayerEntity player, float cooldownProgress) {
        double attackSpeed = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
        cooldownProgress *= cooldownProgress;
        return (float) Math.min(6f, Math.max(0.02, 20 * getStaminaRegenerationRate(20f) / (cooldownProgress * attackSpeed * getStaminaDepletionRate())));
    }

    static boolean canAttack(PlayerEntity player, float stamina) {
        if (player.getHungerManager() instanceof ExtendedHungerManager ext) {
            return stamina > ext.getAttackExhaustion(player, player.getAttackCooldownProgress(0.5f)) * ext.getStaminaDepletionRate();
        }
        return true;
    }

    default float getStaminaRegenerationRate(float foodLevel) {
        return Math.max(2, Math.min(10 / this.getWeight(), Math.min(foodLevel + 2, 10))) * 0.00028f;
    }

    float getStamina();

    default float getStaminaDepletionRate() {
        return 0.5f;
    }

    default float getStaminaHungerMultiplier(Vec3d travelAmount) {
        float travelPenalty = Math.min((float) travelAmount.length() / 300, 2.8f);
        return 0.2f + travelPenalty;
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
        float weight = getInventoryWeight(inv) / inv.size();
        return Math.max(1f, weight * weight);
    }

    private static float getInventoryWeight(PlayerInventory inventory) {
        float weight = 0;
        boolean hasContainers = false;
        for(int j = 0; j < inventory.size(); ++j) {
            float itemWeight = getItemWeight(inventory.getStack(j));
            weight += itemWeight;
            if (itemWeight > 1) hasContainers = true;
        }
        return hasContainers ? weight : 0f;
    }

    private static float getNbtWeight(NbtCompound nbt) {
        float weight = 0;
        for (String key : nbt.getKeys()) {
            if (key.equals("Items") && nbt.getType("Items") == NbtElement.LIST_TYPE) {
                NbtList itemList = nbt.getList("Items", 10);
                for (NbtElement el : itemList) {
                    if (el.getType() != NbtElement.COMPOUND_TYPE) continue;
                    ItemStack itemStack = ItemStack.fromNbt((NbtCompound) el);
                    if (itemStack == null) continue;
                    weight += getItemWeight(itemStack);
                }
            } else {
                NbtElement sub = nbt.get(key);
                if (sub != null && sub.getType() == NbtElement.COMPOUND_TYPE) {
                    weight += getNbtWeight((NbtCompound) sub);
                }
            }
        }
        return weight;
    }

    private static float getItemWeight(ItemStack itemStack) {
        float weight = (float) itemStack.getCount() / itemStack.getMaxCount();
        if (itemStack.hasNbt()) {
            float containerWeight = getNbtWeight(itemStack.getNbt());
            if (containerWeight > 0) {
                weight += containerWeight + 1;
            }
        }
        return weight;
    }
}
