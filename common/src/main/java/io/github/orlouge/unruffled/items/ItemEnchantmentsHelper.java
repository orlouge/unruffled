package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

public class ItemEnchantmentsHelper {
    public static ItemStack createWithItemEnchantments(Item item) {
        return setItemEnchantments(new ItemStack(item));
    }

    public static boolean hasItemEnchantments(ItemStack stack) {
        return Config.INSTANCE.get().enchantmentsConfig.itemEnchantments().containsKey(stack.getItem());
    }
    public static ItemStack setItemEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = Config.INSTANCE.get().enchantmentsConfig.itemEnchantments().get(stack.getItem());
        boolean hide = true;
        if (stack.isOf(CustomItems.EVIL_TOTEM) && Config.INSTANCE.get().mechanicsConfig.evokerDropsEvilTotem()) {
            if (enchantments == null) enchantments = new HashMap<>();
            enchantments.put(Enchantments.BINDING_CURSE, 1);
            hide = false;
        }
        if (enchantments == null) return stack;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            stack.addEnchantment(entry.getKey(), entry.getValue());
        }
        if (hide) stack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        return stack;
    }

    public static boolean isDisabled(Enchantment enchantment, ItemStack stack) {
        return Config.isLoaded() && Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(enchantment) && !(Config.INSTANCE.get().enchantmentsConfig.itemEnchantments().getOrDefault(stack.getItem(), Collections.emptyMap()).containsKey(enchantment));
    }

    private static boolean isUnselectable(Enchantment enchantment, ItemStack stack) {
        return Config.isLoaded() && Config.INSTANCE.get().enchantmentsConfig.unobtainableEnchantments().contains(enchantment);
    }

    public static ItemStack processItem(ItemStack stack, RegistryEntryLookup.RegistryLookup lookup, boolean allowNonRandomLootEnchantments) {
        stack = stack.copy();
        boolean hasEnchantments = stack.hasEnchantments();
        boolean hasStoredEnchantments = stack.hasNbt() && stack.getOrCreateNbt().contains("StoredEnchantments");
        while (hasEnchantments || hasStoredEnchantments) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
            Map<Enchantment, Integer> out = new LinkedHashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (!ItemEnchantmentsHelper.isDisabled(entry.getKey(), stack) &&
                    (allowNonRandomLootEnchantments || !ItemEnchantmentsHelper.isUnselectable(entry.getKey(), stack)
                )) {
                    out.put(entry.getKey(), entry.getValue());
                }
            }
            EnchantmentHelper.set(enchantments, stack);
            if (hasEnchantments) {
                hasEnchantments = false;
            } else {
                hasStoredEnchantments = false;
            }
        }

        if (stack.isOf(Items.ENCHANTED_BOOK) && EnchantmentHelper.get(stack).isEmpty()) {
            stack = new ItemStack(Items.BOOK, stack.getCount());
        }

        if (stack.hasNbt() && stack.getOrCreateNbt().contains("Items")) {
            NbtList itemList = stack.getOrCreateNbt().getList("Items", NbtElement.COMPOUND_TYPE);
            NbtList outList = new NbtList();
            for (NbtElement el : itemList) {
                if (el.getType() != NbtElement.COMPOUND_TYPE) continue;
                ItemStack itemStack = ItemStack.fromNbt((NbtCompound) el);
                NbtCompound out = new NbtCompound();
                processItem(itemStack.copy(), lookup, allowNonRandomLootEnchantments).writeNbt(out);
                outList.add(out);
            }
            stack.getOrCreateNbt().put("Items", outList);
        }

        return stack;
    }
}
