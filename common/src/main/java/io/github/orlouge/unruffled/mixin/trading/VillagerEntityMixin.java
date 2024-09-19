package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.Config;
import io.github.orlouge.unruffled.Trades;
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
import java.util.List;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract VillagerData getVillagerData();

    @Inject(method = "fillRecipes", at = @At(value = "HEAD"), cancellable = true)
    public void modifyOffers(CallbackInfo ci) {
        VillagerData villagerData = this.getVillagerData();
        Config.INSTANCE.get().tradesConfig.villagerTrades().ifPresent(trades -> {
            Trades.ConfiguredVillagerTrades villagerTrades = trades.get(villagerData.getProfession());
            if (villagerTrades != null && villagerTrades.enabled() && villagerTrades.pools().length >= villagerData.getLevel()) {
                Trades.ConfiguredVillagerPool pool = villagerTrades.pools()[villagerData.getLevel() - 1];
                this.fillRecipesFromPool(this.getOffers(), Arrays.stream(pool.trades()).map(
                    Trades.ConfiguredTrade::toFactory
                ).toArray(TradeOffers.Factory[]::new), pool.count());
                ci.cancel();;
            }
        });
    }
}
