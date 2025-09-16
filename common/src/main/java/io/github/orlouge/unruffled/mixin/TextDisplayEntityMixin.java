package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.interfaces.HasAttachedLodestone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayEntityMixin extends DisplayEntity implements HasAttachedLodestone {
    @Shadow protected abstract void setText(net.minecraft.text.Text text);

    @Shadow protected abstract void setTextOpacity(byte textOpacity);

    private static final TrackedData<Optional<BlockPos>> ATTACHED_LODESTONE = DataTracker.registerData(DisplayEntity.TextDisplayEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    public TextDisplayEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    public void trackLodestone(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(ATTACHED_LODESTONE, Optional.empty());
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    public void readLodestone(ReadView view, CallbackInfo ci) {
        Optional<BlockPos> pos = view.read("attached_lodestone", BlockPos.CODEC);
        this.dataTracker.set(ATTACHED_LODESTONE, pos);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    public void writeLodestone(WriteView view, CallbackInfo ci) {
        if (this.dataTracker.get(ATTACHED_LODESTONE).isPresent()) {
            view.put("attached_lodestone", BlockPos.CODEC, this.dataTracker.get(ATTACHED_LODESTONE).get());
        }
    }

    @Override
    public Optional<BlockPos> getAttachedLodestone() {
        return this.dataTracker.get(ATTACHED_LODESTONE);
    }

    @Override
    public void setAttachedLodestone(BlockPos pos, Text name) {
        this.dataTracker.set(ATTACHED_LODESTONE, Optional.ofNullable(pos));
        if (pos != null) this.setPosition(Vec3d.ofCenter(pos).add(0, 0.8, 0));
        this.setText(Text.of(name.asTruncatedString(50)));
        this.setTextOpacity((byte) 200);
        this.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
    }
}
