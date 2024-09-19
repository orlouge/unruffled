package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.Config;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderManager.class)
public class WanderingTraderManagerMixin {
    @Shadow @Final private ServerWorldProperties properties;

    @Shadow private int spawnChance;

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 25))
    public int decreaseMinimumChanceAndIncrement(int constant) {
        return Config.INSTANCE.get().mechanicsConfig.wanderingSpawnFrequency();
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 75))
    public int increaseMaximumChance(int constant) {
        return 99;
    }

    @Inject(method = "spawn", at = @At("RETURN"))
    public void alwaysSaveSpawnChance(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() > 0) this.properties.setWanderingTraderSpawnChance(this.spawnChance);
    }

    @ModifyConstant(method = "trySpawn", constant = @Constant(intValue = 10))
    public int alwaysAttemptSpawn(int constant) {
        return 1;
    }

    @ModifyConstant(method = "trySpawn", constant = @Constant(intValue = 48, ordinal = 2))
    public int decreaseSpawnRange(int constant) {
        return 32;
    }

    @ModifyConstant(method = "getNearbySpawnPos", constant = @Constant(intValue = 10))
    public int increaseSpawnPosAttempts(int attempts) {
        return 100;
    }
}
