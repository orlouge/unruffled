package io.github.orlouge.unruffled.fabric;

import com.mojang.serialization.MapCodec;
import io.github.orlouge.unruffled.Packets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String mod) {
        return FabricLoader.getInstance().isModLoaded(mod);
    }

    public static void sendToClient(Packets.Packet packet, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToServer(Packets.Packet packet) {
        ClientPlayNetworking.send(packet);
    }

    public static <T extends Packets.Packet> void registerServerReceiver(CustomPayload.Id<T> type, BiConsumer<T, PlayerEntity> receiver) {
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            context.server().execute(() -> receiver.accept(payload, context.player()));
        });
    }

    public static <T extends Packets.Packet> void registerClientReceiver(CustomPayload.Id<T> type, Consumer<T> receiver) {
        ClientImpl.registerClientReceiver(type, receiver);
    }

    public static <T extends LootFunction> Supplier<LootFunctionType<T>> registerLootFunctionType(Identifier id, MapCodec<T> codec) {
        System.out.println("Registering " + id);
        LootFunctionType<T> type = Registry.register(Registries.LOOT_FUNCTION_TYPE, id, new LootFunctionType<T>(codec));
        return () -> type;
    }

    private static class ClientImpl {
        private static <T extends Packets.Packet> void registerClientReceiver(CustomPayload.Id<T> id, Consumer<T> receiver) {
            ClientPlayNetworking.registerGlobalReceiver(id, ((packet, context) -> {
                context.client().execute(() -> receiver.accept(packet));
            }));
        }
    }
}
