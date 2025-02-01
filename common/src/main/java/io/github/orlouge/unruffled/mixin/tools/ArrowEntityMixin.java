package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin extends PersistentProjectileEntity {
    protected ArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initColor", at = @At("TAIL"))
    public void initSpecialArrows(CallbackInfo ci) {
        if (this.getItemStack().isOf(CustomItems.PIERCING_ARROW)) {
            this.setPierceLevel((byte) 5);
        } else if (this.getItemStack().isOf(CustomItems.IGNITING_ARROW)) {
            this.setOnFireFor(100);
        }
    }
}
