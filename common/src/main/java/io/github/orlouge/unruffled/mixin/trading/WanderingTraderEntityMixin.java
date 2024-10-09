package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.Trades;
import io.github.orlouge.unruffled.interfaces.WanderingTraderManagerTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity implements WanderingTraderManagerTracker {
    private int unruffled_spawnChanceDiff = 0;
    private float unruffled_spawnChanceIncrease = 0f;
    private WanderingTraderManager unruffled_wanderingTraderManager = null;

    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "afterUsing", at = @At("HEAD"))
    public void increaseSpawnChangeAfterTrading(TradeOffer offer, CallbackInfo ci) {
        if (unruffled_spawnChanceDiff < 30 && this.getWorld() instanceof ServerWorld world && this.unruffled_wanderingTraderManager != null) {
            ServerWorldProperties properties = world.getServer().getSaveProperties().getMainWorldProperties();
            unruffled_spawnChanceIncrease += offer.getMerchantExperience() * 0.8f;
            if (unruffled_spawnChanceIncrease >= 1f) {
                int diff = (int) unruffled_spawnChanceIncrease;
                unruffled_spawnChanceIncrease -= diff;
                unruffled_spawnChanceDiff += diff;
                this.unruffled_wanderingTraderManager.spawnChance = Math.min(99, properties.getWanderingTraderSpawnChance() + diff);
                properties.setWanderingTraderSpawnChance(this.unruffled_wanderingTraderManager.spawnChance);
            }
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (damageSource.getAttacker() instanceof PlayerEntity && this.getWorld() instanceof ServerWorld world && this.unruffled_wanderingTraderManager != null) {
            ServerWorldProperties properties = world.getServer().getSaveProperties().getMainWorldProperties();
            this.unruffled_wanderingTraderManager.spawnChance = 1 + unruffled_spawnChanceDiff;
            properties.setWanderingTraderSpawnChance(this.unruffled_wanderingTraderManager.spawnChance);
            this.unruffled_wanderingTraderManager.spawnDelay = 96000 - unruffled_spawnChanceDiff * 1200;
            properties.setWanderingTraderSpawnDelay(this.unruffled_wanderingTraderManager.spawnDelay);
        }
    }

    @Inject(method = "fillRecipes", at = @At("HEAD"))
    public void addOffersOnFill(CallbackInfo ci) {
        if (this.offers != null) {
            Config.INSTANCE.get().tradesConfig.wanderingTraderTrades().ifPresent(
                trades -> {
                    if (trades.enabled()) {
                        for (Trades.ConfiguredWanderingTraderPool pool : trades.pools()) {
                            this.fillRecipesFromPool(this.offers, Arrays.stream(pool.trades()).map(
                                Trades.ConfiguredTrade::toFactory
                            ).toArray(TradeOffers.Factory[]::new), pool.count());
                        }
                    }
                }
            );
        }
    }

    @Override
    public void setManager(WanderingTraderManager manager) {
        this.unruffled_wanderingTraderManager = manager;
    }

    @Override
    public WanderingTraderManager getManager() {
        return this.unruffled_wanderingTraderManager;
    }
}
