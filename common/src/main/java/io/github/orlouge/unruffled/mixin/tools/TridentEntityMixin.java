package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity {
    @Shadow private ItemStack tridentStack;

    @Shadow protected abstract boolean isOwnerAlive();

    private boolean isOnLightningRod = false;

    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.tridentStack.isOf(Items.TRIDENT)) return;
        BlockState blockState = this.getWorld().getBlockState(blockHitResult.getBlockPos());
        if (blockState != null) {
            if (blockState.isIn(BlockTags.WOOL)) {
                ItemStack newStack = new ItemStack(CustomItems.CHARGED_TRIDENT);
                newStack.setNbt(this.tridentStack.getNbt());
                newStack = ItemEnchantmentsHelper.setItemEnchantments(newStack);
                this.tridentStack = newStack;
                if (this.getOwner() instanceof ServerPlayerEntity player) {
                    UnruffledMod.CHARGED_TRIDENT_CRITERION.trigger(player);
                }
            } else if (blockState.isOf(Blocks.LIGHTNING_ROD)) {
                this.isOnLightningRod = true;
            }
        }
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        super.onStruckByLightning(world, lightning);
        if (this.isOnLightningRod) {
            ItemStack newStack = new ItemStack(CustomItems.MAGNETIC_TRIDENT);
            newStack.setNbt(this.tridentStack.getNbt());
            newStack = ItemEnchantmentsHelper.setItemEnchantments(newStack);
            this.tridentStack = newStack;
            this.isOnLightningRod = false;
            if (this.getOwner() instanceof ServerPlayerEntity player) {
                UnruffledMod.MAGNETIC_TRIDENT_CRITERION.trigger(player);
            }
        }
    }

    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public boolean impalingForUnderwaterMobs(Entity instance, DamageSource source, float amount) {
        if (Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(Enchantments.IMPALING) && instance instanceof LivingEntity livingEntity && livingEntity.isWet()) amount += 12.5f;
        return instance.damage(source, amount);
    }

    @ModifyVariable(method = "age", at = @At("STORE"), ordinal = 0)
    public int disableNonLoyaltyDespawn(int loyaltyLevel) {
        return 3;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readOnLightningRod(NbtCompound nbt, CallbackInfo ci) {
        this.isOnLightningRod = nbt.getBoolean("IsOnLightningRod");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeOnLightningRod(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("IsOnLightningRod", this.isOnLightningRod);
    }
}
