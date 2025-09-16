package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.component.type.MapIdComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MapIdComponent.class)
public class MapIdComponentMixin {
    @ModifyConstant(method = "appendTooltip", constant = @Constant(intValue = 4))
    public int overrideMaxMapSize(int constant) {
        return Config.INSTANCE.get().navigationConfig.maxMapSize();
    }
}
