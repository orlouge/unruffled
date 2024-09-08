package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin extends PersistentProjectileEntity {
    private boolean unruffled_isPiercing = false, unruffled_isIgniting = false;

    protected ArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initFromStack", at = @At("TAIL"))
    public void initSpecialArrows(ItemStack stack, CallbackInfo ci) {
        if (stack.isOf(CustomItems.PIERCING_ARROW)) {
            this.setPierceLevel((byte) 5);
            unruffled_isPiercing = true;
        } else if (stack.isOf(CustomItems.IGNITING_ARROW)) {
            this.setOnFireFor(100);
            unruffled_isIgniting = true;
        }
    }

    @Inject(method = "asItemStack", cancellable = true, at = @At("HEAD"))
    public void dropPiercingAndIgnitingArrows(CallbackInfoReturnable<ItemStack> cir) {
        if (unruffled_isPiercing) {
            cir.setReturnValue(new ItemStack(CustomItems.PIERCING_ARROW));
            cir.cancel();
        } else if (unruffled_isIgniting) {
            cir.setReturnValue(new ItemStack(CustomItems.IGNITING_ARROW));
            cir.cancel();
        }
    }
}
