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
import net.minecraft.util.math.BlockPos;
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
        super(StatusEffectCategory.NEUTRAL, 0x40A095);
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
            ServerWorld targetWorld = null;
            BlockPos targetPos = null;
            boolean hasCompass = false;
            Collection<Entity> teleportTargets = teleporter.getTeleportTargets();
            teleporter.clearTeleporting();
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
            } else if (targetPos == null) {
                player.sendMessage(Text.translatable("effect." + UnruffledMod.MOD_ID + ".teleport.no_lodestone"), true);
            } else {
                Optional<Vec3d> teleportPos = Optional.empty();
                found:
                for (int y = 0; y <= 1; y++) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            teleportPos = PlayerEntity.findRespawnPosition(targetWorld, targetPos.add(x, y, z), 0f, true, true);
                            if (teleportPos.isPresent()) break found;
                        }
                    }
                }
                if (teleportPos.isEmpty()) {
                    player.sendMessage(Text.translatable("effect." + UnruffledMod.MOD_ID + ".teleport.no_teleport"), true);
                } else {
                    Vec3d deltaVec = teleportPos.get().add(0, 1, 0).subtract(targetPos.toCenterPos()).normalize();
                    float yaw = (float) MathHelper.atan2(deltaVec.getZ(), deltaVec.getX()) * 57 + 90;
                    float pitch = (float) Math.asin(MathHelper.clamp(deltaVec.getY(), -1, 1)) * 57;
                    for (Entity targetEntity : teleportTargets) {
                        targetEntity.teleport(targetWorld, teleportPos.get().getX(), teleportPos.get().getY(), teleportPos.get().getZ(), new HashSet<>(), yaw, pitch);
                        if (targetEntity instanceof PigEntity && targetWorld != playerWorld && targetWorld.getDimensionKey().equals(DimensionTypes.THE_END)) {
                            UnruffledMod.PIG_TELEPORTATION_CRITERION.trigger(player);
                        }
                    }
                    player.teleport(targetWorld, teleportPos.get().getX(), teleportPos.get().getY(), teleportPos.get().getZ(), yaw, pitch);
                    targetWorld.playSound(null, targetPos, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
                    UnruffledMod.TELEPORTATION_CRITERION.trigger(player);
                }
            }
        }
        super.onRemoved(entity, attributes, amplifier);
    }
}
