package io.github.orlouge.unruffled.mixin.potions;

import io.github.orlouge.unruffled.interfaces.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements TeleporterEntity {
    private Set<Entity> teleportTargets = new HashSet<>();
    private boolean teleporting = false;

    @Override
    public void setTeleporting() {
        this.teleporting = true;
    }

    @Override
    public void addTeleportTarget(Entity entity) {
        this.teleportTargets.add(entity);
    }

    @Override
    public void removeTeleportTarget(Entity entity) {
        this.teleportTargets.remove(entity);
    }

    @Override
    public boolean isTeleporting() {
        return this.teleporting;
    }

    @Override
    public Collection<Entity> getTeleportTargets() {
        return new HashSet<>(this.teleportTargets);
    }

    @Override
    public void clearTeleporting() {
        this.teleporting = false;
        this.teleportTargets.clear();;
    }
}
