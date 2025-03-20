package io.github.orlouge.unruffled.mixin.worldgen;

import io.github.orlouge.unruffled.config.Config;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @ModifyVariable(method = "spawnEntities", at = @At(value = "STORE"))
    public NbtCompound removeEnchantmentsFromItemFrames(NbtCompound nbt, ServerWorldAccess world) {
        if (Config.INSTANCE.get().enchantmentsConfig.filterStructureItemFrames().orElse(true) && nbt.contains("id")) {
            Identifier id = new Identifier(nbt.getString("id"));
            if (id.equals(new Identifier("item_frame")) || id.equals(new Identifier("glow_item_frame"))) {
                if (nbt.contains("Item")) {
                    ItemStack stack = ItemStack.fromNbt(nbt.getCompound("Item"));
                    NbtCompound itemNbt = new NbtCompound();
                    ItemEnchantmentsHelper.processItem(
                        stack,
                        world.getRegistryManager().createRegistryLookup(),
                        false
                    ).writeNbt(itemNbt);
                    nbt.put("Item", itemNbt);
                }
            }
        }
        return nbt;
    }

    @Inject(method = "process", at = @At("RETURN"), cancellable = true)
    private static void processContainerBlockEntities(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, List<StructureTemplate.StructureBlockInfo> infos, CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir) {
        List<StructureTemplate.StructureBlockInfo> blocks = cir.getReturnValue();
        if (!(blocks instanceof ArrayList<StructureTemplate.StructureBlockInfo>)) blocks = new ArrayList<>(blocks);
        for (int i = 0; i < blocks.size(); i++) {
            NbtCompound nbt = null;
            StructureTemplate.StructureBlockInfo block = blocks.get(i);
            if (Config.INSTANCE.get().enchantmentsConfig.filterStructureChiseledBookshelves().orElse(true) && block.state().isOf(Blocks.CHISELED_BOOKSHELF) && block.nbt() != null && block.nbt().contains("Items", NbtElement.LIST_TYPE)) {
                nbt = block.nbt().copy();
                NbtList bookListNbt = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
                for (int j = 0; j < bookListNbt.size(); j++) {
                    NbtCompound bookNbt = bookListNbt.getCompound(j);
                    if (bookNbt.contains("id", NbtElement.STRING_TYPE) && new Identifier(bookNbt.getString("id")).equals(new Identifier("enchanted_book"))) {
                        ItemStack stack = ItemStack.fromNbt(bookNbt);
                        ItemStack newStack = ItemEnchantmentsHelper.processItem(stack, world.getRegistryManager().createRegistryLookup(), false);
                        NbtCompound newBookNbt = new NbtCompound();
                        newStack.writeNbt(newBookNbt);
                        newBookNbt.putByte("Slot", bookNbt.getByte("Slot"));
                        bookListNbt.set(j, newBookNbt);
                    }
                }
            }
            if (nbt != null) {
                blocks.set(i, new StructureTemplate.StructureBlockInfo(block.pos(), block.state(), nbt));
            }
        }
        cir.setReturnValue(blocks);
    }
}
