package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BolsterItem extends MiningToolItem {
    protected BolsterItem(ToolMaterial material, Settings settings) {
        super(material, TagKey.of(RegistryKeys.BLOCK, Identifier.of(UnruffledMod.MOD_ID, "mineable/bolster")), settings);
    }
}
