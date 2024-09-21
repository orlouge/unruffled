package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.Config;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin {
    @Redirect(method = "initCustomGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 5))
    public void dontTargetVillagers(GoalSelector instance, int priority, Goal goal) {
        if (!Config.INSTANCE.get().mechanicsConfig.zombiesDontTargetVillagers()) {
            instance.add(priority, goal);
        }
    }
}
