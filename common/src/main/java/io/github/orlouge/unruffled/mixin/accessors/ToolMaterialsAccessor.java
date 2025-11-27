package io.github.orlouge.unruffled.mixin.accessors;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import io.github.orlouge.unruffled.config.Tools;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialsAccessor {
    private Integer modifiedDurability = null;

    @ModifyArg(method = "applyBaseSettings", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item$Settings;maxDamage(I)Lnet/minecraft/item/Item$Settings;"))
    public int modifyDurability(int maxDamage) {
        return modifiedDurability != null ? modifiedDurability : maxDamage;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void storeIsGold(TagKey<Block> tagKey, int durability, float f, float g, int j, TagKey<Block> tagKey2, CallbackInfo ci) {
        String material = tagKey2.id().getPath().replace("_tool_materials", "").replace("wooden", "wood");
        if (Tools.INSTANCE.get().toolConfig.durability().containsKey(material)) {
            modifiedDurability = Tools.INSTANCE.get().toolConfig.durability().get(material);
        }
    }
}
