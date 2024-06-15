package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.utils.RomanNumerals;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class AncientCodexItem extends Item {
    public AncientCodexItem(Settings settings) {
        super(settings);
    }

    public static ItemStack setNumber(ItemStack stack, int number) {
        stack = stack.copy();
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("number", number);
        return stack;
    }

    public static NbtCompound getNumberNbt(int number) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("number", number);
        return nbt;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text base = super.getName(stack);
        if (stack.hasNbt() && stack.getNbt().contains("number", NbtElement.INT_TYPE)) {
            MutableText name = base.copy();
            name.append(" " + RomanNumerals.MAP.getOrDefault(stack.getNbt().getInt("number"), "?"));
            return name;
        }
        return base;
    }
}
