package io.github.orlouge.unruffled.mixin.trading;

import io.github.orlouge.unruffled.Trades;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "fillRecipes", at = @At("TAIL"))
    public void addOffersOnFill(CallbackInfo ci) {
        if (this.offers != null) {
            this.fillRecipesFromPool(this.offers, Trades.WANDERING_TRADER_DECORATION, 2);
            this.fillRecipesFromPool(this.offers, Trades.WANDERING_TRADER_GLAZED_TERRACOTTA, 1);
            this.fillRecipesFromPool(this.offers, Trades.WANDERING_TRADER_ASSORTED, 4);
            this.fillRecipesFromPool(this.offers, Trades.WANDERING_TRADER_POTIONS, 1);
            this.fillRecipesFromPool(this.offers, Trades.WANDERING_TRADER_BUY, 1);
            this.fillRecipesFromPool(this.offers, Trades.WANDERING_TRADER_CODEX, 1);
        }
    }
}
