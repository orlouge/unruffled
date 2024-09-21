package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.Config;
import net.minecraft.screen.CartographyTableScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CartographyTableScreenHandler.class)
public class CartographyTableScreenHandlerMixin {
    @ModifyConstant(method = "method_17382", constant = @Constant(intValue = 4, ordinal = 0))
    public int overrideMaxMapSize(int constant) {
        return Config.INSTANCE.get().mechanicsConfig.maxMapSize();
    }
}
