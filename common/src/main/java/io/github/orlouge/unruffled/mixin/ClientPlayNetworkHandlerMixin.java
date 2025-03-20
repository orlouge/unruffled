package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.items.CustomItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onOpenWrittenBook", at = @At("TAIL"))
    public void openAncientCodex(OpenWrittenBookS2CPacket packet, CallbackInfo ci) {
        ItemStack itemStack = this.client.player.getStackInHand(packet.getHand());
        if (itemStack.isOf(CustomItems.ANCIENT_CODEX)) {
            this.client.setScreen(new BookScreen(BookScreen.Contents.create(itemStack)));
        }
    }
}
