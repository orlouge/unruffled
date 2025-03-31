package io.github.orlouge.unruffled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Optional;
import java.util.function.Consumer;
public class Packets {
    public static abstract class Packet {
        public abstract void write(PacketByteBuf buf);
        public abstract Identifier getIdentifier();

        public void sendToServer() {
            Platform.sendToServer(this);
        }

        public void sendToPlayer(ServerPlayerEntity player) {
            Platform.sendToClient(this, player);
        }
    }

    public static class AttackMiss extends Packet {
        public static final Identifier PACKET_ID = new Identifier(UnruffledMod.MOD_ID, "attackmiss");

        public static void register(Consumer<ServerPlayerEntity> receiver) {
            Platform.registerServerReceiver(AttackMiss.class, PACKET_ID,
                    buf -> new AttackMiss(),
                    (packet, player) -> { if (packet != null && player instanceof ServerPlayerEntity p) receiver.accept(p); }
            );
        }

        @Override
        public void write(PacketByteBuf buf) {
        }

        @Override
        public Identifier getIdentifier() {
            return PACKET_ID;
        }
    }

    public static class LockCompass extends Packet {
        public static final Identifier PACKET_ID = new Identifier(UnruffledMod.MOD_ID, "lockrecoverycompass");

        public static void register(Consumer<ServerPlayerEntity> receiver) {
            Platform.registerServerReceiver(LockCompass.class, PACKET_ID,
                buf -> new LockCompass(),
                (packet, player) -> { if (packet != null && player instanceof ServerPlayerEntity p) receiver.accept(p); }
            );
        }

        @Override
        public void write(PacketByteBuf buf) {
        }

        @Override
        public Identifier getIdentifier() {
            return PACKET_ID;
        }
    }

    public static class ExtendedHungerUpdate extends Packet {
        public static final Identifier PACKET_ID = new Identifier(UnruffledMod.MOD_ID, "extendedhunger");
        private final float stamina, staminaRegeneration, travelPenalty;

        public ExtendedHungerUpdate(float stamina, float staminaRegeneration, float travelPenalty) {
            this.stamina = stamina;
            this.staminaRegeneration = staminaRegeneration;
            this.travelPenalty = travelPenalty;
        }

        public static void register(TriConsumer<Float, Float, Float> receiver) {
            Platform.registerClientReceiver(ExtendedHungerUpdate.class, PACKET_ID,
                    buf -> new ExtendedHungerUpdate(buf.readFloat(), buf.readFloat(), buf.readFloat()),
                    packet -> { if (packet != null) receiver.accept(packet.stamina, packet.staminaRegeneration, packet.travelPenalty); }
            );
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeFloat(stamina);
            buf.writeFloat(staminaRegeneration);
            buf.writeFloat(travelPenalty);
        }

        @Override
        public Identifier getIdentifier() {
            return PACKET_ID;
        }
    }

    public static class LockedDeathPositionUpdate extends Packet {
        public static final Identifier PACKET_ID = new Identifier(UnruffledMod.MOD_ID, "lockeddeathpos");
        private final Optional<GlobalPos> position;

        public LockedDeathPositionUpdate(Optional<GlobalPos> position) {
            this.position = position;
        }

        public static void register(Consumer<Optional<GlobalPos>> receiver) {
            Platform.registerClientReceiver(LockedDeathPositionUpdate.class, PACKET_ID,
                buf -> new LockedDeathPositionUpdate(buf.readOptional(PacketByteBuf::readGlobalPos)),
                packet -> { if (packet != null) receiver.accept(packet.position); }
            );
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeOptional(position, PacketByteBuf::writeGlobalPos);
        }

        @Override
        public Identifier getIdentifier() {
            return PACKET_ID;
        }
    }
}
