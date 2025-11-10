package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.PlayerPositionDebugHudEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(PlayerPositionDebugHudEntry.class)
public abstract class PlayerPositionDebugHudEntryMixin implements DebugHudEntry {
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/debug/DebugHudLines;addLinesToSection(Lnet/minecraft/util/Identifier;Ljava/util/Collection;)V"), index = 1)
    public Collection<String> addFacing(Collection<String> lines) {
        if (MinecraftClient.getInstance().hasReducedDebugInfo() && Config.INSTANCE.get().navigationConfig.reducedDebugFacing()) {
            List<String> lines2 = new ArrayList<>(lines);
            lines2.remove(2);
            lines2.remove(1);
            lines2.remove(0);
            return lines2;
        } else {
            return lines;
        }
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return Config.INSTANCE.get().navigationConfig.reducedDebugFacing() || !reducedDebugInfo;
    }
}
