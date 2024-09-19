package io.github.orlouge.unruffled.mixin.tools;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrownedEntity.class)
public class DrownedEntityMixin extends ZombieEntity {
    public DrownedEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "initEquipment", constant = @Constant(doubleValue = 0.9))
    public double decreaseTridentChance(double chance) {
        return chance + 0.05;
    }

    @Inject(method = "initEquipment", at = @At("TAIL"))
    public void updateDropChancesOnInitEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        this.updateDropChances(EquipmentSlot.MAINHAND);
    }
}
