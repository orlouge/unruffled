package io.github.orlouge.unruffled;

import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

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

        ModelPredicateProvider compassProvider = ModelPredicateProviderRegistry.get(Items.RECOVERY_COMPASS, new Identifier("angle"));
        if (compassProvider instanceof CompassAnglePredicateProvider compassAnglePredicateProvider) {
            CompassAnglePredicateProvider.CompassTarget originalTarget = compassAnglePredicateProvider.compassTarget;
            compassAnglePredicateProvider.compassTarget = (world, stack, entity) ->
                lockedDeathPosition.filter((pos) ->
                    EnchantmentHelper.hasBindingCurse(stack)).orElse(originalTarget.getPos(world, stack, entity)
                );
        }

        return 0;
    }

    public static void onAttackMiss(ClientPlayerEntity player, boolean alwaysSwing) {
        if (alwaysSwing || ExtendedHungerManager.canAttack(player, UnruffledModClient.stamina)) {
            player.swingHand(Hand.MAIN_HAND, false);
            new Packets.AttackMiss().sendToServer();
        }
    }

    public static void onItemUse(ClientPlayerEntity player) {
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(Items.RECOVERY_COMPASS)) {
            new Packets.LockRecoveryCompass().sendToServer();
        }
    }
}
