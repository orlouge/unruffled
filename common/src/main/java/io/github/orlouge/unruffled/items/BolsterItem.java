package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BolsterItem extends MiningToolItem {
    protected BolsterItem(float attackDamage, float attackSpeed, ToolMaterial material, Settings settings) {
        super(attackDamage, attackSpeed, material, TagKey.of(RegistryKeys.BLOCK, new Identifier(UnruffledMod.MOD_ID, "mineable/bolster")), settings);
    }
}
