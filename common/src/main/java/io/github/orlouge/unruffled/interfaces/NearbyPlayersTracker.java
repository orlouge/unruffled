package io.github.orlouge.unruffled.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface NearbyPlayersTracker {
    PlayerEntity getNearbyPlayer(int index);
}
