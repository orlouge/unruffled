package io.github.orlouge.unruffled.mixin.accessors;

import net.minecraft.item.ToolMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ToolMaterials.class)
public abstract class ToolMaterialsAccessor {
    @Accessor
    public abstract void setItemDurability(int durability);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void modifyInitDurability(String string, int i, int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Supplier repairIngredient, CallbackInfo ci) {
        if (string.equals("GOLD")) {
            this.setItemDurability(200);
        } else {
            this.setItemDurability(itemDurability * 2);
        }
    }
}
