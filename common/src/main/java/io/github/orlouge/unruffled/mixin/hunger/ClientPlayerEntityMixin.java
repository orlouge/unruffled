package io.github.orlouge.unruffled.mixin.hunger;

import com.mojang.authlib.GameProfile;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow private boolean lastSprinting;

    @Shadow protected abstract boolean canSprint();

    private float sprintingCooldown = 0f;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "canSprint", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;getFoodLevel()I"))
    public int redirectGetFoodLevel(HungerManager instance) {
        return (instance instanceof ExtendedHungerManager ext) ? (int) (UnruffledModClient.stamina * 9 / (0.1 * ext.getStaminaDepletionRate())) : instance.getFoodLevel();
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;canSprint()Z"))
    public boolean redirectCanSprint(ClientPlayerEntity instance) {
        return instance.canSprint() && this.sprintingCooldown <= 0;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickHead(CallbackInfo ci) {
        if (this.sprintingCooldown > 0) {
            this.sprintingCooldown -= 1f;
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    public void onTickSendSprintingPacket(CallbackInfo ci) {
        if (this.lastSprinting != this.isSprinting()) {
            if (!this.isSprinting() && !this.canSprint()) {
                this.sprintingCooldown = 60f;
            }
        }
    }
}
