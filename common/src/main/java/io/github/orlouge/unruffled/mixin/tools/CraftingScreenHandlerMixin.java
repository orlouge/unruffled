package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    @ModifyVariable(method = "updateResult", at = @At("STORE"), ordinal = 1)
    private static ItemStack addItemEnchantments(ItemStack stack, ScreenHandler screenHandler, World world) {
        return ItemEnchantmentsHelper.setItemEnchantments(stack, world.getRegistryManager().createRegistryLookup());
    }
}
