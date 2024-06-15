package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class ItemEnchantmentsHelper {
    public static ItemStack createWithItemEnchantments(Item item) {
        return setItemEnchantments(new ItemStack(item));
    }

    public static boolean hasItemEnchantments(ItemStack stack) {
        return UnruffledMod.ITEM_ENCHANTMENTS.containsKey(stack.getItem());
    }
    public static ItemStack setItemEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = UnruffledMod.ITEM_ENCHANTMENTS.get(stack.getItem());
        if (enchantments == null) return stack;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            stack.addEnchantment(entry.getKey(), entry.getValue());
        }
        stack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        return stack;
    }
}
