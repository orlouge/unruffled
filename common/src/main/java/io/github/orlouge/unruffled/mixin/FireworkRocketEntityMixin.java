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
            Vec3d direction = this.shooter.getRotationVector();
            double ydir = 0.4 * Math.max(0, direction.y);
            if (Math.abs(direction.x) > 0.01) {
                factor = Math.min(factor, ydir / Math.abs(direction.x));
            }
            if (Math.abs(direction.z) > 0.01) {
                factor = Math.min(factor, ydir / Math.abs(direction.z));
            }
            factor = Math.max(0.08, factor * factor);
        }
        return playerVelocity.add(x * factor, y * factor, z * factor);
    }
}
