package io.github.orlouge.unruffled.interfaces;

import net.minecraft.util.math.GlobalPos;

import java.util.Optional;

public interface HasLockedDeathPosition {
    Optional<GlobalPos> getLockedDeathPosition();
    void setLockedDeathPosition();
}
