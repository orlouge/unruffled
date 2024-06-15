package io.github.orlouge.unruffled.fabric;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.items.CustomItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class UnruffedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UnruffledModClient.initClient();
        for (Map.Entry<String, Item> tridentType : CustomItems.TRIDENTS.entrySet()) {
            CustomTridentItemRenderer tridentRenderer = new CustomTridentItemRenderer(tridentType.getKey());
            ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(tridentRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(tridentType.getValue(), tridentRenderer);
        }

        ModelLoadingPlugin.register(new CustomModelLoadingPlugin());
    }

    private static class CustomModelLoadingPlugin implements ModelLoadingPlugin {
        @Override
        public void onInitializeModelLoader(Context pluginContext) {
            pluginContext.addModels(CustomItems.TRIDENTS.keySet().stream().map(
                    type -> new ModelIdentifier(UnruffledMod.MOD_ID, type + "_trident_gui", "inventory")
            ).toList());
        }
    }

    public static class CustomTridentItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, SimpleSynchronousResourceReloadListener {
        private final Identifier id;
        private final ModelIdentifier guiModelId;
        private TridentEntityModel tridentEntityModel = null;
        private BakedModel tridentGuiModel = null;
        private ItemRenderer itemRenderer = null;

        public CustomTridentItemRenderer(String tridentType) {
            this.id = new Identifier(UnruffledMod.MOD_ID, tridentType);
            this.guiModelId = new ModelIdentifier(UnruffledMod.MOD_ID, tridentType + "_trident_gui", "inventory");
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
            if (this.tridentEntityModel != null && mode != ModelTransformationMode.GUI && mode != ModelTransformationMode.GROUND && mode != ModelTransformationMode.FIXED) {
                matrices.push();
                matrices.scale(1.0F, -1.0F, -1.0F);
                Identifier inHandTexture = TridentEntityModel.TEXTURE;
                VertexConsumer vertexConsumer2 = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, this.tridentEntityModel.getLayer(inHandTexture), false, stack.hasGlint());
                this.tridentEntityModel.render(matrices, vertexConsumer2, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                matrices.pop();
            } else {
                matrices.pop();
                matrices.push();
                this.itemRenderer.renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, this.tridentGuiModel);
            }
        }

        @Override
        public Identifier getFabricId() {
            return this.id;
        }

        @Override
        public void reload(ResourceManager manager) {
            this.tridentEntityModel = new TridentEntityModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.TRIDENT));
            this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            this.tridentGuiModel = MinecraftClient.getInstance().getBakedModelManager().getModel(this.guiModelId);
        }
    }
}
