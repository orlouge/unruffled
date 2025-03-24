package io.github.orlouge.unruffled.potions;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.interfaces.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class TeleportEffect extends StatusEffect  {
    public TeleportEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x40A095);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof TeleporterEntity teleporter && teleporter.isTeleporting() && entity instanceof ServerPlayerEntity player) {
            ServerWorld playerWorld = player.getServerWorld();
            Collection<Entity> teleportTargets = teleporter.getTeleportTargets();
            teleporter.clearTeleporting();
            Optional<Pair<ServerWorld, Pair<BlockPos, Vec3d>>> teleportPosWorld = getTeleportPos(player);
            if (teleportPosWorld.isPresent()) {
                ServerWorld targetWorld = teleportPosWorld.get().getLeft();
                Vec3d teleportPos = teleportPosWorld.get().getRight().getRight();
                BlockPos lodestonePos = teleportPosWorld.get().getRight().getLeft();
                Vec3d deltaVec = teleportPos.add(0, 1, 0).subtract(lodestonePos.toCenterPos()).normalize();
                float yaw = (float) MathHelper.atan2(deltaVec.getZ(), deltaVec.getX()) * 57 + 90;
                float pitch = (float) Math.asin(MathHelper.clamp(deltaVec.getY(), -1, 1)) * 57;
                for (Entity targetEntity : teleportTargets) {
                    targetEntity.teleport(targetWorld, teleportPos.getX(), teleportPos.getY(), teleportPos.getZ(), new HashSet<>(), yaw, pitch);
                    if (targetEntity instanceof PigEntity && targetWorld != playerWorld && targetWorld.getDimensionEntry().getKey().map(k -> k.equals(DimensionTypes.THE_END)).orElse(false)) {
                        UnruffledMod.PIG_TELEPORTATION_CRITERION.trigger(player);
                    }
                }
                teleporter.setTeleportCooldown(200);
                player.teleport(targetWorld, teleportPos.getX(), teleportPos.getY(), teleportPos.getZ(), yaw, pitch);
                targetWorld.playSound(null, lodestonePos, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
                UnruffledMod.TELEPORTATION_CRITERION.trigger(player);
            }
        }
        super.onRemoved(entity, attributes, amplifier);
    }

    public static Optional<Pair<ServerWorld, Pair<BlockPos, Vec3d>>> getTeleportPos(ServerPlayerEntity player) {
        ServerWorld playerWorld = player.getServerWorld();
        ServerWorld targetWorld = null;
        BlockPos targetPos = null;
        boolean hasCompass = false;
        for (int i = -2; i < player.getInventory().size(); i++) {
            ItemStack stack;
            if (i == -2) {
                stack = player.getMainHandStack();
            } else if (i == -1) {
                stack = player.getOffHandStack();
            } else {
                stack = player.getInventory().getStack(i);
            }
            if (stack.isOf(Items.COMPASS) && CompassItem.hasLodestone(stack)) {
                hasCompass = true;

                NbtCompound compassNbt = stack.getOrCreateNbt();
                if (!compassNbt.contains("LodestonePos") || !compassNbt.contains("LodestoneTracked") || !compassNbt.getBoolean("LodestoneTracked")) {
                    continue;
                }

                Optional<RegistryKey<World>> lodestoneDimension = CompassItem.getLodestoneDimension(compassNbt);
                if (lodestoneDimension.isPresent()) {
                    targetWorld = playerWorld.getServer().getWorld(lodestoneDimension.get());
                    if (targetWorld == null) continue;
                    BlockPos lodestonePos = NbtHelper.toBlockPos(compassNbt.getCompound("LodestonePos"));
                    if (targetWorld.isInBuildLimit(lodestonePos) && targetWorld.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, lodestonePos)) {
                        targetPos = lodestonePos;
                        break;
                    }
                }
            }
        }
        if (!hasCompass) {
            player.sendMessage(Text.translatable("effect." + UnruffledMod.MOD_ID + ".teleport.no_compass"), true);
            if (player instanceof TeleporterEntity teleporter) teleporter.setTeleportCooldown(20);
        } else if (targetPos == null) {
            player.sendMessage(Text.translatable("effect." + UnruffledMod.MOD_ID + ".teleport.no_lodestone"), true);
            if (player instanceof TeleporterEntity teleporter) teleporter.setTeleportCooldown(100);
        } else {
            Optional<Vec3d> teleportPos = Optional.empty();
            found:
            for (int up = 1; up >= -1; up -= 2) {
                for (int y = up > 0 ? 0 : 1; y <= 2; y++) {
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            teleportPos = ServerPlayerEntity.findRespawnPosition(targetWorld, targetPos.add(x, y * up, z), 0f, true, true).map(r -> r.pos);
                            if (teleportPos.isPresent()) break found;
                        }
                    }
                }
            }
            if (teleportPos.isEmpty()) {
                if (player instanceof TeleporterEntity teleporter) teleporter.setTeleportCooldown(400);
                player.sendMessage(Text.translatable("effect." + UnruffledMod.MOD_ID + ".teleport.no_teleport"), true);
            } else {
                return Optional.of(new Pair<>(targetWorld, new Pair<>(targetPos, teleportPos.get())));
            }
        }

        return Optional.empty();
    }
}
