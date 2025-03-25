package io.github.orlouge.unruffled.interfaces;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface HasAttachedLodestone {
    Optional<BlockPos> getAttachedLodestone();
    void setAttachedLodestone(BlockPos pos, Text name);
}
