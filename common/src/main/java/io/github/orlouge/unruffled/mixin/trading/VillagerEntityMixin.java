package io.github.orlouge.unruffled.mixin.trading;

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
        List<TradeOffers.Factory[]> offersByLevel = Trades.VILLAGER_TRADES.get(villagerData.getProfession());
        if (offersByLevel != null && offersByLevel.size() > villagerData.getLevel()) {
            this.fillRecipesFromPool(this.getOffers(), offersByLevel.get(villagerData.getLevel()), 2);
            ci.cancel();
        }
    }
}
