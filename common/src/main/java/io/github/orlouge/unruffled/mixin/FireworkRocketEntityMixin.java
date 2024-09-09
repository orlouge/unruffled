package io.github.orlouge.unruffled.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {
    /*
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d shooterAlwaysUpwards(LivingEntity instance) {
        return new Vec3d(0, 1, 0);
    }
     */

    @Shadow @Nullable private LivingEntity shooter;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
    public Vec3d slowHorizontalRocket(Vec3d playerVelocity, double x, double y, double z) {
        double factor = 1;
        if (this.shooter != null) {
            Vec3d direction = this.shooter.getRotationVector().normalize();
            factor = 0.5 + direction.getY() * 0.5;
            factor = Math.min(1, Math.pow(factor, 5) + 0.04);
        }
        return playerVelocity.add(x * factor, y * factor, z * factor);
    }
}
