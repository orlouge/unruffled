package io.github.orlouge.unruffled.interfaces;

import net.minecraft.entity.Entity;

import java.util.Collection;

public interface TeleporterEntity {
    void setTeleporting();
    void addTeleportTarget(Entity entity);
    void removeTeleportTarget(Entity entity);
    boolean isTeleporting();
    Collection<Entity> getTeleportTargets();
    void clearTeleporting();
}
