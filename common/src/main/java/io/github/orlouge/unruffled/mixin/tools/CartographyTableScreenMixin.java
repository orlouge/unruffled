package io.github.orlouge.unruffled.mixin.tools;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CartographyTableScreen.class)
public class CartographyTableScreenMixin {
    @ModifyConstant(method = "drawBackground", constant = @Constant(intValue = 4, ordinal = 0))
    public int overrideMaxMapSize(int constant) {
        return Config.INSTANCE.get().navigationConfig.maxMapSize();
    }
}
