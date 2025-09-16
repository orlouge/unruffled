package io.github.orlouge.unruffled;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
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
