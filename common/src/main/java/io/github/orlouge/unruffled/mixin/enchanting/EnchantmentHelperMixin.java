package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @ModifyVariable(method = "set", at = @At("LOAD"), argsOnly = true)
    private static Map<Enchantment, Integer> removeDisabledEnchantments(Map<Enchantment, Integer> enchantments, Map<Enchantment, Integer> enchantments2, ItemStack stack) {
        Map<Enchantment, Integer> newEnchantments = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (!CustomItems.isDisabled(entry.getKey(), stack)) {
                newEnchantments.put(entry.getKey(), entry.getValue());
            }
        }
        return newEnchantments;
    }

    @ModifyVariable(method = "getProtectionAmount", at = @At("STORE"))
    private static MutableInt replaceProtectionEnchantments(MutableInt protection, Iterable<ItemStack> equipment, DamageSource source) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            if (UnruffledMod.DISABLED_ENCHANTMENTS.contains(Enchantments.FIRE_PROTECTION)) {
                for (ItemStack item : equipment) {
                    if (item.getItem() instanceof ArmorItem armor) {
                        if (armor.getMaterial() == ArmorMaterials.DIAMOND) {
                            protection.add(4);
                        } else if (armor.getMaterial() == ArmorMaterials.NETHERITE) {
                            protection.add(8);
                        }
                    }
                }
            }
        } else if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            if (UnruffledMod.DISABLED_ENCHANTMENTS.contains(Enchantments.BLAST_PROTECTION)) {
                for (ItemStack item : equipment) {
                    if (item.getItem() instanceof ArmorItem armor) {
                        if (armor.getMaterial() == ArmorMaterials.DIAMOND) {
                            protection.add(4);
                        } else if (armor.getMaterial() == ArmorMaterials.NETHERITE) {
                            protection.add(6);
                        }
                    }
                }
            }
        } else if (source.isIn(DamageTypeTags.IS_FALL)) {
            if (UnruffledMod.DISABLED_ENCHANTMENTS.contains(Enchantments.FEATHER_FALLING)) {
                for (ItemStack item : equipment) {
                    if (item.getItem() instanceof ArmorItem armor && armor.getSlotType() == EquipmentSlot.FEET) {
                        if (armor.getMaterial() == ArmorMaterials.LEATHER) {
                            protection.add(16);
                        } else {
                            protection.add(10);
                        }
                    }
                }
            }
        } else if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (UnruffledMod.DISABLED_ENCHANTMENTS.contains(Enchantments.PROTECTION)) {
                for (ItemStack item : equipment) {
                    if (item.getItem().isDamageable() && item.getItem().isDamageable()) {
                        if (source.isOf(DamageTypes.WITHER) && item.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.NETHERITE) {
                            protection.add(6);
                        } else {
                            protection.add(2);
                        }
                    }
                }
            }
        }
        return protection;
    }
}
