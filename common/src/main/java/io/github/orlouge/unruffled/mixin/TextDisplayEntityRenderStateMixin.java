package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.interfaces.HasAttachedLodestone;
import net.minecraft.client.render.entity.state.TextDisplayEntityRenderState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(TextDisplayEntityRenderState.class)
public class TextDisplayEntityRenderStateMixin implements HasAttachedLodestone {
    private Optional<BlockPos> attachedLodestone = null;

    @Override
    public Optional<BlockPos> getAttachedLodestone() {
        return Optional.ofNullable(attachedLodestone).flatMap(o -> o);
    }

    @Override
    public void setAttachedLodestone(BlockPos pos, Text name) {
        attachedLodestone = Optional.ofNullable(pos);
    }
}
