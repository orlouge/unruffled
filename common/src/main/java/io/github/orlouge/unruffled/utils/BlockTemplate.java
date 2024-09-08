package io.github.orlouge.unruffled.utils;

import io.github.orlouge.unruffled.UnruffledMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BlockTemplate {
    public abstract BlockState getBlockState(Random random);
    public abstract void process(StructureWorldAccess world, Random random, BlockPos pos, Direction direction);

    public static BlockTemplate empty() {
        return new Simple(null);
    }

    public static BlockTemplate block(BlockState state) {
        return new Simple(state);
    }

    public static BlockTemplate block(Block block) {
        return block(block.getDefaultState());
    }

    public static BlockTemplate random(BlockState[] states) {
        return new RandomChoice(Arrays.stream(states).map(Simple::new).toList(), 0);
    }

    public static BlockTemplate random(BlockState[] states, int emptyWeight) {
        return new RandomChoice(Arrays.stream(states).map(Simple::new).toList(), emptyWeight);
    }

    public static BlockTemplate lootContainer(BlockState state, Identifier lootTableId) {
        return new ProcessBlockEntity(new Simple(state), (entity, random) -> {
            if (entity instanceof LootableContainerBlockEntity) {
                ((LootableContainerBlockEntity) entity).setLootTable(lootTableId, random.nextLong());
            }
        });
    }

    public static BlockTemplate lootContainer(Block block, Identifier lootTableId) {
        return lootContainer(block.getDefaultState(), lootTableId);
    }

    public static BlockTemplate lootBrushable(BlockState state, Identifier lootTableId) {
        return new ProcessBlockEntity(new Simple(state), (entity, random) -> {
            if (entity instanceof BrushableBlockEntity) {
                ((BrushableBlockEntity) entity).setLootTable(lootTableId, random.nextLong());
            }
        });
    }

    public static BlockTemplate lootBrushable(Block block, Identifier lootTableId) {
        return lootBrushable(block.getDefaultState(), lootTableId);
    }

    public static BlockTemplate blockEntity(BlockTemplate template, BiConsumer<BlockEntity, Random> blockEntityFunction) {
        return new ProcessBlockEntity(template, blockEntityFunction);
    }

    public static BlockTemplate blockEntity(BlockState state, BiConsumer<BlockEntity, Random> blockEntityFunction) {
        return blockEntity(new Simple(state), blockEntityFunction);
    }

    public static BlockTemplate blockEntity(Block block, BiConsumer<BlockEntity, Random> blockEntityFunction) {
        return blockEntity(block.getDefaultState(), blockEntityFunction);
    }

    public static BlockTemplate sideEffect(BlockTemplate template, Consumer<ProcessContext> processor) {
        return new ProcessWorld(template, processor);
    }

    public static BlockTemplate sideEffect(BlockTemplate template, BiConsumer<StructureWorldAccess, BlockPos> processor) {
        return new ProcessWorld(template, ctx -> processor.accept(ctx.world, ctx.pos));
    }

    public static BlockTemplate sideEffect(BlockState state, BiConsumer<StructureWorldAccess, BlockPos> processor) {
        return sideEffect(new Simple(state), processor);
    }

    private static class Simple extends BlockTemplate {
        private final BlockState state;

        public Simple(BlockState state) {
            this.state = state;
        }

        @Override
        public BlockState getBlockState(Random random) {
            return state;
        }

        @Override
        public void process(StructureWorldAccess world, Random random, BlockPos pos, Direction direction) {
        }
    }

    private static class RandomChoice extends BlockTemplate {
        private final List<? extends BlockTemplate> choices;
        private final int emptyWeight;

        public RandomChoice(List<? extends BlockTemplate> choices, int emptyWeight) {
            this.choices = choices;
            this.emptyWeight = emptyWeight;
        }

        @Override
        public BlockState getBlockState(Random random) {
            int choice = random.nextInt(this.choices.size() + emptyWeight);
            return choice < this.choices.size() ? this.choices.get(choice).getBlockState(random) : null;
        }

        @Override
        public void process(StructureWorldAccess world, Random random, BlockPos pos, Direction direction) {
        }
    }

    private static class ProcessWorld extends BlockTemplate {
        private final BlockTemplate template;
        private final Consumer<ProcessContext> worldFunction;

        private ProcessWorld(BlockTemplate template, Consumer<ProcessContext> worldFunction) {
            this.template = template;
            this.worldFunction = worldFunction;
        }

        @Override
        public BlockState getBlockState(Random random) {
            return template.getBlockState(random);
        }

        @Override
        public void process(StructureWorldAccess world, Random random, BlockPos pos, Direction direction) {
            this.worldFunction.accept(new ProcessContext(world, random, pos, direction));
        }
    }

    public record ProcessContext(StructureWorldAccess world, Random random, BlockPos pos, Direction direction) {}

    private static class ProcessBlockEntity extends BlockTemplate {
        private final BlockTemplate baseTemplate;
        private final BiConsumer<BlockEntity, Random> blockEntityFunction;

        private ProcessBlockEntity(BlockTemplate baseTemplate, BiConsumer<BlockEntity, Random> blockEntityFunction) {
            this.baseTemplate = baseTemplate;
            this.blockEntityFunction = blockEntityFunction;
        }

        @Override
        public BlockState getBlockState(Random random) {
            return baseTemplate.getBlockState(random);
        }

        @Override
        public void process(StructureWorldAccess world, Random random, BlockPos pos, Direction direction) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity != null) this.blockEntityFunction.accept(entity, random);
        }
    }
}
