package io.github.orlouge.unruffled.mixin.hunger;

import com.mojang.authlib.GameProfile;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    private BlockPos lastSprintBlockPos = new BlockPos(0, 0, 0);
    private boolean isOnPath = false;

    @Redirect(method = "swingHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;resetLastAttackedTicks()V"))
    public void onSwingHandAttackCooldown(ServerPlayerEntity player) {
        if (player.getHungerManager() instanceof ExtendedHungerManager ext) {
            if (!ExtendedHungerManager.canAttack(player, ext.getStamina())) return;
        }
        player.resetLastAttackedTicks();
    }


    @ModifyConstant(method = "increaseTravelMotionStats", constant = @Constant(floatValue = 0.01f, ordinal = 0))
    public float increaseSwimmingExhaustion(float constant) {
        return constant * 4f;
    }

    @ModifyConstant(method = "increaseTravelMotionStats", constant = @Constant(floatValue = 0.1f, ordinal = 0))
    public float decreaseSprintingExhaustionOnPaths(float constant) {
        if (!this.lastSprintBlockPos.equals(this.getBlockPos())) {
            this.lastSprintBlockPos = this.getBlockPos();
            if (Config.INSTANCE.get().hungerConfig.steadyBlockBlacklist()) {
                this.isOnPath = !this.getServerWorld().getBlockState(this.getVelocityAffectingPos()).isIn(UnruffledMod.UNSTEADY);
            } else {
                this.isOnPath = this.getServerWorld().getBlockState(this.getVelocityAffectingPos()).isIn(UnruffledMod.STEADY);
            }
        }
        return this.isOnPath ? constant * 0.5f : constant;
    }
}
