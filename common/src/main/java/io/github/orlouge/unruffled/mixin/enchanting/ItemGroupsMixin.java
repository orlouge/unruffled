package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {
    @Redirect(method = "addMaxLevelEnchantedBooks", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream<Enchantment> filterMaxLevelDisabledEnchantments(Stream<Enchantment> stream, Predicate<? super Enchantment> predicate) {
        return stream.filter(ench -> predicate.test(ench) && !UnruffledMod.DISABLED_ENCHANTMENTS.contains(ench));
    }

    @Redirect(method = "addAllLevelEnchantedBooks", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream<Enchantment> filterAllLevelDisabledEnchantments(Stream<Enchantment> stream, Predicate<? super Enchantment> predicate) {
        return stream.filter(ench -> predicate.test(ench) && !UnruffledMod.DISABLED_ENCHANTMENTS.contains(ench));
    }
}
