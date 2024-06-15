package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
public class BowItemMixin {
    @ModifyArg(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    public Entity modifyArrowProperties(Entity par1) {
        PersistentProjectileEntity arrowEntity = (PersistentProjectileEntity) par1;
        if (UnruffledMod.DISABLED_ENCHANTMENTS.contains(Enchantments.POWER)) {
            arrowEntity.setDamage(arrowEntity.getDamage() + 2.);
        }
        return arrowEntity;
    }
}
