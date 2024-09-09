package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;

public class ItemEnchantmentsLootFunction implements LootFunction {
    public ItemEnchantmentsLootFunction() {
    }

    @Override
    public LootFunctionType getType() {
        return UnruffledMod.ITEM_ENCHANTMENTS_LOOT_FUNCTION_TYPE.get();
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return ItemEnchantmentsHelper.setItemEnchantments(itemStack);
    }

}
