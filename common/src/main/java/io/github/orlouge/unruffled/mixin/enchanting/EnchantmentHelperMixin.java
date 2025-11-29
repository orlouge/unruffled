package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @ModifyVariable(method = "set", at = @At(value = "LOAD"))
    private static ItemEnchantmentsComponent removeDisabledEnchantments(ItemEnchantmentsComponent value, ItemStack stack) {
        ItemEnchantmentsComponent.Builder out = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : value.getEnchantmentEntries()) {
            if (entry.getKey().getKey().isEmpty() || !ItemEnchantmentsHelper.isDisabled(entry.getKey().getKey().get(), stack)) {
                out.add(entry.getKey(), entry.getValue());
            }
        }
        return out.build();
    }

    @Inject(method = "getFishingTimeReduction", at = @At("HEAD"), cancellable = true)
    private static void setLureIfDisabled(ServerWorld world, ItemStack stack, Entity user, CallbackInfoReturnable<Float> cir) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.LURE)) {
            cir.setReturnValue(Math.max(15f, cir.getReturnValueF()));
            cir.cancel();
        }
    }

    @Inject(method = "getProtectionAmount", at = @At("RETURN"), cancellable = true)
    private static void replaceProtectionEnchantments(ServerWorld world, LivingEntity user, DamageSource source, CallbackInfoReturnable<Float> cir) {
        float protection = cir.getReturnValueF();
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.FIRE_PROTECTION)) {
                for (ItemStack item : user.getEquippedItems()) {
                    if (item.getItem() instanceof ArmorItem armor) {
                        if (armor.getMaterial() == ArmorMaterials.DIAMOND) {
                            protection += 4;
                        } else if (armor.getMaterial() == ArmorMaterials.NETHERITE) {
                            protection += 8;
                        }
                    }
                }
            }
        } else if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.BLAST_PROTECTION)) {
                for (ItemStack item : user.getEquippedItems()) {
                    if (item.getItem() instanceof ArmorItem armor) {
                        if (armor.getMaterial() == ArmorMaterials.DIAMOND) {
                            protection += 4;
                        } else if (armor.getMaterial() == ArmorMaterials.NETHERITE) {
                            protection += 6;
                        }
                    }
                }
            }
        } else if (source.isIn(DamageTypeTags.IS_FALL)) {
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.FEATHER_FALLING)) {
                for (ItemStack item : user.getEquippedItems()) {
                    if (item.getItem() instanceof ArmorItem armor && armor.getSlotType() == EquipmentSlot.FEET) {
                        if (armor.getMaterial() == ArmorMaterials.LEATHER) {
                            protection += 16;
                        } else {
                            protection += 10;
                        }
                    }
                }
            }
        } else if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.PROTECTION)) {
                for (ItemStack item : user.getEquippedItems()) {
                    if (item.getItem().getComponents().contains(DataComponentTypes.DAMAGE)) {
                        if (source.isOf(DamageTypes.WITHER) && item.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.NETHERITE) {
                            protection += 6;
                        } else {
                            protection += 2;
                        }
                    }
                }
            }
        }
        cir.setReturnValue(protection);
    }
}
