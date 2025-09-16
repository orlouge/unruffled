package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BundleTooltipComponent.class)
public class BundleTooltipComponentMixin {
    /*
    @ModifyConstant(method = "appendTooltip", constant = @Constant(intValue = 64))
    public int modifyAppendTooltip(int max) {
        return Config.INSTANCE.get().mechanicsConfig.bundleSize();
    }

     */
}
