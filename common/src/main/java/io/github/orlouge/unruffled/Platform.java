package io.github.orlouge.unruffled;

import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Platform {
    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String mod) {
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
    public static <T extends Packets.Packet> void registerServerReceiver(CustomPayload.Id<T> type, BiConsumer<T, PlayerEntity> receiver) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Packets.Packet> void registerClientReceiver(CustomPayload.Id<T> type, Consumer<T> receiver) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends LootFunction> Supplier<LootFunctionType<T>> registerLootFunctionType(Identifier id, MapCodec<T> codec) {
        throw new AssertionError();
    }
}
