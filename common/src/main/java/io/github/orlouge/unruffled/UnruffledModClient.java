package io.github.orlouge.unruffled;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class UnruffledModClient {
    public static float stamina = 1.0f, lastStaminaRegeneration = 1f, lastTravelPenalty = 0f;
    public static Optional<GlobalPos> lockedDeathPosition = Optional.empty();

    public static int initClient() {
        stamina = 1.0f;
        Packets.ExtendedHungerUpdate.register((s, r, h) -> {
            stamina = s;
            lastStaminaRegeneration = r;
            lastTravelPenalty = h;
        });

        Packets.LockedDeathPositionUpdate.register((pos) -> { lockedDeathPosition = pos; });

        if (Config.INSTANCE.get().navigationConfig.recoveryCompassLocking()) {
            ModelPredicateProvider recoveryCompassProvider = ModelPredicateProviderRegistry.get(Items.RECOVERY_COMPASS.getDefaultStack(), Identifier.ofVanilla("angle"));
            if (recoveryCompassProvider instanceof CompassAnglePredicateProvider compassAnglePredicateProvider) {
                CompassAnglePredicateProvider.CompassTarget originalTarget = compassAnglePredicateProvider.compassTarget;
                compassAnglePredicateProvider.compassTarget = (world, stack, entity) ->
                    lockedDeathPosition.filter((pos) ->
                        stack.contains(UnruffledMod.LOCKED_COMPASS_COMPONENT)).orElse(originalTarget.getPos(world, stack, entity)
                    );
            }
        }

        if (Config.INSTANCE.get().navigationConfig.compassPointsNorth()) {
            ModelPredicateProvider compassProvider = ModelPredicateProviderRegistry.get(Items.COMPASS.getDefaultStack(), Identifier.ofVanilla("angle"));
            if (compassProvider instanceof CompassAnglePredicateProvider compassAnglePredicateProvider) {
                CompassAnglePredicateProvider.CompassTarget originalTarget = compassAnglePredicateProvider.compassTarget;
                compassAnglePredicateProvider.compassTarget = (world, stack, entity) ->
                    stack.contains(DataComponentTypes.LODESTONE_TRACKER) || stack.contains(UnruffledMod.LOCKED_COMPASS_COMPONENT)
                        ? originalTarget.getPos(world, stack, entity)
                        : new GlobalPos(entity.getWorld().getRegistryKey(), entity.getBlockPos().north(10000));
            }
        }

        return 0;
    }

    public static void onAttackMiss(ClientPlayerEntity player, boolean alwaysSwing) {
        if (alwaysSwing || ExtendedHungerManager.canAttack(player, UnruffledModClient.stamina)) {
            player.swingHand(Hand.MAIN_HAND, false);
            Packets.AttackMiss.INSTANCE.sendToServer();
        }
    }

    public static boolean onItemUse(PlayerEntity player) {
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);
        if ((Config.INSTANCE.get().navigationConfig.recoveryCompassLocking() && stackInHand.isOf(Items.RECOVERY_COMPASS)) || (Config.INSTANCE.get().navigationConfig.compassToggleSpawn() && stackInHand.isOf(Items.COMPASS) && !stackInHand.contains(DataComponentTypes.LODESTONE_TRACKER))) {
            Packets.LockCompass.INSTANCE.sendToServer();
            return true;
        }
        return false;
    }
}
