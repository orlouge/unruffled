package io.github.orlouge.unruffled;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Platform {
    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToClient(Packets.Packet packet, ServerPlayerEntity player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToServer(Packets.Packet packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Packets.Packet> void registerServerReceiver(Class<T> type, Identifier id, Function<PacketByteBuf, T> decoder, BiConsumer<T, PlayerEntity> receiver) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Packets.Packet> void registerClientReceiver(Class<T> type, Identifier id, Function<PacketByteBuf, T> decoder, Consumer<T> receiver) {
        throw new AssertionError();
    }
}
