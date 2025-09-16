package io.github.orlouge.unruffled.mixin.accessors;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialsAccessor {
    private boolean isGold = false;

    @ModifyArg(method = "applyBaseSettings", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item$Settings;maxDamage(I)Lnet/minecraft/item/Item$Settings;"))
    public int modifyDurability(int maxDamage) {
        return isGold ? 200 : maxDamage * 2;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void storeIsGold(TagKey<Block> tagKey, int durability, float f, float g, int j, TagKey<Block> tagKey2, CallbackInfo ci) {
        if (tagKey == BlockTags.INCORRECT_FOR_GOLD_TOOL) isGold = true;
    }
}
