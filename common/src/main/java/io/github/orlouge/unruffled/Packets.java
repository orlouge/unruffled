package io.github.orlouge.unruffled;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Optional;
import java.util.function.Consumer;
public class Packets {
    public static abstract class Packet implements CustomPayload {
        public void sendToServer() {
            Platform.sendToServer(this);
        }

        public void sendToPlayer(ServerPlayerEntity player) {
            Platform.sendToClient(this, player);
        }
    }

    public static class AttackMiss extends Packet {
        public static final Id<AttackMiss> PACKET_ID = new Id<>(Identifier.of(UnruffledMod.MOD_ID, "attackmiss"));
        public static final AttackMiss INSTANCE = new AttackMiss();
        public static final PacketCodec<RegistryByteBuf, AttackMiss> CODEC = PacketCodec.unit(INSTANCE);

        private AttackMiss() {}

        public static void register(Consumer<ServerPlayerEntity> receiver) {
            Platform.registerServerReceiver(PACKET_ID,
                    (packet, player) -> { if (packet != null && player instanceof ServerPlayerEntity p) receiver.accept(p); }
            );
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public static class LockCompass extends Packet {
        public static final Id<LockCompass> PACKET_ID = new Id<>(Identifier.of(UnruffledMod.MOD_ID, "lockrecoverycompass"));
        public static final LockCompass INSTANCE = new LockCompass();
        public static final PacketCodec<RegistryByteBuf, LockCompass> CODEC = PacketCodec.unit(INSTANCE);

        private LockCompass() {}

        public static void register(Consumer<ServerPlayerEntity> receiver) {
            Platform.registerServerReceiver(PACKET_ID,
                (packet, player) -> { if (packet != null && player instanceof ServerPlayerEntity p) receiver.accept(p); }
            );
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public static class ExtendedHungerUpdate extends Packet {
        public static final Id<ExtendedHungerUpdate> PACKET_ID = new Id<>(Identifier.of(UnruffledMod.MOD_ID, "extendedhunger"));
        public static final PacketCodec<RegistryByteBuf, ExtendedHungerUpdate> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, e -> e.stamina,
            PacketCodecs.FLOAT, e -> e.staminaRegeneration,
            PacketCodecs.FLOAT, e -> e.travelPenalty,
            ExtendedHungerUpdate::new
        );
        private final float stamina, staminaRegeneration, travelPenalty;

        public ExtendedHungerUpdate(float stamina, float staminaRegeneration, float travelPenalty) {
            this.stamina = stamina;
            this.staminaRegeneration = staminaRegeneration;
            this.travelPenalty = travelPenalty;
        }

        public static void register(TriConsumer<Float, Float, Float> receiver) {
            Platform.registerClientReceiver(PACKET_ID,
                    packet -> { if (packet != null) receiver.accept(packet.stamina, packet.staminaRegeneration, packet.travelPenalty); }
            );
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public static class LockedDeathPositionUpdate extends Packet {
        public static final Id<LockedDeathPositionUpdate> PACKET_ID = new Id<>(Identifier.of(UnruffledMod.MOD_ID, "lockeddeathpos"));
        private final Optional<GlobalPos> position;
        public static final PacketCodec<ByteBuf, LockedDeathPositionUpdate> CODEC = PacketCodecs.optional(GlobalPos.PACKET_CODEC).xmap(LockedDeathPositionUpdate::new, p -> p.position);

        public LockedDeathPositionUpdate(Optional<GlobalPos> position) {
            this.position = position;
        }

        public static void register(Consumer<Optional<GlobalPos>> receiver) {
            Platform.registerClientReceiver(PACKET_ID,
                packet -> { if (packet != null) receiver.accept(packet.position); }
            );
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }
}
