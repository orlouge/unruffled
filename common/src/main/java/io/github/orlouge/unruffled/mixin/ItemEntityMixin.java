package io.github.orlouge.unruffled.mixin;

import io.github.orlouge.unruffled.interfaces.HasFireImmunitySetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements HasFireImmunitySetting {
    @Unique
    private boolean unruffled_fireImmune = false;

    @Inject(method = "readCustomData", at = @At("TAIL"))
    public void readFireImmunity(ReadView view, CallbackInfo ci) {
        this.unruffled_fireImmune = view.getBoolean("DeathDropFireImmune", false);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    public void writeFireImmunity(WriteView view, CallbackInfo ci) {
        if (unruffled_fireImmune) view.putBoolean("DeathDropFireImmune", true);
    }

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    public void forceFireImmunity(CallbackInfoReturnable<Boolean> cir) {
        if (this.unruffled_fireImmune) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Override
    public void setFireImmune(boolean immune) {
        this.unruffled_fireImmune = true;
    }

    @Override
    public boolean getFireImmune() {
        return this.unruffled_fireImmune;
    }
}
