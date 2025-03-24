package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin extends Item {
    @Shadow public abstract void writeNbt(RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt);

    public CompassItemMixin(Settings settings) {
        super(settings);
    }

    private static void addBuy(ItemUsageContext context, ItemStack stack) {
        if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer && CompassItem.hasLodestone(stack) &&
            (stack.hasCustomName() || (stack.hasNbt() && stack.getNbt().contains("display", NbtElement.COMPOUND_TYPE) && stack.getNbt().getCompound("display").contains("Lore")))
        ) {
            ItemStack buyStack = stack.copyWithCount(1);
            TradedCompasses.get(serverPlayer.getServerWorld().getPersistentStateManager()).addBuy(serverPlayer, buyStack);
        }
    }

    @Inject(method = "useOnBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void dontLockSpawnCompass(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getStack().hasNbt() && context.getStack().getNbt().getBoolean("Spawn")) {
            cir.setReturnValue(super.useOnBlock(context));
            cir.cancel();
        }
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CompassItem;writeNbt(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/nbt/NbtCompound;)V", ordinal = 0))
    public void setLoreInPlace(CompassItem instance, RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt, ItemUsageContext context) {
        updateLore(context, nbt);
        instance.writeNbt(worldKey, pos, nbt);
        ItemStack stack = new ItemStack(Items.COMPASS, 1);
        stack.setNbt(nbt);
        addBuy(context, stack);
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    public boolean addBuyNewStack(PlayerInventory instance, ItemStack stack, ItemUsageContext context) {
        addBuy(context, stack);
        return context.getWorld().isClient || instance.insertStack(stack);
    }

    @ModifyVariable(method = "useOnBlock", at = @At(value = "STORE"))
    public NbtCompound setLoreNewStack(NbtCompound nbt, ItemUsageContext context) {
        updateLore(context, nbt);
        return nbt;
    }

    private static void updateLore(ItemUsageContext context, NbtCompound nbt) {
        if (context.getWorld() instanceof ServerWorld world) {
            Text newLore = TradedCompasses.get(world.getPersistentStateManager()).getLodestoneName(GlobalPos.create(context.getWorld().getRegistryKey(), context.getBlockPos()));
            if (newLore != null) {
                NbtList loreList = new NbtList();
                loreList.add(NbtString.of(Text.Serializer.toJson(newLore)));
                NbtCompound displayNbt = nbt.contains("display", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("display") : new NbtCompound();
                displayNbt.put("Lore", loreList);
                nbt.put("display", displayNbt);
            } else if (nbt.contains("display")) {
                nbt.getCompound("display").remove("Lore");
            }
        }
    }
}
