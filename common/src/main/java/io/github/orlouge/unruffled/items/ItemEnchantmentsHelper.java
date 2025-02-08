package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.config.Config;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;

import javax.xml.crypto.Data;
import java.util.*;

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

    public static ItemStack processItem(ItemStack stack, RegistryEntryLookup.RegistryLookup lookup, boolean allowNonRandomLootEnchantments) {
        stack = stack.copy();
        boolean hasEnchantments = stack.contains(DataComponentTypes.ENCHANTMENTS);
        boolean hasStoredEnchantments = stack.contains(DataComponentTypes.STORED_ENCHANTMENTS);
        while (hasEnchantments || hasStoredEnchantments) {
            ItemEnchantmentsComponent enchantmentsComponent;
            if (hasEnchantments) {
                enchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
            } else {
                enchantmentsComponent = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
            }
            ItemEnchantmentsComponent.Builder out = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantmentsComponent.getEnchantmentEntries()) {
                if (entry.getKey().getKey().isEmpty() || (
                    !ItemEnchantmentsHelper.isDisabled(entry.getKey().getKey().get(), stack) &&
                    (allowNonRandomLootEnchantments || entry.getKey().isIn(EnchantmentTags.ON_RANDOM_LOOT))
                )) {
                    out.add(entry.getKey(), entry.getValue());
                }
            }
            ItemEnchantmentsComponent outComponent = out.build();
            if (hasEnchantments) {
                hasEnchantments = false;
                stack.set(DataComponentTypes.ENCHANTMENTS, outComponent);
            } else {
                hasStoredEnchantments = false;
                stack.set(DataComponentTypes.STORED_ENCHANTMENTS, outComponent);
            }
        }

        if (stack.isOf(Items.ENCHANTED_BOOK) && (!stack.contains(DataComponentTypes.STORED_ENCHANTMENTS) || stack.get(DataComponentTypes.STORED_ENCHANTMENTS).isEmpty())) {
            stack = stack.copyComponentsToNewStack(Items.BOOK, stack.getCount());
            if (stack.contains(DataComponentTypes.STORED_ENCHANTMENTS)) stack.remove(DataComponentTypes.STORED_ENCHANTMENTS);
        }

        if (stack.contains(DataComponentTypes.BUNDLE_CONTENTS)) {
            BundleContentsComponent bundleComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            BundleContentsComponent.Builder out = new BundleContentsComponent.Builder(BundleContentsComponent.DEFAULT);
            for (ItemStack substack : bundleComponent.iterate()) {
                out.add(processItem(substack, lookup, allowNonRandomLootEnchantments));
            }
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, out.build());
        }

        if (stack.contains(DataComponentTypes.CONTAINER)) {
            ContainerComponent bundleComponent = stack.get(DataComponentTypes.CONTAINER);
            LinkedList<ItemStack> out = new LinkedList<>();
            for (ItemStack substack : bundleComponent.iterateNonEmpty()) {
                out.add(processItem(substack, lookup, allowNonRandomLootEnchantments));
            }
            stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(out));
        }

        return stack;
    }
}
