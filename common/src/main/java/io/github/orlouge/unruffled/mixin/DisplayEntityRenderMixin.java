package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.interfaces.HasAttachedLodestone;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayEntityRenderer.TextDisplayEntityRenderer.class)
public class DisplayEntityRenderMixin {
    @Inject(method = "render(Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity;Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity$Data;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", at = @At("HEAD"), cancellable = true)
    public void hideLodestoneText(DisplayEntity.TextDisplayEntity textDisplayEntity, DisplayEntity.TextDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f, CallbackInfo ci) {
        if (textDisplayEntity instanceof HasAttachedLodestone attachedLodestone && attachedLodestone.getAttachedLodestone().isPresent()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null ||
                !(client.crosshairTarget instanceof BlockHitResult hitResult) ||
                !hitResult.getBlockPos().equals(attachedLodestone.getAttachedLodestone().get()) ||
                !client.world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.LODESTONE)) {
                ci.cancel();
            }
        }
    }
}
