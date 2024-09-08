package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
    @Shadow @Nullable private List<Entity> piercingKilledEntities;

    @Shadow protected abstract ItemStack asItemStack();


    @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;isShotFromCrossbow()Z", shift = At.Shift.BEFORE, ordinal = 0))
    public void triggerPiercingArrowAdvancement(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (this.piercingKilledEntities != null && this.piercingKilledEntities.size() >= 2 && this.asItemStack().isOf(CustomItems.PIERCING_ARROW) && ((Object) this) instanceof ProjectileEntity projectile && projectile.getOwner() instanceof ServerPlayerEntity player) {
            UnruffledMod.PIERCING_CRITERION.trigger(player);
        }
    }
}
