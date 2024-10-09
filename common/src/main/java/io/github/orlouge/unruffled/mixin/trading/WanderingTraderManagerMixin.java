package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.interfaces.WanderingTraderManagerTracker;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderManager.class)
public class WanderingTraderManagerMixin {
    @Shadow @Final private ServerWorldProperties properties;

    @Shadow private int spawnChance;

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 25))
    public int decreaseMinimumChanceAndIncrement(int constant, ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        return Config.INSTANCE.get().mechanicsConfig.wanderingSpawnFrequency() * (1 + (int) (4 / (1 + world.getTime() / 200000)));
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

    /*
    @ModifyConstant(method = "trySpawn", constant = @Constant(intValue = 48, ordinal = 1))
    public int decreaseSpawnRange(int constant) {
        return 32;
    }
     */

    @ModifyConstant(method = "getNearbySpawnPos", constant = @Constant(intValue = 10))
    public int increaseSpawnPosAttempts(int attempts) {
        return 100;
    }

    @ModifyVariable(method = "trySpawn", at = @At("STORE"))
    public WanderingTraderEntity trackSpawnManager(WanderingTraderEntity trader) {
        if (trader instanceof WanderingTraderManagerTracker tracker && (Object) this instanceof WanderingTraderManager manager) {
            tracker.setManager(manager);
        }
        return trader;
    }
}
