package io.github.orlouge.unruffled.fabric;

import io.github.orlouge.unruffled.Packets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static void sendToClient(Packets.Packet packet, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, packet.getIdentifier(), buf);
    }

    public static void sendToServer(Packets.Packet packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(packet.getIdentifier(), buf);
    }

    public static <T extends Packets.Packet> void registerServerReceiver(Class<T> type, Identifier id, Function<PacketByteBuf, T> decoder, BiConsumer<T, PlayerEntity> receiver) {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, sender) -> {
            T packet = decoder.apply(buffer);
            server.execute(() -> receiver.accept(packet, player));
        });
    }

    public static <T extends Packets.Packet> void registerClientReceiver(Class<T> type, Identifier id, Function<PacketByteBuf, T> decoder, Consumer<T> receiver) {
        ClientImpl.registerClientReceiver(id, decoder, receiver);
    }

    private static class ClientImpl {
        private static <T extends Packets.Packet> void registerClientReceiver(Identifier id, Function<PacketByteBuf, T> decoder, Consumer<T> receiver) {
            ClientPlayNetworking.registerGlobalReceiver(id, ((client, handler, buf, responseSender) -> {
                T packet = decoder.apply(buf);
                client.execute(() -> receiver.accept(packet));
            }));
        }
    }
}
