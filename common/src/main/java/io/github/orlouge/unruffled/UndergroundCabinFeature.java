package io.github.orlouge.unruffled;

import com.mojang.serialization.Codec;
import io.github.orlouge.unruffled.items.AncientCodexItem;
import io.github.orlouge.unruffled.items.CustomItems;
import io.github.orlouge.unruffled.items.ItemEnchantmentsHelper;
import io.github.orlouge.unruffled.utils.BlockTemplate;
import io.github.orlouge.unruffled.utils.WeightedRandomList;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.enums.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.decoration.painting.PaintingVariants;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.*;

public class UndergroundCabinFeature extends Feature<DefaultFeatureConfig> {

    public static final int WALL_RADIUS = 3, FLOOR_HEIGHT = 0, CEILING_HEIGHT = 4;

    public static final BlockState[] WALL_BLOCKS = {
        Blocks.DEEPSLATE_BRICKS.getDefaultState(),
        Blocks.DEEPSLATE_BRICKS.getDefaultState(),
        Blocks.COBBLED_DEEPSLATE.getDefaultState(),
        Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(),
        Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
    };

    public static final BlockState[] FLOOR_BLOCKS = {
        Blocks.STRIPPED_DARK_OAK_WOOD.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X),
        Blocks.STRIPPED_DARK_OAK_WOOD.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z),
        Blocks.DARK_OAK_WOOD.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X),
        Blocks.DARK_OAK_WOOD.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z),
        Blocks.MANGROVE_WOOD.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X),
        Blocks.MANGROVE_WOOD.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z)
    };

    public static final BlockState[] FLOOR_DECORATION = {
        Blocks.REDSTONE_WIRE.getDefaultState(),
        Blocks.REDSTONE_WIRE.getDefaultState(),
        Blocks.REDSTONE_WIRE.getDefaultState(),
        Blocks.REDSTONE_WIRE.getDefaultState(),
        Blocks.REDSTONE_WIRE.getDefaultState(),
        Blocks.REDSTONE_WIRE.getDefaultState(),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE),
        Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 1),
        //Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 2),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 3),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 5),
        //Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 6),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 7),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 9),
        //Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 10),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 11),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 13),
        //Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 14),
        Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 15),
        Blocks.COBWEB.getDefaultState(),
        Blocks.COBWEB.getDefaultState(),
        Blocks.COBWEB.getDefaultState(),
        Blocks.COBWEB.getDefaultState(),
        Blocks.POINTED_DRIPSTONE.getDefaultState().with(PointedDripstoneBlock.VERTICAL_DIRECTION, Direction.UP),
        Blocks.POINTED_DRIPSTONE.getDefaultState().with(PointedDripstoneBlock.VERTICAL_DIRECTION, Direction.UP)
    };

    public static final BlockState[] CEILING_DECORATION = {
        Blocks.COBWEB.getDefaultState(),
        Blocks.COBWEB.getDefaultState(),
        Blocks.GLOW_LICHEN.getDefaultState().with(GlowLichenBlock.getProperty(Direction.UP), true)
    };

    public static final WallDecorationTemplate BREWING_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[]{
                BlockTemplate.block(Blocks.SPRUCE_FENCE_GATE.getDefaultState().with(FenceGateBlock.FACING, Direction.NORTH)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.EAST))
            },
            new BlockTemplate[]{
                BlockTemplate.block(Blocks.SPRUCE_FENCE_GATE.getDefaultState().with(FenceGateBlock.FACING, Direction.NORTH)),
                BlockTemplate.block(Blocks.BREWING_STAND),
                BlockTemplate.lootContainer(Blocks.HOPPER.getDefaultState().with(HopperBlock.FACING, Direction.DOWN), new Identifier(UnruffledMod.MOD_ID, "chests/fermented_spider_eyes"))
            },
            new BlockTemplate[]{
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/assorted_potions")),
                BlockTemplate.block(Blocks.HOPPER.getDefaultState().with(HopperBlock.FACING, Direction.WEST).with(HopperBlock.ENABLED, false)),
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/mundane_potions")),
            },
            new BlockTemplate[]{
                null,
                BlockTemplate.block(Blocks.LEVER.getDefaultState().with(LeverBlock.FACING, Direction.SOUTH).with(LeverBlock.POWERED, true))
            },
            new BlockTemplate[]{},
            new BlockTemplate[]{
                null,
                null,
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.SOUTH))
            },
        }
    );

    public static final WallDecorationTemplate CAVE_VINES_ROOM = new FixedWallTemplate(
        new BlockTemplate[][] {
            new BlockTemplate[] {null, null, BlockTemplate.block(Blocks.CAVE_VINES.getDefaultState())},
            new BlockTemplate[] {
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/glow_berries")),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.NORTH))
            },
            new BlockTemplate[] {
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/golden_berries")),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.NORTH))
            },
            new BlockTemplate[] {null, null, BlockTemplate.block(Blocks.CAVE_VINES.getDefaultState())},
            new BlockTemplate[] {null, null, BlockTemplate.block(Blocks.CAVE_VINES.getDefaultState())},
            new BlockTemplate[] {null, null, BlockTemplate.block(Blocks.CAVE_VINES.getDefaultState())},
        }
    );

    public static final WallDecorationTemplate TNT_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.ASCENDING_EAST))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.TNT),
                BlockTemplate.sideEffect(Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.EAST_WEST),
                        (world, pos) -> world.spawnEntity(new TntMinecartEntity(world.toServerWorld(), (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5))
                ),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.WEST))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.TNT),
                BlockTemplate.block(Blocks.TNT),
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/explosives")),
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.TNT),
                BlockTemplate.block(Blocks.TNT),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.EAST))
            },
            new BlockTemplate[] {},
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.TNT),
                BlockTemplate.block(Blocks.LEVER.getDefaultState().with(LeverBlock.FACING, Direction.SOUTH).with(LeverBlock.POWERED, false)),
                BlockTemplate.block(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(WallSignBlock.FACING, Direction.SOUTH))
            },
        }
    );

    public static final WallDecorationTemplate CART_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[]{
                BlockTemplate.block(Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.EAST_WEST)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.WEST))
            },
            new BlockTemplate[]{
                BlockTemplate.sideEffect(Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.SOUTH_WEST),
                    (world, pos) -> world.spawnEntity(new MinecartEntity(world.toServerWorld(), (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5))
                ),
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.block(Blocks.BLACK_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2))
            },
            new BlockTemplate[]{
                BlockTemplate.block(Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.ASCENDING_EAST)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.EAST))
            },
            new BlockTemplate[]{
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/rails")),
                BlockTemplate.block(Blocks.POWERED_RAIL.getDefaultState().with(PoweredRailBlock.SHAPE, RailShape.EAST_WEST))
            },
            new BlockTemplate[]{
                BlockTemplate.block(Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.NORTH_SOUTH)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.SOUTH))
            },
        }
    );

    public static final WallDecorationTemplate CAULDRON_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.WEST).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.WEST))
            },
            new BlockTemplate[] {
                BlockTemplate.random(new BlockState[] {Blocks.CAULDRON.getDefaultState(), Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 1), Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3)}),
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.block(Blocks.BLACK_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 1))
            },
            new BlockTemplate[] {
                BlockTemplate.random(new BlockState[] {Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 2), Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3)}),
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.block(Blocks.BLACK_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.EAST).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.EAST))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.SOUTH))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.SOUTH))
            }
        }
    );

    public static final WallDecorationTemplate FURNACE_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {BlockTemplate.block(Blocks.COAL_BLOCK)},
            new BlockTemplate[] {BlockTemplate.block(Blocks.COAL_BLOCK), BlockTemplate.block(Blocks.COAL_BLOCK)},
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.BLAST_FURNACE.getDefaultState().with(BlastFurnaceBlock.FACING, Direction.SOUTH)),
                BlockTemplate.block(Blocks.BLAST_FURNACE.getDefaultState().with(BlastFurnaceBlock.FACING, Direction.SOUTH)),
                BlockTemplate.block(Blocks.COAL_BLOCK)
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.BLAST_FURNACE.getDefaultState().with(BlastFurnaceBlock.FACING, Direction.SOUTH)),
                BlockTemplate.block(Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.SOUTH))
            },
            new BlockTemplate[] {BlockTemplate.block(Blocks.COAL_BLOCK)},
            new BlockTemplate[] {BlockTemplate.block(Blocks.COAL_BLOCK)}
        }
    );

    public static final WallDecorationTemplate SMITHING_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.sideEffect(BlockTemplate.empty(), (ctx) -> {
                    Vec3d center = ctx.pos().toCenterPos();
                    ArmorStandEntity armorStand = new ArmorStandEntity(ctx.world().toServerWorld(), center.getX(), ctx.pos().getY(), center.getZ());
                    armorStand.setRotation(-30 + (float) Math.toDegrees(ctx.direction().getUnitVector().angle(Direction.NORTH.getUnitVector())), 0);
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.HEAD, (ctx.random().nextBoolean() ? Items.DIAMOND_HELMET : Items.IRON_HELMET).getDefaultStack());
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.CHEST, Items.IRON_CHESTPLATE.getDefaultStack());
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.LEGS, Items.CHAINMAIL_LEGGINGS.getDefaultStack());
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.FEET, (ctx.random().nextBoolean() ? Items.DIAMOND_BOOTS : Items.CHAINMAIL_BOOTS).getDefaultStack());
                    ctx.world().spawnEntity(armorStand);
                }),
                null,
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.X))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.CHIPPED_ANVIL.getDefaultState().with(AnvilBlock.FACING, Direction.EAST)),
                BlockTemplate.sideEffect(
                    BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.WEST)),
                    (ctx) -> {
                        ItemFrameEntity itemFrame = new ItemFrameEntity(ctx.world().toServerWorld(), ctx.pos(), Direction.UP);
                        itemFrame.setInvisible(true);
                        itemFrame.setHeldItemStack(switch (ctx.random().nextInt(9)) {
                            case 0 -> Items.IRON_AXE.getDefaultStack();
                            case 1 -> ItemEnchantmentsHelper.createWithItemEnchantments(CustomItems.SACRED_SWORD);
                            case 2 -> Items.DIAMOND_SWORD.getDefaultStack();
                            case 3 -> Items.DIAMOND_AXE.getDefaultStack();
                            default -> Items.IRON_SWORD.getDefaultStack();
                        }, false);
                        itemFrame.setRotation(switch (ctx.direction()) {
                            case NORTH -> 1;
                            case EAST -> 3;
                            case SOUTH -> 5;
                            default -> 7;
                        }, false);
                        ctx.world().spawnEntity(itemFrame);
                    }
                ),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.X))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SMITHING_TABLE),
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/smithing")),
            },
            new BlockTemplate[] {
                BlockTemplate.sideEffect(BlockTemplate.empty(), (ctx) -> {
                    Vec3d center = ctx.pos().toCenterPos();
                    ArmorStandEntity armorStand = new ArmorStandEntity(ctx.world().toServerWorld(), center.getX(), ctx.pos().getY(), center.getZ());
                    armorStand.setRotation(30 + (float) Math.toDegrees(ctx.direction().getUnitVector().angle(Direction.NORTH.getUnitVector())), 0);
                    // TODO: random armor with random trims
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.HEAD, Items.GOLDEN_HELMET.getDefaultStack());
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE.getDefaultStack());
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS.getDefaultStack());
                    if (ctx.random().nextBoolean()) armorStand.equipStack(EquipmentSlot.FEET, Items.LEATHER_BOOTS.getDefaultStack());
                    ctx.world().spawnEntity(armorStand);
                }),
                BlockTemplate.block(Blocks.SPRUCE_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.EAST)),
            },
        }
    );

    public static final WallDecorationTemplate BED_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/bedroom")),
                BlockTemplate.block(Blocks.BLACK_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.BROWN_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.WEST)),
                null,
                BlockTemplate.sideEffect(BlockTemplate.empty(), (ctx) -> {
                    Optional<RegistryEntry.Reference<PaintingVariant>> variant = Registries.PAINTING_VARIANT.getEntry(switch (ctx.random().nextInt(4)) {
                        case 0 -> PaintingVariants.POOL;
                        case 1 -> PaintingVariants.COURBET;
                        case 2 -> PaintingVariants.SUNSET;
                        default -> PaintingVariants.CREEBET;
                    });
                    if (variant.isEmpty()) return;
                    ctx.world().spawnEntity(new PaintingEntity(ctx.world().toServerWorld(), ctx.pos(), ctx.direction().getOpposite(), variant.get()));
                })
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.BROWN_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.WEST)),
            }
    });

    public static final WallDecorationTemplate LODESTONE_ROOM = new FixedWallTemplate(
        new BlockTemplate[][] {
            new BlockTemplate[] {
                BlockTemplate.empty(),
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/lodestone")),
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.POLISHED_ANDESITE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST)),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y)),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y)),
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.LODESTONE),
                BlockTemplate.sideEffect(
                    BlockTemplate.block(Blocks.LIGHT_GRAY_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2)),
                    (ctx) -> {
                        ItemFrameEntity itemFrame = new ItemFrameEntity(ctx.world().toServerWorld(), ctx.pos(), ctx.direction().getOpposite());
                        //itemFrame.setInvisible(true);
                        itemFrame.setHeldItemStack(new ItemStack(Items.COMPASS), false);
                        ctx.world().spawnEntity(itemFrame);
                    }
                )
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.POLISHED_ANDESITE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST)),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y)),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y)),
            }
        }
    );

    public static final WallDecorationTemplate ARCHAEOLOGY_ROOM = new FixedWallTemplate(
        new BlockTemplate[][] {
            new BlockTemplate[] {
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.random(new BlockState[] {Blocks.SAND.getDefaultState()}, 1),
            },
            new BlockTemplate[] {
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.random(new BlockState[] {Blocks.SAND.getDefaultState()}, 1),
            },
            new BlockTemplate[] {
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.random(new BlockState[] {Blocks.SAND.getDefaultState()}, 1),
            },
            new BlockTemplate[] {
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.random(new BlockState[] {Blocks.SAND.getDefaultState()}, 1),
            },
            new BlockTemplate[] {
                BlockTemplate.sideEffect(
                    BlockTemplate.empty(),
                    (ctx) -> {
                        ItemFrameEntity itemFrame = new ItemFrameEntity(ctx.world().toServerWorld(), ctx.pos(), Direction.UP);
                        itemFrame.setInvisible(true);
                        itemFrame.setHeldItemStack(new ItemStack(Items.BRUSH), false);
                        itemFrame.setRotation(ctx.random().nextInt(8), false);
                        ctx.world().spawnEntity(itemFrame);
                    }
                )
            },
            new BlockTemplate[] {
                BlockTemplate.lootBrushable(Blocks.SUSPICIOUS_SAND, new Identifier(UnruffledMod.MOD_ID, "archaeology/underground_pond")),
                BlockTemplate.random(new BlockState[] {Blocks.SAND.getDefaultState()}, 1),
            },
        }
    );

    public static final WallDecorationTemplate LAMP_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.WEST).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.WEST).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.WEST).with(TrapdoorBlock.OPEN, true))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.STICKY_PISTON.getDefaultState().with(PistonBlock.FACING, Direction.UP)),
                BlockTemplate.block(Blocks.REDSTONE_LAMP)
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.LEVER.getDefaultState().with(LeverBlock.FACING, Direction.SOUTH).with(LeverBlock.POWERED, false)),
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/redstone")),
                BlockTemplate.block(Blocks.REDSTONE_BLOCK)
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.EAST).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.EAST).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.EAST).with(TrapdoorBlock.OPEN, true))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true)),
                BlockTemplate.empty(),
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true))
            }
        }
    );

    public static final WallDecorationTemplate AMETHYST_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.random(new BlockState[]{
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.LARGE_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.WEST),
                    Blocks.MEDIUM_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.WEST)
                })
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.AMETHYST_BLOCK),
                BlockTemplate.random(new BlockState[]{
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.SMALL_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.UP),
                })
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.AMETHYST_BLOCK),
                BlockTemplate.block(Blocks.AMETHYST_BLOCK),
                BlockTemplate.random(new BlockState[]{
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.AMETHYST_CLUSTER.getDefaultState().with(AmethystClusterBlock.FACING, Direction.UP),
                })
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.AMETHYST_BLOCK),
                BlockTemplate.random(new BlockState[]{
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.LARGE_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.EAST),
                    Blocks.MEDIUM_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.UP),
                    Blocks.AMETHYST_CLUSTER.getDefaultState().with(AmethystClusterBlock.FACING, Direction.UP)
                })
            },
            new BlockTemplate[] {
                BlockTemplate.random(new BlockState[]{
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.LARGE_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.SOUTH),
                    Blocks.AMETHYST_CLUSTER.getDefaultState().with(AmethystClusterBlock.FACING, Direction.SOUTH)
                })
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.AMETHYST_BLOCK),
                BlockTemplate.random(new BlockState[]{
                    Blocks.AMETHYST_BLOCK.getDefaultState(),
                    Blocks.AMETHYST_CLUSTER.getDefaultState().with(AmethystClusterBlock.FACING, Direction.SOUTH),
                    Blocks.MEDIUM_AMETHYST_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.SOUTH)
                })
            },
        }
    );

    public static final WallDecorationTemplate LIBRARY_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {
                BlockTemplate.sideEffect(
                    BlockTemplate.random(new BlockState[]{ Blocks.BOOKSHELF.getDefaultState(), Blocks.CHISELED_BOOKSHELF.getDefaultState().with(HorizontalFacingBlock.FACING, Direction.SOUTH)}),
                    ctx -> UndergroundCabinFeature.setRandomBooks(ctx.world(), ctx.random(), ctx.pos())
                )
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.BOOKSHELF),
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), UnruffledMod.BOOKSHELF_LOOT_TABLE)
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.BOOKSHELF),
                BlockTemplate.sideEffect(
                    BlockTemplate.random(new BlockState[]{ Blocks.BOOKSHELF.getDefaultState(), Blocks.CHISELED_BOOKSHELF.getDefaultState().with(HorizontalFacingBlock.FACING, Direction.SOUTH)}),
                    ctx -> UndergroundCabinFeature.setRandomBooks(ctx.world(), ctx.random(), ctx.pos())
                )
            },
            new BlockTemplate[] {
                BlockTemplate.sideEffect(
                    BlockTemplate.random(new BlockState[]{ Blocks.BOOKSHELF.getDefaultState(), Blocks.CHISELED_BOOKSHELF.getDefaultState().with(HorizontalFacingBlock.FACING, Direction.SOUTH)}),
                    ctx -> UndergroundCabinFeature.setRandomBooks(ctx.world(), ctx.random(), ctx.pos())
                )
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.LECTERN.getDefaultState().with(LecternBlock.FACING, Direction.SOUTH))
            }
        }
    );

    public static final WallDecorationTemplate TARGET_ROOM = new FixedWallTemplate(
        new BlockTemplate[][]{
            new BlockTemplate[] {},
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.sideEffect(
                    BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.NORTH).with(TrapdoorBlock.HALF, BlockHalf.TOP)),
                    (ctx) -> {
                        ItemFrameEntity itemFrame = new ItemFrameEntity(ctx.world().toServerWorld(), ctx.pos(), Direction.UP);
                        itemFrame.setInvisible(true);
                        itemFrame.setHeldItemStack(new ItemStack(ctx.random().nextBoolean() ? Items.BOW : Items.CROSSBOW), false);
                        itemFrame.setRotation(ctx.random().nextInt(8), false);
                        ctx.world().spawnEntity(itemFrame);
                    }
                ),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y))
            },
            new BlockTemplate[] {
                BlockTemplate.lootContainer(Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.SOUTH), new Identifier(UnruffledMod.MOD_ID, "chests/arrows")),
                BlockTemplate.block(Blocks.TARGET),
                BlockTemplate.block(Blocks.STRIPPED_SPRUCE_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y))
            },
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)),
                BlockTemplate.sideEffect(
                    BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.NORTH).with(TrapdoorBlock.HALF, BlockHalf.TOP)),
                    (ctx) -> {
                        ItemFrameEntity itemFrame = new ItemFrameEntity(ctx.world().toServerWorld(), ctx.pos(), Direction.UP);
                        itemFrame.setInvisible(true);
                        itemFrame.setHeldItemStack(new ItemStack(ctx.random().nextBoolean() ? Items.BOW : Items.CROSSBOW), false);
                        itemFrame.setRotation(ctx.random().nextInt(8), false);
                        ctx.world().spawnEntity(itemFrame);
                    }
                ),
                BlockTemplate.block(Blocks.CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y))
            },
            new BlockTemplate[] {},
            new BlockTemplate[] {
                BlockTemplate.block(Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.FACING, Direction.SOUTH).with(TrapdoorBlock.OPEN, true))
            }
        }
    );

    public static final WeightedRandomList<WallDecorationTemplate> WALL_TEMPLATES = new WeightedRandomList();
    static {
        WALL_TEMPLATES.add(8, BREWING_ROOM);
        WALL_TEMPLATES.add(6, CART_ROOM);
        WALL_TEMPLATES.add(5, BED_ROOM);
        WALL_TEMPLATES.add(4, FURNACE_ROOM);
        WALL_TEMPLATES.add(4, SMITHING_ROOM);
        WALL_TEMPLATES.add(4, LODESTONE_ROOM);
        WALL_TEMPLATES.add(3, ARCHAEOLOGY_ROOM);
        WALL_TEMPLATES.add(2, LIBRARY_ROOM);
        WALL_TEMPLATES.add(2, CAULDRON_ROOM);
        WALL_TEMPLATES.add(2, CAVE_VINES_ROOM);
        WALL_TEMPLATES.add(2, TNT_ROOM);
        WALL_TEMPLATES.add(2, AMETHYST_ROOM);
        WALL_TEMPLATES.add(1, TARGET_ROOM);
        WALL_TEMPLATES.add(1, LAMP_ROOM);

        WALL_TEMPLATES.add(6, new RandomPile(BlockTemplate.random(new BlockState[] {
            Blocks.RAW_IRON_BLOCK.getDefaultState(),
            Blocks.DEEPSLATE_IRON_ORE.getDefaultState(),
            Blocks.DEEPSLATE_IRON_ORE.getDefaultState(),
            Blocks.DEEPSLATE_IRON_ORE.getDefaultState()
        })));
        WALL_TEMPLATES.add(3, new RandomPile(BlockTemplate.random(new BlockState[] {
            Blocks.RAW_COPPER_BLOCK.getDefaultState(),
            Blocks.DEEPSLATE_COPPER_ORE.getDefaultState(),
            Blocks.DEEPSLATE_COPPER_ORE.getDefaultState(),
        })));
        WALL_TEMPLATES.add(3, new RandomPile(BlockTemplate.random(new BlockState[] {
            Blocks.RAW_GOLD_BLOCK.getDefaultState(),
            Blocks.DEEPSLATE_GOLD_ORE.getDefaultState(),
            Blocks.DEEPSLATE_GOLD_ORE.getDefaultState(),
            Blocks.DEEPSLATE_GOLD_ORE.getDefaultState(),
        })));
        WALL_TEMPLATES.add(2, new RandomPile(BlockTemplate.random(new BlockState[] {
            Blocks.BONE_BLOCK.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X),
            Blocks.BONE_BLOCK.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y),
            Blocks.BONE_BLOCK.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z)
        }), BlockTemplate.random(new BlockState[] {
            Blocks.WHITE_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2),
            Blocks.WHITE_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2),
            Blocks.WHITE_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 3),
            Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 6),
            Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 7),
            Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 8),
            Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 9),
            Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 10),
            Blocks.SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, 11),
        }, 3), 2));
        WALL_TEMPLATES.add(4, new RandomPile(BlockTemplate.blockEntity(BlockTemplate.random(new BlockState[] {
            Blocks.DECORATED_POT.getDefaultState().with(DecoratedPotBlock.FACING, Direction.SOUTH),
            Blocks.DECORATED_POT.getDefaultState().with(DecoratedPotBlock.FACING, Direction.WEST),
            Blocks.DECORATED_POT.getDefaultState().with(DecoratedPotBlock.FACING, Direction.EAST)
        }), UndergroundCabinFeature::setRandomSherds), BlockTemplate.random(new BlockState[] {
            Blocks.FLOWER_POT.getDefaultState(),
            Blocks.FLOWER_POT.getDefaultState(),
            Blocks.FLOWER_POT.getDefaultState(),
            Blocks.BLACK_CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2)
        }, 1), 2));
    }

    public UndergroundCabinFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        BlockPos origin = context.getOrigin(), center = origin;
        ChunkPos originChunk = new ChunkPos(origin);
        BlockPos entranceOffset = null;
        openingfound:
        for (int yoff = 0; yoff < Math.min(50, -center.getY()); yoff++) {
            for (int axis = 0; axis <= 1; axis++) {
                for (int edge = -1; edge <= 1; edge += 2) {
                    for (int shift = -16 + WALL_RADIUS + 1; shift <= 16 - WALL_RADIUS - 1; shift++) {
                        for (int depth = WALL_RADIUS + 3; depth <= 16; depth++) {
                            int x = axis * (depth * edge) + (1 - axis) * shift;
                            int z = (1 - axis) * (depth * edge) + axis * shift;
                            int tunnelLength = 2 /* Math.min(3, depth - WALL_RADIUS - 1) */, depthOffset = depth - tunnelLength - WALL_RADIUS - 1;
                            if (!world.getBlockState(center.add(x, yoff, z)).isAir() && world.isAir(center.add(x, yoff + 1, z)) && world.isAir(center.add(x, yoff + 2, z))) {
                                if (axis == 0) {
                                    center = new BlockPos(center.getX() + shift, center.getY() + yoff, center.getZ() + depthOffset * edge);
                                    entranceOffset = new BlockPos(0, 1, (depth - depthOffset - 1) * edge);
                                } else {
                                    center = new BlockPos(center.getX() + depthOffset * edge, center.getY() + yoff, center.getZ() + shift);
                                    entranceOffset = new BlockPos((depth - depthOffset - 1) * edge, 1, 0);
                                }
                                break openingfound;
                            }
                        }
                    }
                }
            }
        }
        if (entranceOffset == null) {
            //System.out.println("No opening found " + center);
            return false;
        }
        Direction doorDirection = Direction.fromVector(entranceOffset.getX(), 0, entranceOffset.getZ());
        int entranceDepth = Math.abs(entranceOffset.getComponentAlongAxis(doorDirection.getAxis()));
        Vec3i doorShiftVec = doorDirection.rotateYClockwise().getVector();

        if (!checkTile(world, center)) return false;

        for (int ceiling = 0; ceiling <= 1; ceiling++) {
            int y = FLOOR_HEIGHT + -1 + ceiling * (CEILING_HEIGHT + 1);
            for (int x = -WALL_RADIUS; x <= WALL_RADIUS; x++) {
                for (int z = -WALL_RADIUS; z <= WALL_RADIUS; z++) {
                    BlockState state = world.getBlockState(center.add(x, y, z));
                    if (state.isAir() || state.isLiquid()) {
                        //System.out.println("No opening ceil " + center);
                        return false;
                    }
                }
            }
        }


        for (int depth = entranceDepth; depth >= WALL_RADIUS + 1; depth--) {
            BlockPos base = center.add(doorDirection.getVector().multiply(depth));
            for (int y = FLOOR_HEIGHT - 1; y <= FLOOR_HEIGHT + 3; y++) {
                for (int shift = -2; shift <= 2; shift++) {
                    if (shift == 0 && (y == FLOOR_HEIGHT + 1 || y == FLOOR_HEIGHT + 2)) continue;
                    BlockState state = world.getBlockState(base.add(0, y, 0).add(doorShiftVec.multiply(shift)));

                    if (state.isAir() || state.isLiquid()) {
                        //System.out.println("Air around door " + center + ", " + base.add(0, y, 0).add(doorShiftVec.multiply(shift)));
                        return false;
                    }
                }
            }
        }

        /*
        for (int x = -WALL_RADIUS; x <= WALL_RADIUS; x++) {
            for (int z = -WALL_RADIUS; z <= WALL_RADIUS; z++) {
                for (int y = FLOOR_HEIGHT; y <= CEILING_HEIGHT; y++) {
                    if (y == CEILING_HEIGHT || z == WALL_RADIUS || z == -WALL_RADIUS || x == WALL_RADIUS || x == -WALL_RADIUS) {
                        this.setRandomBlock(world, random, center.add(x, y, z).add(doorShiftVec), WALL_BLOCKS, 1);
                    } else {
                        this.setBlockStateIf(world, center.add(x, y, z), Blocks.AIR.getDefaultState(), this::canReplace);
                    }
                }
            }
        }
         */

        //System.out.println("[" + (WALL_RADIUS + 1) + ", " + entranceDepth + "]");
        for (int depth = WALL_RADIUS + 1; depth <= entranceDepth; depth++) {
            //System.out.println(depth);
            BlockPos base = center.add(doorDirection.getVector().multiply(depth));
            if (depth == WALL_RADIUS + 1) {
                //System.out.println("Placed door at " + base.add(0, FLOOR_HEIGHT + 1, 0));
                this.setBlockStateIf(world, base.add(0, FLOOR_HEIGHT + 1, 0), Blocks.SPRUCE_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, doorDirection), this::canReplace);
                this.setBlockStateIf(world, base.add(0, FLOOR_HEIGHT + 2, 0), Blocks.SPRUCE_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, doorDirection), this::canReplace);
            } else {
                this.setBlockStateIf(world, base.add(0, FLOOR_HEIGHT + 1, 0), Blocks.AIR.getDefaultState(), this::canReplace);
                this.setBlockStateIf(world, base.add(0, FLOOR_HEIGHT + 2, 0), Blocks.AIR.getDefaultState(), this::canReplace);
            }
            if (depth < entranceDepth) {
                this.setRandomBlock(world, random, base.add(0, FLOOR_HEIGHT + 3, 0), WALL_BLOCKS, 1);
                for (int y = FLOOR_HEIGHT + 1; y <= FLOOR_HEIGHT + 2; y++) {
                    this.setRandomBlock(world, random, base.add(0, y, 0).add(doorShiftVec), WALL_BLOCKS, 1);
                    this.setRandomBlock(world, random, base.add(0, y, 0).add(doorShiftVec.multiply(-1)), WALL_BLOCKS, 1);
                }
            }
        }

        HashSet<Vec3i> availableTiles = new HashSet<>();
        int roomSpacing = (WALL_RADIUS) * 2;
        for (
                int x = center.getX() + roomSpacing * (int) Math.ceil((double) (new ChunkPos(originChunk.x - 1, originChunk.z).getStartX() - center.getX() + WALL_RADIUS) / roomSpacing);
                x <= center.getX() + roomSpacing * (int) Math.floor((double) (new ChunkPos(originChunk.x + 1, originChunk.z).getEndX() - center.getX() - WALL_RADIUS) / roomSpacing);
                x += roomSpacing
        ) {
            for (
                    int z = center.getZ() + roomSpacing * (int) Math.ceil((double) (new ChunkPos(originChunk.x, originChunk.z - 1).getStartZ() - center.getZ() + WALL_RADIUS) / roomSpacing);
                    z <= center.getZ() + roomSpacing * (int) Math.floor((double) (new ChunkPos(originChunk.x, originChunk.z + 1).getEndZ() - center.getZ() - WALL_RADIUS) / roomSpacing);
                    z += roomSpacing
            ) {
                availableTiles.add(new Vec3i(x, center.getY(), z));
            }
        }

        RoomGenerator roomGenerator = new RoomGenerator(world, random, availableTiles);
        roomGenerator.roomWithDoor(center, doorDirection, new WallWithEntrance(center, doorDirection));

        for (Piece template : roomGenerator.generatedPieces) {
            template.place(world, random);
        }

        return true;
    }

    private boolean checkTile(StructureWorldAccess world, BlockPos center) {
        for (int axis = 0; axis <= 1; axis++) {
            for (int edge = -1; edge <= 1; edge += 2) {
                for (int i = -WALL_RADIUS - 1; i <= WALL_RADIUS + 1; i++) {
                    for (int y = FLOOR_HEIGHT; y <= CEILING_HEIGHT; y++) {
                        int x = axis * ((WALL_RADIUS + 1) * edge) + (1 - axis) * i;
                        int z = (1 - axis) * ((WALL_RADIUS + 1) * edge) + axis * i;
                        BlockState state = world.getBlockState(center.add(x, y, z));
                        if (state.isAir() || state.isLiquid()) {
                            //System.out.println("No wall " + center);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean canReplace(BlockState state) {
        return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    private boolean canReplaceNonSolid(BlockState state) {
        return this.canReplace(state) && (state.isAir() || state.isLiquid());
    }

    protected void setRandomBlock(StructureWorldAccess world, Random random, BlockPos pos, BlockState[] states, int fail) {
        int i = random.nextInt(states.length + fail);
        if (i < states.length) {
            this.setBlockStateIf(world, pos, states[i], this::canReplace);
        }
    }

    protected static BlockRotation rotateFromNorth(Direction to) {
        return switch (to) {
            case EAST -> BlockRotation.CLOCKWISE_90;
            case SOUTH -> BlockRotation.CLOCKWISE_180;
            case WEST -> BlockRotation.COUNTERCLOCKWISE_90;
            default -> BlockRotation.NONE;
        };
    }

    protected static void setRandomSherds(BlockEntity blockEntity, Random random) {
        final Item[] sherds = {
                Items.BRICK, Items.MINER_POTTERY_SHERD, Items.MINER_POTTERY_SHERD,
                Items.BURN_POTTERY_SHERD, Items.DANGER_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD,
                Items.BREWER_POTTERY_SHERD, Items.PRIZE_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD
        };
        Item sherd = sherds[random.nextInt(sherds.length)];
        if (blockEntity instanceof DecoratedPotBlockEntity decoratedPot) {
            decoratedPot.sherds = switch (random.nextInt(4)) {
                case 0 -> new DecoratedPotBlockEntity.Sherds(sherd, sherd, sherd, sherd);
                case 1 -> new DecoratedPotBlockEntity.Sherds(sherd, Items.BRICK, Items.BRICK, sherd);
                case 2 -> new DecoratedPotBlockEntity.Sherds(Items.BRICK, sherd, sherd, Items.BRICK);
                default -> new DecoratedPotBlockEntity.Sherds(Items.BRICK, Items.BRICK, Items.BRICK, sherd);
            };
            decoratedPot.markDirty();
        }
    }

    protected static void setRandomBooks(StructureWorldAccess world, Random random, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof ChiseledBookshelfBlockEntity bookshelf) {
            BlockState state = world.getBlockState(pos);
            List<Integer> codices = Config.INSTANCE.get().lootCodicesAdd.getOrDefault(UnruffledMod.BOOKSHELF_LOOT_TABLE, Collections.emptyList());
            for (int i = 0; i < 6; i++) {
                int pick = random.nextInt(codices.size() * 8 + 4);
                boolean set = false;
                if (pick < codices.size()) {
                    bookshelf.inventory.set(i, AncientCodexItem.setNumber(new ItemStack(CustomItems.ANCIENT_CODEX), codices.get(pick)));
                    set = true;
                } else if (pick % 5 > 1) {
                    bookshelf.inventory.set(i, new ItemStack(Items.BOOK));
                    set = true;
                }
                state = state.with(ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i), set);
            }
            bookshelf.markDirty();
            world.setBlockState(pos, state, 3);
        }
    }

    private class RoomGenerator {
        private final StructureWorldAccess world;
        private final Random random;
        private final Set<Vec3i> availableTiles;
        private final Set<Vec3i> placedWalls = new HashSet<>();
        private final WeightedRandomList<WallDecorationTemplate> wallTemplates = new WeightedRandomList<>(WALL_TEMPLATES);
        public final List<Piece> generatedPieces = new LinkedList<>();

        private RoomGenerator(StructureWorldAccess world, Random random, Set<Vec3i> availableTiles) {
            this.world = world;
            this.random = random;
            this.availableTiles = availableTiles;
        }

        private Vec3i wallCenter(BlockPos center, Direction direction) {
            return center.add(direction.getVector().multiply(WALL_RADIUS));
        }

        public void roomWithDoor(BlockPos center, Direction doorDirection, Piece doorPiece) {
            availableTiles.remove(center);
            generatedPieces.add(new Room(center));
            generatedPieces.add(doorPiece);
            placedWalls.add(wallCenter(center, doorDirection));
            if (!placedWalls.contains(wallCenter(center, Direction.NORTH))) addWallOrRoom(center, Direction.NORTH);
            else if (doorDirection != Direction.NORTH) decorateWall(center, Direction.NORTH);
            if (!placedWalls.contains(wallCenter(center, Direction.EAST))) addWallOrRoom(center, Direction.EAST);
            else if (doorDirection != Direction.EAST) decorateWall(center, Direction.EAST);
            if (!placedWalls.contains(wallCenter(center, Direction.SOUTH))) addWallOrRoom(center, Direction.SOUTH);
            else if (doorDirection != Direction.SOUTH) decorateWall(center, Direction.SOUTH);
            if (!placedWalls.contains(wallCenter(center, Direction.WEST))) addWallOrRoom(center, Direction.WEST);
            else if (doorDirection != Direction.WEST) decorateWall(center, Direction.WEST);
        }

        private void addWallOrRoom(BlockPos center, Direction direction) {
            BlockPos nextTileCenter = center.add(direction.getVector().multiply(WALL_RADIUS * 2));
            if (0.5f - 10f / (20 + availableTiles.size()) >= random.nextFloat() && availableTiles.contains(nextTileCenter)) {
                if (checkTile(world, nextTileCenter)) {
                    roomWithDoor(nextTileCenter, direction.getOpposite(), new RoomPassage(nextTileCenter, direction.getOpposite()));
                } else {
                    availableTiles.remove(nextTileCenter);
                }
            } else {
                generatedPieces.add(new Wall(center, direction));
                placedWalls.add(wallCenter(center, direction));
                decorateWall(center, direction);
            }
        }

        private void decorateWall(BlockPos center, Direction direction) {
            WallDecorationTemplate wallTemplate = wallTemplates.popSample(random);
            if (wallTemplate != null) {
                generatedPieces.add(new WallDecoration(center, direction, wallTemplate));
                wallTemplates.add(0.1, wallTemplate);
            }
        }
    }

    private static abstract class Piece {
        public abstract void place(StructureWorldAccess world, Random random);
    }

    private class Wall extends Piece {
        private final BlockPos center;
        private final Direction direction;


        private Wall(BlockPos center, Direction direction) {
            this.center = center;
            this.direction = direction;
        }

        @Override
        public void place(StructureWorldAccess world, Random random) {
            Vec3i wallDir = direction.getVector(), shiftDir = direction.rotateYClockwise().getVector();
            for (int shift = - WALL_RADIUS + 1; shift <= WALL_RADIUS - 1; shift++) {
                for (int y = FLOOR_HEIGHT + 1; y <= CEILING_HEIGHT - 1; y++) {
                    setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), WALL_BLOCKS, 1);
                }
            }
        }
    }

    private class RoomPassage extends Piece {
        private final BlockPos center;
        private final Direction direction;


        private RoomPassage(BlockPos center, Direction direction) {
            this.center = center;
            this.direction = direction;
        }

        @Override
        public void place(StructureWorldAccess world, Random random) {
            Vec3i wallDir = direction.getVector(), shiftDir = direction.rotateYClockwise().getVector();
            for (int shift = - WALL_RADIUS + 1; shift <= WALL_RADIUS - 1; shift++) {
                for (int y = FLOOR_HEIGHT + 1; y <= CEILING_HEIGHT - 1; y++) {
                    if (shift == 0) {
                        if (y == FLOOR_HEIGHT + 1 || y == FLOOR_HEIGHT + 2) {
                            setBlockStateIf(world, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), Blocks.AIR.getDefaultState(), UndergroundCabinFeature.this::canReplace);
                            continue;
                        }
                        if (y == FLOOR_HEIGHT + 3) {
                            setBlockStateIf(world, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), Blocks.SPRUCE_FENCE_GATE.getDefaultState().with(FenceGateBlock.FACING, direction), UndergroundCabinFeature.this::canReplace);
                            continue;
                        }
                    } else if (shift == -1 || shift == 1) {
                        if (y == FLOOR_HEIGHT + 1 || y == FLOOR_HEIGHT + 2) {
                            setBlockStateIf(world, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), Blocks.SPRUCE_FENCE.getDefaultState().with(shift == 1 ? FenceBlock.EAST : FenceBlock.WEST, true).rotate(rotateFromNorth(direction)), UndergroundCabinFeature.this::canReplace);
                            continue;
                        } else if (y == FLOOR_HEIGHT + 3) {
                            setBlockStateIf(world, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState().with(PillarBlock.AXIS, direction.getAxis()), UndergroundCabinFeature.this::canReplace);
                            continue;
                        }
                    }
                    setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), WALL_BLOCKS, 1);
                }
            }
            setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(0, FLOOR_HEIGHT, 0)), FLOOR_BLOCKS, 0);
            setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir).add(0, FLOOR_HEIGHT, 0)), FLOOR_BLOCKS, 1);
            setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(-1)).add(0, FLOOR_HEIGHT, 0)), FLOOR_BLOCKS, 1);

            for (int i = 0; i < 6; i++) {
                int dist = WALL_RADIUS - 1 - i / 4, shift = i % 4 + i / 4 - 2;
                BlockPos pos = center.add(wallDir.multiply(dist)).add(shiftDir.multiply(shift));
                setRandomBlock(world, random, pos.add(0, FLOOR_HEIGHT + 3, 0), CEILING_DECORATION, CEILING_DECORATION.length * 3);
                if (i == 2) continue;
                setRandomBlock(world, random, pos.add(0, FLOOR_HEIGHT + 1, 0), FLOOR_DECORATION, FLOOR_DECORATION.length);
            }
        }
    }

    private class WallWithEntrance extends Piece {
        private final BlockPos center;
        private final Direction direction;


        private WallWithEntrance(BlockPos center, Direction direction) {
            this.center = center;
            this.direction = direction;
        }

        @Override
        public void place(StructureWorldAccess world, Random random) {
            Vec3i wallDir = direction.getVector(), shiftDir = direction.rotateYClockwise().getVector();
            for (int shift = - WALL_RADIUS + 1; shift <= WALL_RADIUS - 1; shift++) {
                for (int y = FLOOR_HEIGHT + 1; y <= CEILING_HEIGHT - 1; y++) {
                    if ((y == FLOOR_HEIGHT + 1 || y == FLOOR_HEIGHT + 2) && shift == 0) {
                        setBlockStateIf(world, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), Blocks.AIR.getDefaultState(), UndergroundCabinFeature.this::canReplace);
                        continue;
                    }
                    setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(shiftDir.multiply(shift)).add(0, y, 0)), WALL_BLOCKS, 1);
                }
            }
            setRandomBlock(world, random, center.add(wallDir.multiply(WALL_RADIUS).add(0, FLOOR_HEIGHT, 0)), FLOOR_BLOCKS, 0);
            for (int i = 0; i < 6; i++) {
                int dist = WALL_RADIUS - 1 - i / 4, shift = i % 4 + i / 4 - 2;
                BlockPos pos = center.add(wallDir.multiply(dist)).add(shiftDir.multiply(shift));
                setRandomBlock(world, random, pos.add(0, FLOOR_HEIGHT + 3, 0), CEILING_DECORATION, CEILING_DECORATION.length * 3);
                if (i == 2) continue;
                setRandomBlock(world, random, pos.add(0, FLOOR_HEIGHT + 1, 0), FLOOR_DECORATION, FLOOR_DECORATION.length);
            }
        }
    }

    private class Room extends Piece {
        private final BlockPos center;

        private Room(BlockPos center) {
            this.center = center;
        }

        @Override
        public void place(StructureWorldAccess world, Random random) {
            for (int x = -WALL_RADIUS + 1; x <= WALL_RADIUS - 1; x++) {
                for (int z = -WALL_RADIUS + 1; z <= WALL_RADIUS - 1; z++) {
                    setRandomBlock(world, random, center.add(x, FLOOR_HEIGHT, z), FLOOR_BLOCKS, 2);
                    setBlockStateIf(world, center.add(x, CEILING_HEIGHT, z), Blocks.DEEPSLATE_BRICKS.getDefaultState(), UndergroundCabinFeature.this::canReplaceNonSolid);
                    for (int y = FLOOR_HEIGHT + 1; y <= CEILING_HEIGHT - 1; y++) {
                        setBlockStateIf(world, center.add(x, y, z), Blocks.AIR.getDefaultState(), UndergroundCabinFeature.this::canReplace);
                    }
                }
            }
        }
    }

    private class WallDecoration extends Piece {
        private final BlockPos center;
        private final Direction direction;
        private final WallDecorationTemplate template;

        private WallDecoration(BlockPos center, Direction direction, WallDecorationTemplate template) {
            this.center = center;
            this.direction = direction;
            this.template = template;
        }

        @Override
        public void place(StructureWorldAccess world, Random random) {
            BlockTemplate[][] columns = this.template.getColumns(random);
            Vec3i dirVec = direction.getVector(), shiftVec = direction.rotateYClockwise().getVector();

            for (int i = 0; i < 6; i++) {
                int dist = WALL_RADIUS - 1 - i / 4, shift = i % 4 + i / 4 - 2;
                BlockPos bottom = center.add(dirVec.multiply(dist)).add(shiftVec.multiply(shift));
                if (i >= columns.length) break;
                BlockTemplate[] column = columns[i];
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = bottom.add(0, y + FLOOR_HEIGHT + 1, 0);
                    if (y >= column.length) {
                        if (y == 0) {
                            setRandomBlock(world, random, pos, FLOOR_DECORATION, FLOOR_DECORATION.length);
                        } else if (y == 2) {
                            setRandomBlock(world, random, pos, CEILING_DECORATION, CEILING_DECORATION.length * 2);
                        }
                        continue;
                    }
                    BlockTemplate template = column[y];
                    if (template == null) continue;
                    BlockState state = template.getBlockState(random);
                    if (state != null) setBlockStateIf(world, pos, state.rotate(rotateFromNorth(direction)), UndergroundCabinFeature.this::canReplace);
                    template.process(world, random, pos, direction);
                }
            }
        }
    }

    private static abstract class WallDecorationTemplate {
        public abstract BlockTemplate[][] getColumns(Random random);
    }

    private static class FixedWallTemplate extends WallDecorationTemplate {
        public final BlockTemplate[][] columns;

        public FixedWallTemplate(BlockTemplate[][] columns) {
            this.columns = columns;
        }

        @Override
        public BlockTemplate[][] getColumns(Random random) {
            return columns;
        }
    }

    private static class RandomPile extends WallDecorationTemplate {
        public final BlockTemplate pileBlock;
        public final BlockTemplate topBlock;
        public final int maxPileHeight;

        private RandomPile(BlockTemplate pileBlock, BlockTemplate topBlock, int maxPileHeight) {
            this.pileBlock = pileBlock;
            this.topBlock = topBlock;
            this.maxPileHeight = maxPileHeight;
        }

        private RandomPile(BlockTemplate pileBlock) {
            this.pileBlock = pileBlock;
            this.topBlock = pileBlock;
            this.maxPileHeight = 3;
        }

        @Override
        public BlockTemplate[][] getColumns(Random random) {
            BlockTemplate[][] columns = new BlockTemplate[6][3];
            for (int i = 0; i < 6; i++) {
                int y;
                for (y = 0; y < maxPileHeight; y++) {
                    if ((float) (10 - Math.abs(i - 2) - (11 - maxPileHeight * 3) * y) / 11f < random.nextFloat()) {
                        break;
                    }
                    columns[i][y] = pileBlock;
                }
                if (y < 3) {
                    columns[i][y] = topBlock;
                }
            }
            return columns;
        }
    }
}
