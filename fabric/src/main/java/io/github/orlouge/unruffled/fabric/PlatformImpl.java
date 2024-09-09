package io.github.orlouge.unruffled.fabric;

import io.github.orlouge.unruffled.Packets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static Supplier<LootFunctionType> registerLootFunctionType(Identifier id, JsonSerializer<? extends LootFunction> serializer) {
        System.out.println("Registering " + id);
        LootFunctionType type = Registry.register(Registries.LOOT_FUNCTION_TYPE, id, new LootFunctionType(serializer));
        return () -> type;
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
