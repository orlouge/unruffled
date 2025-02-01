package io.github.orlouge.unruffled.mixin.accessors;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
    @Accessor
    ComponentMap getComponents();
    @Accessor
    void setComponents(ComponentMap components);
}
