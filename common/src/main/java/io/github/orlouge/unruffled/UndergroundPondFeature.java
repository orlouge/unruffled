package io.github.orlouge.unruffled;

import com.mojang.serialization.Codec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class UndergroundPondFeature extends Feature<DefaultFeatureConfig> {
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
    public static final BlockState[] SURFACE_BLOCKS = {
            Blocks.MOSS_BLOCK.getDefaultState(),
            Blocks.MOSS_BLOCK.getDefaultState(),
            Blocks.MOSS_BLOCK.getDefaultState(),
            Blocks.MOSS_BLOCK.getDefaultState(),
            Blocks.ANDESITE.getDefaultState(),
            Blocks.PACKED_MUD.getDefaultState()
    };
    public static final BlockState[] SUBMERGED_BLOCKS = {
            Blocks.MOSS_BLOCK.getDefaultState(),
            Blocks.MUD.getDefaultState(),
            Blocks.GRAVEL.getDefaultState(),
            Blocks.GRAVEL.getDefaultState()
    };
    public static final BlockState[] CEILING_BLOCKS = {
            Blocks.HANGING_ROOTS.getDefaultState(),
            Blocks.HANGING_ROOTS.getDefaultState(),
            Blocks.CAVE_VINES.getDefaultState().with(CaveVines.BERRIES, true),
            Blocks.CAVE_VINES.getDefaultState(),
            Blocks.CAVE_VINES.getDefaultState(),
            Blocks.POINTED_DRIPSTONE.getDefaultState().with(PointedDripstoneBlock.VERTICAL_DIRECTION, Direction.DOWN),
            Blocks.POINTED_DRIPSTONE.getDefaultState().with(PointedDripstoneBlock.VERTICAL_DIRECTION, Direction.DOWN),
            Blocks.SPORE_BLOSSOM.getDefaultState()
    };
    public static final BlockState[] WALL_BLOCKS = {
            Blocks.ROOTED_DIRT.getDefaultState(),
            Blocks.DRIPSTONE_BLOCK.getDefaultState(),
            Blocks.DRIPSTONE_BLOCK.getDefaultState(),
            Blocks.ANDESITE.getDefaultState(),
            Blocks.PACKED_MUD.getDefaultState()
    };
    public static final BlockState[] FLOOR_BLOCKS = {
            Blocks.MOSS_CARPET.getDefaultState(),
            Blocks.ANDESITE_SLAB.getDefaultState(),
            Blocks.GRASS.getDefaultState()
    };
    public static final BlockState[] SUBMERGED_FLOOR_BLOCKS = {
            Blocks.SEAGRASS.getDefaultState(),
            Blocks.POINTED_DRIPSTONE.getDefaultState().with(PointedDripstoneBlock.WATERLOGGED, true),
            Blocks.ANDESITE_SLAB.getDefaultState().with(SlabBlock.WATERLOGGED, true)
    };
    public static final int WATER_DEPTH = 4, MIN_RADIUS = 5, MAX_RADIUS = 11, HEIGHT = 5, MAX_AIR_INTERSECTION = 1;

    public UndergroundPondFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        BlockPos origin = context.getOrigin();

        movedown:
        for (int tries = 0; tries < 20 && origin.getY() > WATER_DEPTH; tries++) {
            //System.out.println(tries + ": " + origin);
            for (int axis = 0; axis <= 1; axis++) {
                for (int edge = -1; edge <= 1; edge += 2) {
                    for (int i = -MIN_RADIUS; i <= MIN_RADIUS; i++) {
                        int x = axis * (MIN_RADIUS * edge) + (1 - axis) * i;
                        int z = (1 - axis) * (MIN_RADIUS * edge) + axis * i;
                        int airY = 1;
                        for (int y = 0; y >= -WATER_DEPTH - 1 && y >= -origin.getY() - WATER_DEPTH; y--) {
                            BlockState state = world.getBlockState(origin.add(x, y, z));
                            if (state.isAir() || state.isLiquid()) {
                                airY = y;
                            }
                        }
                        if (airY <= 0) {
                            origin = origin.add(0, airY - 1, 0);
                            continue movedown;
                        }
                    }
                }
            }
            break;
        }

        int airBlocks = 0;
        int[] openings = new int[2 * MAX_RADIUS + 4];
        int maxRadius = MAX_RADIUS;
        outer:
        for (int distance = MIN_RADIUS; distance <= MAX_RADIUS + 1; distance++) {
            for (int axis = 0; axis <= 1; axis++) {
                for (int edge = -1; edge <= 1; edge += 2) {
                    for (int i = -distance; i <= distance; i++) {
                        int x = axis * (distance * edge) + (1 - axis) * i;
                        int z = (1 - axis) * (distance * edge) + axis * i;
                        int sqRadius = x * x + z * z;
                        for (int y = -WATER_DEPTH - 1; y <= HEIGHT + 1; y++) {
                            BlockState state = world.getBlockState(origin.add(x, y, z));
                            if ((state.isLiquid() || !canReplace(state)) && sqRadius <= (MAX_RADIUS + 1) * (MAX_RADIUS + 1)) {
                                maxRadius = Math.min(maxRadius, distance - 1);
                                break outer;
                            }
                            if (state.isAir()) {
                                if (y <= 0) {
                                    maxRadius = Math.min(maxRadius, distance - 1);
                                    break outer;
                                } else {
                                    airBlocks++;
                                    if (y < HEIGHT - 1) {
                                        openings[(int) Math.ceil(Math.sqrt(sqRadius))]++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (airBlocks > 45 + MAX_AIR_INTERSECTION * distance * distance) {
                maxRadius = Math.min(maxRadius, distance - 1);
                break;
            }
            if (openings[distance] > 10 && random.nextBoolean()) {
                maxRadius = Math.min(maxRadius, distance);
                break;
            }
        }

        if (maxRadius < MIN_RADIUS) return false;
        int radius = MIN_RADIUS;
        if (maxRadius > MIN_RADIUS) {
            if (openings[maxRadius] > 5) {
                //for (radius = maxRadius; radius > MIN_RADIUS; radius--) {
                //    if (openings[radius - 1] < 1 + openings[maxRadius] / 2 || random.nextBoolean()) break;
                //}
                radius = maxRadius;
                //System.out.println("radius: " + radius + ", airblocks:" + airBlocks + ", openings: " + openings[radius]);
            } else {
                radius = random.nextBetween(-maxRadius * 10, maxRadius);
                if (radius < MIN_RADIUS) return false;
                //System.out.println("radius: " + radius + ", airblocks:" + airBlocks);
            }
        }


        for (int y = world.getSeaLevel(); y < world.getHeight(); y++) {
            if (world.getBlockState(new BlockPos(origin.getX(), y, origin.getZ())).isAir()) {
                BlockPos surfacePos = new BlockPos(origin.getX(), y - 1, origin.getZ());
                BlockState surfaceBlock = world.getBlockState(surfacePos);
                boolean replace = true;
                if (surfaceBlock.isOf(Blocks.GRAVEL)) {
                    this.setBlockStateIf(world, surfacePos, Blocks.SUSPICIOUS_GRAVEL.getDefaultState(), this::canReplace);
                } else if (surfaceBlock.isOf(Blocks.SAND)) {
                    this.setBlockStateIf(world, surfacePos, Blocks.SUSPICIOUS_SAND.getDefaultState(), this::canReplace);
                } else {
                    replace = false;
                }
                if (replace) {
                    BlockEntity blockEntity = world.getBlockEntity(surfacePos);
                    if (blockEntity instanceof BrushableBlockEntity) {
                        ((BrushableBlockEntity) blockEntity).setLootTable(new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond"), random.nextLong());
                    }
                }
                break;
            }
        }

        float[][] rawHeight = new float[radius * 2 + 3][radius * 2 + 3];
        float[][] rawDepth = new float[radius * 2 + 3][radius * 2 + 3];

        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                int sqRadius = x * x + z * z;
                if (sqRadius > radius * radius) continue;

                int height = 1;
                for (; height <= HEIGHT - 1; height++) {
                    double stopChance = Math.pow((double) sqRadius / (radius * radius), 1.5f);
                    if (random.nextDouble() < stopChance) break;
                }
                rawHeight[x + radius + 1][z + radius + 1] = height;

                int depth = 0;
                for (; depth < WATER_DEPTH; depth++) {
                    if (depth > height) break;
                    double stopChance = 0.1 + Math.pow((double) Math.max(0, sqRadius - 4) / ((radius - 2) * (radius - 4)), 1.5f);
                    if (random.nextDouble() < stopChance) break;
                }
                rawDepth[x + radius + 1][z + radius + 1] = depth;
            }
        }

        int springs = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int sqRadius = x * x + z * z;
                if (sqRadius > radius * radius) continue;

                float heightSum = rawHeight[x + radius + 1][z + radius + 1] + rawHeight[x - 1 + radius + 1][z + radius + 1] + rawHeight[x + 1 + radius + 1][z + radius + 1] + rawHeight[x + radius + 1][z - 1 + radius + 1] + rawHeight[x + radius + 1][z + 1 + radius + 1];
                int height = Math.round(heightSum / 5);

                float depthSum = rawDepth[x + radius + 1][z + radius + 1] + rawDepth[x - 1 + radius + 1][z + radius + 1] + rawDepth[x + 1 + radius + 1][z + radius + 1] + rawDepth[x + radius + 1][z - 1 + radius + 1] + rawDepth[x + radius + 1][z + 1 + radius + 1] + rawDepth[x + 1 + radius + 1][z + 1 + radius + 1] + rawDepth[x + 1 + radius + 1][z - 1 + radius + 1] + rawDepth[x - 1 + radius + 1][z + 1 + radius + 1] + rawDepth[x - 1 + radius + 1][z - 1 + radius + 1];
                int depth = Math.round(depthSum / 9);

                if (height > 0) {
                    for (int y = 1; y < height; y++) {
                        this.setBlockStateIf(world, origin.add(x, y, z), CAVE_AIR, this::canReplace);
                    }
                }
                boolean lastBlockIsAir = false;
                for (int y = height; y <= HEIGHT; y++) {
                    lastBlockIsAir = world.getBlockState(origin.add(x, y, z)).isAir();
                    if (lastBlockIsAir) continue;
                    this.setRandomBlock(world, random, origin.add(x, y, z), WALL_BLOCKS, 1);
                    if (random.nextInt(2) == 0) break;
                }
                if (!lastBlockIsAir) {
                    if (height == HEIGHT - 1 && depth > 1 && random.nextInt(springs * radius + openings[radius]) == 0) {
                        this.setBlockStateIf(world, origin.add(x, HEIGHT - 1, z), CAVE_AIR, this::canReplace);
                        this.setBlockStateIf(world, origin.add(x, HEIGHT, z), Blocks.WATER.getDefaultState(), this::canReplace);
                        world.scheduleFluidTick(origin.add(x, HEIGHT, z), Fluids.WATER, 0);
                        springs += 2;
                    } else if (height > 2) {
                        this.setRandomBlock(world, random, origin.add(x, height, z), CEILING_BLOCKS, 18);
                    }
                }

                if (depth == 0) {
                    this.setRandomBlock(world, random, origin.add(x, 0, z), SURFACE_BLOCKS, 1);
                    this.setRandomBlock(world, random, origin.add(x, 1, z), FLOOR_BLOCKS, 3);
                } else {
                    for (int y = 0; y < depth; y++) {
                        this.setBlockStateIf(world, origin.add(x, -y, z), Blocks.WATER.getDefaultState(), this::canReplace);
                    }
                    for (int y = depth; y <= WATER_DEPTH; y++) {
                        BlockPos floor = origin.add(x, -y, z);
                        if (random.nextInt(2 + (WATER_DEPTH - depth + 1) / 2) == 0) {
                            this.setBlockStateIf(world, floor, Blocks.SUSPICIOUS_GRAVEL.getDefaultState(), this::canReplace);
                            BlockEntity blockEntity = world.getBlockEntity(floor);
                            if (blockEntity instanceof BrushableBlockEntity) {
                                ((BrushableBlockEntity) blockEntity).setLootTable(new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond"), random.nextLong());
                            }
                        } else {
                            this.setRandomBlock(world, random, floor, SUBMERGED_BLOCKS, 1);
                        }
                        if (random.nextInt(2) == 0) break;
                    }
                    if (depth > 2) {
                        this.setRandomBlock(world, random, origin.add(x, -depth + 1, z), SUBMERGED_FLOOR_BLOCKS, 1);
                    }
                }
            }
        }

        return true;
    }

    private boolean canReplace(BlockState state) {
        return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    protected void setRandomBlock(StructureWorldAccess world, Random random, BlockPos pos, BlockState[] states, int fail) {
        int i = random.nextInt(states.length + fail);
        if (i < states.length) {
            this.setBlockStateIf(world, pos, states[i], this::canReplace);
        }
    }
}
