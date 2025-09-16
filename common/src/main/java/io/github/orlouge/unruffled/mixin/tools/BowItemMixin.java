package io.github.orlouge.unruffled.mixin.tools;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.orlouge.unruffled.config.Config;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RangedWeaponItem.class)
public class BowItemMixin {
    @ModifyExpressionValue(method = "shootAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/entity/projectile/ProjectileEntity;"))
    public ProjectileEntity modifyArrowProperties(ProjectileEntity entity, ServerWorld world, LivingEntity shooter, Hand hand, ItemStack weapon) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.POWER) && weapon.isIn(ItemTags.BOW_ENCHANTABLE) && entity instanceof PersistentProjectileEntity arrowEntity) {
            arrowEntity.setDamage(arrowEntity.damage + 2.);
        }
        return entity;
    }
}
