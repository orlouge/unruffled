package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import net.minecraft.recipe.MapExtendingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MapExtendingRecipe.class)
public class MapExtendingRecipeMixin {
    @ModifyConstant(method = "matches(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/world/World;)Z", constant = @Constant(intValue = 4, ordinal = 0))
    public int overrideMaxMapSize(int constant) {
        return Config.INSTANCE.get().mechanicsConfig.maxMapSize();
    }
}
