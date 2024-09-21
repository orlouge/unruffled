package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MapState.class)
public class MapStateMixin {
    @ModifyConstant(method = "zoomOut", constant = @Constant(intValue = 4))
    public int overrideMaxMapSize(int constant) {
        return Config.INSTANCE.get().mechanicsConfig.maxMapSize();
    }
}
