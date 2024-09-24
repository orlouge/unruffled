package io.github.orlouge.unruffled;

import io.github.orlouge.unruffled.interfaces.ExtendedHungerManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;

public class UnruffledModClient {
    public static float stamina = 1.0f, lastStaminaRegeneration = 1f, lastTravelPenalty = 0f;

    public static int initClient() {
        stamina = 1.0f;
        Packets.ExtendedHungerUpdate.register((s, r, h) -> {
            stamina = s;
            lastStaminaRegeneration = r;
            lastTravelPenalty = h;
        });
        return 0;
    }

    public static void onAttackMiss(ClientPlayerEntity player, boolean alwaysSwing) {
        if (alwaysSwing || ExtendedHungerManager.canAttack(player, UnruffledModClient.stamina)) {
            player.swingHand(Hand.MAIN_HAND, false);
            new Packets.AttackMiss().sendToServer();
        }
    }
}
