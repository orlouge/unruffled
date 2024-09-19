package io.github.orlouge.unruffled.mixin.mobs;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EvokerEntity.class)
public abstract class EvokerEntityMixin extends SpellcastingIllagerEntity {
    private boolean raidSpawned = false;

    protected EvokerEntityMixin(EntityType<? extends SpellcastingIllagerEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!this.raidSpawned && this.getRaid() == null && damageSource.getAttacker() instanceof PlayerEntity playerEntity) {
            if (Config.INSTANCE.get().mechanicsConfig.badOmenFromEvoker()) {
                StatusEffectInstance playerBadOmen = playerEntity.getStatusEffect(StatusEffects.BAD_OMEN);
                int level = 1;
                if (playerBadOmen != null) {
                    level += playerBadOmen.getAmplifier();
                    playerEntity.removeStatusEffectInternal(StatusEffects.BAD_OMEN);
                } else {
                    --level;
                }

                level = MathHelper.clamp(level, 0, 4);
                StatusEffectInstance newPlayerBadOmen = new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, level, false, false, true);
                if (!this.getWorld().getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
                    playerEntity.addStatusEffect(newPlayerBadOmen);
                }
            }
            if (Config.INSTANCE.get().mechanicsConfig.evokerDropsEvilTotem()) {
                this.dropItem(CustomItems.EVIL_TOTEM);
            }
        }

        super.onDeath(damageSource);
    }

    @Override
    public void setRaid(@Nullable Raid raid) {
        this.raidSpawned = true;
        super.setRaid(raid);
    }
}
