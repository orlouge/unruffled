package io.github.orlouge.unruffled.items;

import com.mojang.serialization.Codec;
import io.github.orlouge.unruffled.utils.RomanNumerals;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class AncientCodexItem extends Item {
    public static final ComponentType<Integer> NUMBER = ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build();
    public AncientCodexItem(Settings settings) {
        super(settings);
    }

    public static ItemStack setNumber(ItemStack stack, int number) {
        stack = stack.copy();
        stack.set(NUMBER, number);
        return stack;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text base = super.getName(stack);
        if (stack.contains(NUMBER)) {
            MutableText name = base.copy();
            name.append(" " + RomanNumerals.MAP.getOrDefault(stack.get(NUMBER), "?"));
            return name;
        }
        return base;
    }
}
