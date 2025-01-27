package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.SweepingEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @ModifyVariable(method = "set", at = @At("LOAD"), argsOnly = true)
    private static Map<Enchantment, Integer> removeDisabledEnchantments(Map<Enchantment, Integer> enchantments, Map<Enchantment, Integer> enchantments2, ItemStack stack) {
        Map<Enchantment, Integer> newEnchantments = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (!ItemEnchantmentsHelper.isDisabled(entry.getKey(), stack)) {
                newEnchantments.put(entry.getKey(), entry.getValue());
            }
        }
        return newEnchantments;
    }

    @Inject(method = "getLure", at = @At("HEAD"), cancellable = true)
    private static void setLureIfDisabled(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.LURE)) {
            if (Enchantments.LURE.isAcceptableItem(stack)) {
                cir.setReturnValue(3);
            }
            cir.cancel();
        }
    }

    @Inject(method = "getSweepingMultiplier", at = @At("RETURN"), cancellable = true)
    private static void sweepingEdgeIfMovingHead(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof PlayerEntity playerEntity) {
            float originalMultiplier = cir.getReturnValueF();
            float newMultiplier = SweepingEnchantment.getMultiplier(Math.min(3, (2 + (int) Math.abs(playerEntity.headYaw - playerEntity.prevHeadYaw)) / 10));
            cir.setReturnValue(Math.max(originalMultiplier, newMultiplier));
        }
    }

    @ModifyVariable(method = "getProtectionAmount", at = @At("STORE"))
    private static MutableInt replaceProtectionEnchantments(MutableInt protection, Iterable<ItemStack> equipment, DamageSource source) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.FIRE_PROTECTION)) {
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
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.BLAST_PROTECTION)) {
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
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.FEATHER_FALLING)) {
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
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.PROTECTION)) {
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
