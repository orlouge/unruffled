package io.github.orlouge.unruffled.fabric;

import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.items.ExtendedCompassProperty;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class UnruffledFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UnruffledModClient.initClient();

        NumericProperties.ID_MAPPER.put(Identifier.of(UnruffledMod.MOD_ID, "extended_compass"), ExtendedCompassProperty.CODEC);
    }
}
