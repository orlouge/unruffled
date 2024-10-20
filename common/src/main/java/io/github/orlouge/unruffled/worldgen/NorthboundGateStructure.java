package io.github.orlouge.unruffled.worldgen;

import com.mojang.serialization.Codec;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.utils.BlockTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.WallShape;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class NorthboundGateStructure extends Structure {
    public static final Codec<NorthboundGateStructure> CODEC = createCodec(NorthboundGateStructure::new);

    protected NorthboundGateStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG, collector -> collector.addPiece(new Piece(context.chunkPos())));
    }

    @Override
    public StructureType<?> getType() {
        return UnruffledMod.NORTHBOUND_GATE_STRUCTURE;
    }

    public static class Piece extends StructurePiece {
        public static final int XEXT = 1, ZEXT = 3;

        public Piece(NbtCompound nbt) {
            super(UnruffledMod.NORTHBOUND_GATE_STRUCTURE_PIECE, nbt);
        }

        public Piece(ChunkPos chunkPos) {
            super(UnruffledMod.NORTHBOUND_GATE_STRUCTURE_PIECE, 0, getBox(chunkPos));
        }

        private static BlockBox getBox(ChunkPos chunkPos) {
            BlockPos center = chunkPos.getBlockPos(9, 90, 9);
            return new BlockBox(center.getX() - 5 - XEXT, center.getY(), center.getZ() - 3 - ZEXT, center.getX() + 5 + XEXT, center.getY(), center.getZ() + 3 + ZEXT);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int x1 = this.getBoundingBox().getMinX() + XEXT, x2 = this.getBoundingBox().getMaxX() - XEXT;
            int z1 = this.getBoundingBox().getMinZ() + ZEXT, z2 = this.getBoundingBox().getMaxZ() - ZEXT;
            int minTopY = Math.min(
                Math.min(world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x1, z1), world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x2, z2)),
                Math.min(world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x1, z2), world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x2, z1))
            );
            int topY = Math.max(
                Math.max(world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x1, z1), world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x2, z2)),
                Math.max(world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x1, z2), world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x2, z1))
            );
            topY = Math.min(minTopY + 4, topY);
            boolean fillWithSand = world.getBlockState(new BlockPos(x1, world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x1, z1), z1)).isOf(Blocks.GRAVEL);
            fillWithSand |= world.getBlockState(new BlockPos(x2, world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x2, z2), z2)).isOf(Blocks.GRAVEL);
            int floorY = minTopY - 7 - random.nextInt(2);
            int structureTopY = floorY + 5;
            for (int z = z1 - ZEXT; z <= z2 + ZEXT; z++) {
                int zoff = z - z1;
                BlockTemplate[][] zRow = zoff >= 0 && zoff < BLOCKS_ZYX.length ? BLOCKS_ZYX[zoff] : null;
                for (int x = x1 - XEXT; x <= x2 + XEXT; x++) {
                    int xoff = x - x1;
                    int gravelHeight = topY - Math.max(Math.abs(xoff - 5), Math.abs(zoff - 3)) * (topY - floorY) / 6 + random.nextInt(3);
                    int exposedGravelHeight = gravelHeight - (topY - structureTopY) + random.nextInt(3);
                    for (int y = floorY; y <= topY; y++) {
                        int yoff = y - floorY;
                        float suspiciousChance = 0.3f / Math.max(1, yoff - 2) + (y >= minTopY ? 0.35f / (1 + topY - y) : 0f);
                        BlockTemplate template = null;
                        if (zRow != null && yoff >= 0 && yoff < zRow.length && xoff >= 0 && xoff < zRow[yoff].length) {
                            template = zRow[yoff][xoff];
                        }
                        boolean fillSolid = y <= gravelHeight;
                        boolean fillAir = y <= exposedGravelHeight;
                        boolean support = false;
                        BlockPos pos = new BlockPos(x, y, z);
                        if (template != null) {
                            BlockState state = template.getBlockState(random);
                            if (state != null) {
                                setBlockStateIf(world, pos, state, this::canReplace);
                                if (state.isOf(Blocks.GRAVEL) || state.isOf(Blocks.SAND) || state.isOf(Blocks.SUSPICIOUS_GRAVEL) || state.isOf(Blocks.SUSPICIOUS_SAND)) {
                                    support = true;
                                }
                            } else {
                                template = null;
                            }
                        }
                        if (template == null) {
                            BlockState oldState = world.getBlockState(pos);
                            if ((fillAir && oldState.isAir()) || (fillSolid && !oldState.isAir())) {
                                if (random.nextFloat() <= suspiciousChance) {
                                    template = BlockTemplate.lootBrushable(
                                        (fillWithSand ^ (random.nextInt(8) == 0)) ? Blocks.SUSPICIOUS_SAND : Blocks.SUSPICIOUS_GRAVEL,
                                        UnruffledMod.NORTHBOUND_GATE_LOOT_TABLE
                                    );
                                } else if (world.getBlockState(pos.add(0, 1, 0)).isOf(Blocks.SNOW) && random.nextFloat() > 0.5) {
                                    template = BlockTemplate.block(Blocks.DIRT_PATH);
                                    world.setBlockState(pos.add(0, 1, 0), Blocks.AIR.getDefaultState(), 2);
                                } else if (oldState.isOf(Blocks.SNOW_BLOCK) || oldState.isOf(Blocks.SNOW) && random.nextFloat() > 0.5) {
                                    template = BlockTemplate.block(Blocks.DIRT_PATH);
                                } else {
                                    //template = BlockTemplate.block(fillWithSand ? Blocks.SAND : Blocks.GRAVEL);
                                    template = BlockTemplate.random(FILL_BLOCKS);
                                }
                                BlockState state = template.getBlockState(random);
                                if (state == null) template = null;
                                else {
                                    setBlockStateIf(world, pos, state, this::canReplace);
                                    if (state.isOf(Blocks.GRAVEL) || state.isOf(Blocks.SAND) || state.isOf(Blocks.SUSPICIOUS_GRAVEL) || state.isOf(Blocks.SUSPICIOUS_SAND)) {
                                        support = true;
                                    }
                                }
                            }
                        }
                        if (template != null) {
                            template.process(world, random, pos, Direction.NORTH);
                        }
                        if (yoff == 0 && !world.getBlockState(pos.add(0, -1, 0)).isSolid() && (support || random.nextInt(2 + gravelHeight) > 1)) {
                            setBlockStateIf(world, pos.add(0, -1, 0), Blocks.STONE.getDefaultState(), this::canReplace);
                        }
                    }
                }
            }
        }

        @Override
        protected boolean canReplace(BlockState state) {
            return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
        }

        protected void setBlockStateIf(StructureWorldAccess world, BlockPos pos, BlockState state, Predicate<BlockState> predicate) {
            if (predicate.test(world.getBlockState(pos))) {
                world.setBlockState(pos, state, 2);
            }
        }

        private static final BlockState[] FLOOR_BLOCKS = new BlockState[] {
            Blocks.GRAVEL.getDefaultState(),
            Blocks.COBBLESTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState(),
            Blocks.COARSE_DIRT.getDefaultState(),
            Blocks.MOSS_BLOCK.getDefaultState()
        };

        private static final BlockState[] FILL_BLOCKS = new BlockState[] {
            Blocks.GRAVEL.getDefaultState(),
            Blocks.SAND.getDefaultState(),
            Blocks.COARSE_DIRT.getDefaultState(),
            Blocks.MUD.getDefaultState()
        };

        public static final BlockState[] SLABS = new BlockState[] {
            Blocks.STONE_BRICK_SLAB.getDefaultState(),
            Blocks.STONE_BRICK_SLAB.getDefaultState(),
            Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState(),
            Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE_SLAB.getDefaultState()
        };

        public static final BlockState[] SOUTH_STAIRS = new BlockState[] {
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH),
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH),
            Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH),
        };

        public static final BlockState[] NORTH_STAIRS = new BlockState[] {
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH),
            Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH),
        };

        public static final BlockState[] WEST_STAIRS = new BlockState[] {
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST),
            Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST),
        };

        public static final BlockState[] EAST_STAIRS = new BlockState[] {
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST),
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST),
            Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST),
        };


        public static final BlockState[] TOP_WEST_STAIRS = new BlockState[] {
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, BlockHalf.TOP),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, BlockHalf.TOP),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, BlockHalf.TOP),
            Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, BlockHalf.TOP),
        };

        public static final BlockState[] TOP_EAST_STAIRS = new BlockState[] {
            Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, BlockHalf.TOP),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, BlockHalf.TOP),
            Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, BlockHalf.TOP),
            Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, BlockHalf.TOP),
        };

        public static final BlockState[] SOUTH_WALL = new BlockState[] {
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.SOUTH_SHAPE, WallShape.LOW),
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.SOUTH_SHAPE, WallShape.LOW),
            Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState().with(WallBlock.SOUTH_SHAPE, WallShape.LOW),
            Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState().with(WallBlock.SOUTH_SHAPE, WallShape.LOW),
        };

        public static final BlockState[] WEST_WALL = new BlockState[] {
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW),
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW),
            Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW),
            Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW),
        };

        public static final BlockState[] EAST_WALL = new BlockState[] {
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW),
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW),
            Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW),
            Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW),
        };

        public static final BlockState[] NORTH_WEST_WALL = new BlockState[] {
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
            Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
            Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState().with(WallBlock.WEST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
        };

        public static final BlockState[] NORTH_EAST_WALL = new BlockState[] {
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
            Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
            Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.NORTH_SHAPE, WallShape.TALL),
        };

        public static final BlockState[] EAST_WEST_WALL = new BlockState[] {
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.WEST_SHAPE, WallShape.LOW),
            Blocks.STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.WEST_SHAPE, WallShape.LOW),
            Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.WEST_SHAPE, WallShape.LOW),
            Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState().with(WallBlock.EAST_SHAPE, WallShape.LOW).with(WallBlock.WEST_SHAPE, WallShape.LOW),
        };

        public static final BlockState[] BRICKS = new BlockState[] {
            Blocks.MOSS_BLOCK.getDefaultState(),
            Blocks.STONE_BRICKS.getDefaultState(),
            Blocks.STONE_BRICKS.getDefaultState(),
            Blocks.MOSSY_STONE_BRICKS.getDefaultState(),
            Blocks.CRACKED_STONE_BRICKS.getDefaultState(),
            Blocks.CRACKED_STONE_BRICKS.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState()
        };

        private static final BlockTemplate[][][] BLOCKS_ZYX = new BlockTemplate[][][] {
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    BlockTemplate.random(FLOOR_BLOCKS, 5),
                    BlockTemplate.random(FLOOR_BLOCKS, 5),
                    BlockTemplate.random(FLOOR_BLOCKS, 4),
                    BlockTemplate.random(FLOOR_BLOCKS, 4),
                    BlockTemplate.random(FLOOR_BLOCKS, 3),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 3),
                    BlockTemplate.random(FLOOR_BLOCKS, 4),
                    BlockTemplate.random(FLOOR_BLOCKS, 5),
                    BlockTemplate.random(FLOOR_BLOCKS, 5)
                }
            },
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    BlockTemplate.random(FLOOR_BLOCKS, 4),
                    BlockTemplate.random(FLOOR_BLOCKS, 4),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 4),
                    BlockTemplate.random(FLOOR_BLOCKS, 4)
                }
            },
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    null, null, null,
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    null, null, null
                },
                new BlockTemplate[] {
                    null, BlockTemplate.random(SLABS), null,
                    BlockTemplate.random(SOUTH_WALL), null, null, null, BlockTemplate.random(SOUTH_WALL),
                    null, BlockTemplate.random(SLABS), null,
                },
                new BlockTemplate[] {
                    null, null, null,
                    BlockTemplate.random(SOUTH_STAIRS), null, null, null, BlockTemplate.random(SOUTH_STAIRS),
                    null, null, null,
                }
            },
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    null, null, null, null,
                    BlockTemplate.random(FLOOR_BLOCKS),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    null, null, null, null
                },
                new BlockTemplate[] {
                    BlockTemplate.random(SLABS),
                    BlockTemplate.random(BRICKS),
                    BlockTemplate.random(NORTH_STAIRS),
                    BlockTemplate.random(BRICKS),
                    null, null, null,
                    BlockTemplate.random(BRICKS),
                    BlockTemplate.random(NORTH_STAIRS),
                    BlockTemplate.random(BRICKS),
                    BlockTemplate.random(SLABS)
                },
                new BlockTemplate[] {
                    null,
                    BlockTemplate.random(EAST_WALL),
                    BlockTemplate.random(EAST_WEST_WALL),
                    BlockTemplate.random(NORTH_EAST_WALL),
                    null, null, null,
                    BlockTemplate.random(WEST_WALL),
                    BlockTemplate.random(EAST_WEST_WALL),
                    BlockTemplate.random(NORTH_WEST_WALL),
                    null
                },
                new BlockTemplate[] {
                    null, null, null,
                    BlockTemplate.random(WEST_STAIRS),
                    BlockTemplate.random(TOP_WEST_STAIRS),
                    null,
                    BlockTemplate.random(TOP_EAST_STAIRS),
                    BlockTemplate.random(EAST_STAIRS),
                    null, null, null
                },
                new BlockTemplate[] {
                    null, null, null, null,
                    BlockTemplate.random(SLABS),
                    BlockTemplate.block(Blocks.LODESTONE),
                    BlockTemplate.random(SLABS),
                    null, null, null, null,
                }
            },
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    null, null,
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    null, null
                },
                new BlockTemplate[] {
                    BlockTemplate.random(BRICKS),
                    BlockTemplate.random(SLABS),
                    null, null, null, null, null, null, null,
                    BlockTemplate.random(SLABS),
                    BlockTemplate.random(BRICKS)
                }
            },
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    null,
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    null
                },
                new BlockTemplate[] {
                    BlockTemplate.random(SLABS),
                    null, null, null, null, null, null, null, null, null,
                    BlockTemplate.random(SLABS),
                }
            },
            new BlockTemplate[][] {
                new BlockTemplate[] {
                    BlockTemplate.random(FLOOR_BLOCKS, 5),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 1),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2),
                    BlockTemplate.random(FLOOR_BLOCKS, 2)
                }
            }
        };
    }
}
