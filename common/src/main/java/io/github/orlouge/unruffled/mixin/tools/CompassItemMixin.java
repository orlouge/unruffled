package io.github.orlouge.unruffled.mixin.tools;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.utils.TradedCompasses;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CompassItem.class)
public class CompassItemMixin extends Item {
    public CompassItemMixin(Settings settings) {
        super(settings);
    }

    private static void addBuy(ItemUsageContext context, ItemStack stack) {
        if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer && stack.contains(DataComponentTypes.LODESTONE_TRACKER) &&
            ((stack.contains(DataComponentTypes.CUSTOM_NAME) && !stack.get(DataComponentTypes.CUSTOM_NAME).asTruncatedString(10).isEmpty()) ||
             (stack.contains(DataComponentTypes.LORE) && !stack.get(DataComponentTypes.LORE).lines().isEmpty())
            )
        ) {
            ItemStack buyStack = stack.copyComponentsToNewStack(Items.COMPASS, 1);
            TradedCompasses.get(serverPlayer.getServerWorld().getPersistentStateManager()).addBuy(serverPlayer, buyStack);
        }
    }

    @Inject(method = "useOnBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void dontLockSpawnCompass(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getStack().contains(UnruffledMod.LOCKED_COMPASS_COMPONENT)) {
            cir.setReturnValue(super.useOnBlock(context));
            cir.cancel();
        }
    }

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public void setLoreInPlace(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, @Local ItemStack stack) {
        updateLore(context, stack);
        addBuy(context, stack);
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    public boolean addBuyNewStack(PlayerInventory instance, ItemStack stack, ItemUsageContext context) {
        addBuy(context, stack);
        return context.getWorld().isClient || instance.insertStack(stack);
    }

    @ModifyExpressionValue(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copyComponentsToNewStack(Lnet/minecraft/item/ItemConvertible;I)Lnet/minecraft/item/ItemStack;"))
    public ItemStack setLoreNewStack(ItemStack stack, ItemUsageContext context) {
        updateLore(context, stack);
        return stack;
    }

    private static void updateLore(ItemUsageContext context, ItemStack stack) {
        if (context.getWorld() instanceof ServerWorld world) {
            Text newLore = TradedCompasses.get(world.getPersistentStateManager()).getLodestoneName(context.getBlockPos());
            if (newLore != null) {
                stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(newLore)));
            }
        }
    }
}
