package io.github.orlouge.unruffled.items;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BolsterItem extends Item {
    protected BolsterItem(ToolMaterial material, Settings settings) {
        super(settings.tool(
            material, TagKey.of(RegistryKeys.BLOCK, Identifier.of(UnruffledMod.MOD_ID, "mineable/bolster")),
            1.0F, -2.8F, 0
        ));
    }
}
