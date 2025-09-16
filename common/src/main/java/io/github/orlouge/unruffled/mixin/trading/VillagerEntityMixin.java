package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.config.Trades;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract VillagerData getVillagerData();

    @Inject(method = "fillRecipes", at = @At(value = "HEAD"), cancellable = true)
    public void modifyOffers(CallbackInfo ci) {
        VillagerData villagerData = this.getVillagerData();
        io.github.orlouge.unruffled.config.Config.INSTANCE.get().tradesConfig.villagerTrades().ifPresent(trades -> {
            Trades.ConfiguredVillagerTrades villagerTrades = trades.get(villagerData.profession().getKey().get());
            if (villagerTrades != null && villagerTrades.enabled().orElse(true) && villagerTrades.pools().length >= villagerData.level()) {
                Trades.ConfiguredVillagerPool pool = villagerTrades.pools()[villagerData.level() - 1];
                this.fillRecipesFromPool(this.getOffers(), Arrays.stream(pool.trades()).map(
                    trade -> trade.toFactory(this.getWorld().getRegistryManager())
                ).toArray(TradeOffers.Factory[]::new), pool.count());
                ci.cancel();;
            }
        });
    }
}
