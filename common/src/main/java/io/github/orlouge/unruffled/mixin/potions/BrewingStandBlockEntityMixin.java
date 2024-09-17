package io.github.orlouge.unruffled.mixin.potions;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;canCraft(Lnet/minecraft/util/collection/DefaultedList;)Z"))
    private static void onTickBeforeFuelConsumed(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.brewTime > 6 && blockEntity.fuel > 0) {
            blockEntity.brewTime -= 7;
            /*
            if (blockEntity.brewTime < 7) {
                blockEntity.fuel -= 1;
            }
            */
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;getSlotsEmpty()[Z"))
    private static void onTickAfterFuelConsumed(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.brewTime <= 0 && BrewingStandBlockEntity.canCraft(blockEntity.inventory)) {
            blockEntity.brewTime = 400;
            blockEntity.itemBrewing = blockEntity.inventory.get(3).getItem();
            blockEntity.markDirty();
            /*
            world.markDirty(pos);
            world.updateComparators(pos, state.getBlock());
             */
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V"))
    private static void craftReject(World world, BlockPos pos, DefaultedList<ItemStack> slots, World world2, BlockPos pos2, BlockState state, BrewingStandBlockEntity blockEntity) {
        BrewingStandBlockEntity.craft(world, pos, slots);
        if (blockEntity.fuel == 0) {
            for (int i = 0; i < 3; i++) {
                if (world.getRandom().nextFloat() < 0.1) {
                    ItemStack stack = slots.get(i).copy();
                    slots.set(i, PotionUtil.setPotion(stack, Potions.THICK));
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.getPlayers(player -> player.getBlockPos().isWithinDistance(pos, 16)).forEach(player -> UnruffledMod.BAD_BREW_CRITERION.trigger(player));
                    }
                }
            }
        }
    }

}
