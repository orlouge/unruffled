package io.github.orlouge.unruffled.mixin.enchanting;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {
    @Redirect(method = "addMaxLevelEnchantedBooks", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryWrapper;streamEntries()Ljava/util/stream/Stream;"))
    private static Stream<RegistryEntry.Reference<Enchantment>> filterMaxLevelDisabledEnchantments(RegistryWrapper<Enchantment> instance) {
        return instance.streamEntries().filter(ench -> Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().stream().noneMatch(e -> ench.getKeyOrValue().left().map(k -> k.equals(e)).orElse(false)));
    }

    @Redirect(method = "addAllLevelEnchantedBooks", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryWrapper;streamEntries()Ljava/util/stream/Stream;"))
    private static Stream<RegistryEntry.Reference<Enchantment>> filterAllLevelDisabledEnchantments(RegistryWrapper<Enchantment> instance) {
        return instance.streamEntries().filter(ench -> Config.INSTANCE.get().enchantmentsConfig.disabledEnchantments().stream().noneMatch(e -> ench.getKeyOrValue().left().map(k -> k.equals(e)).orElse(false)));
    }
}
