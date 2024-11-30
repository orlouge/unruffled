package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.Config;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChiseledBookshelfBlockEntity.class)
public class ChiseledBookshelfBlockEntityMixin {
    @Shadow @Final public DefaultedList<ItemStack> inventory;

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void removeUnselectableEnchantedBooks(NbtCompound nbt, CallbackInfo ci) {
        if (!Config.INSTANCE.get().enchantmentsConfig.disenchantChiseledBookshelfBooks().orElse(false)) return;
        for (int idx = 0; idx < this.inventory.size(); idx++) {
            ItemStack stack = this.inventory.get(idx);
            if (!stack.isEmpty() && stack.isOf(Items.ENCHANTED_BOOK)) {
                boolean removeEnchantments = false;
                for (Enchantment enchantment : EnchantmentHelper.get(stack).keySet()) {
                    if (Config.INSTANCE.get().enchantmentsConfig.unobtainableEnchantments().contains(enchantment)) {
                        removeEnchantments = true;
                        break;
                    }
                }
                if (removeEnchantments) {
                    this.inventory.set(idx, new ItemStack(Items.BOOK));
                }
            }
        }
    }
}
