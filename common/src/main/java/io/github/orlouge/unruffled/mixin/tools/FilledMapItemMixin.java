package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import net.minecraft.item.FilledMapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FilledMapItem.class)
public class FilledMapItemMixin {
    @ModifyConstant(method = "appendTooltip", constant = @Constant(intValue = 4))
    public int overrideMaxMapSize(int constant) {
        return Config.INSTANCE.get().mechanicsConfig.maxMapSize();
    }
}
