package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.Trades;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "fillRecipes", at = @At("TAIL"))
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
}
