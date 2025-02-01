package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemEnchantmentsHelper {
    public static ItemStack createWithItemEnchantments(Item item, RegistryEntryLookup.RegistryLookup registryLookup) {
        return setItemEnchantments(new ItemStack(item), registryLookup);
    }

    public static boolean hasItemEnchantments(ItemStack stack) {
        return Config.INSTANCE.get().enchantmentsConfig.itemEnchantments().containsKey(stack.getItem());
    }

    public static ItemStack setItemEnchantments(ItemStack stack, RegistryEntryLookup.RegistryLookup lookup) {
        Map<RegistryKey<Enchantment>, Integer> enchantments = Config.INSTANCE.get().enchantmentsConfig.itemEnchantments().get(stack.getItem());
        boolean hide = true;
        if (stack.isOf(CustomItems.EVIL_TOTEM) && Config.INSTANCE.get().mechanicsConfig.evokerDropsEvilTotem()) {
            if (enchantments == null) enchantments = new HashMap<>();
            enchantments.put(Enchantments.BINDING_CURSE, 1);
            hide = false;
        }
        if (enchantments == null) return stack;
        for (Map.Entry<RegistryKey<Enchantment>, Integer> entry : enchantments.entrySet()) {
            Optional<RegistryEntry.Reference<Enchantment>> enchantment = lookup.getOptionalEntry(RegistryKeys.ENCHANTMENT, entry.getKey());
            enchantment.ifPresent(ref -> stack.addEnchantment(ref, entry.getValue()));
        }
        if (hide && stack.get(DataComponentTypes.ENCHANTMENTS) != null) stack.set(DataComponentTypes.ENCHANTMENTS, stack.get(DataComponentTypes.ENCHANTMENTS).withShowInTooltip(false));
        return stack;
    }

    public static boolean isDisabled(RegistryKey<Enchantment> enchantment, ItemStack stack) {
        return Config.isLoaded() && Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().contains(enchantment) && !(Config.INSTANCE.get().enchantmentsConfig.itemEnchantments().getOrDefault(stack.getItem(), Collections.emptyMap()).containsKey(enchantment));
    }
}
