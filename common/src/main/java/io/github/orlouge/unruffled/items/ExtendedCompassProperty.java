package io.github.orlouge.unruffled.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.orlouge.unruffled.UnruffledMod;
import io.github.orlouge.unruffled.UnruffledModClient;
import io.github.orlouge.unruffled.config.Config;
import net.minecraft.client.render.item.property.numeric.NeedleAngleState;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class ExtendedCompassProperty extends NeedleAngleState implements NumericProperty {
    public static final MapCodec<ExtendedCompassProperty> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        Codec.BOOL.optionalFieldOf("wobble", true).forGetter(ExtendedCompassProperty::hasWobble),
        Target.CODEC.fieldOf("target").forGetter(state -> state.target)
    ).apply(instance, ExtendedCompassProperty::new));
    private final Target target;
    private final NeedleAngleState.Angler aimedAngler = this.createAngler(0.8F);
    private final NeedleAngleState.Angler aimlessAngler = this.createAngler(0.8F);
    private final Random random = Random.create();

    protected ExtendedCompassProperty(boolean wobble, Target target) {
        super(wobble);
        this.target = target;
    }

    @Override
    protected float getAngle(ItemStack stack, ClientWorld world, int seed, Entity user) {
        GlobalPos globalPos = this.target.getPosition(world, stack, user);
        long l = world.getTime();
        return !canPointTo(user, globalPos) ? this.getAimlessAngle(seed, l) : this.getAngleTo(user, l, globalPos.pos());
    }

    private float getAimlessAngle(int seed, long time) {
        if (this.aimlessAngler.shouldUpdate(time)) {
            this.aimlessAngler.update(time, this.random.nextFloat());
        }

        float f = this.aimlessAngler.getAngle() + (float)scatter(seed) / (float)Integer.MAX_VALUE;
        return MathHelper.floorMod(f, 1.0F);
    }

    private float getAngleTo(Entity entity, long time, BlockPos pos) {
        float f = (float)getAngleTo(entity, pos);
        float g = getBodyYaw(entity);
        float h;
        if (entity instanceof PlayerEntity playerEntity) {
            if (playerEntity.isMainPlayer() && playerEntity.getWorld().getTickManager().shouldTick()) {
                if (this.aimedAngler.shouldUpdate(time)) {
                    this.aimedAngler.update(time, 0.5F - (g - 0.25F));
                }

                h = f + this.aimedAngler.getAngle();
                return MathHelper.floorMod(h, 1.0F);
            }
        }

        h = 0.5F - (g - 0.25F - f);
        return MathHelper.floorMod(h, 1.0F);
    }

    private static boolean canPointTo(Entity entity, GlobalPos pos) {
        return pos != null && pos.dimension() == entity.getWorld().getRegistryKey() && !(pos.pos().getSquaredDistance(entity.getPos()) < (double)1.0E-5F);
    }

    private static double getAngleTo(Entity entity, BlockPos pos) {
        Vec3d vec3d = Vec3d.ofCenter(pos);
        return Math.atan2(vec3d.getZ() - entity.getZ(), vec3d.getX() - entity.getX()) / (double)((float)Math.PI * 2F);
    }

    private static float getBodyYaw(Entity entity) {
        return MathHelper.floorMod(entity.getBodyYaw() / 360.0F, 1.0F);
    }

    private static int scatter(int seed) {
        return seed * 1327217883;
    }

    @Override
    public MapCodec<? extends NumericProperty> getCodec() {
        return CODEC;
    }

    public enum Target implements StringIdentifiable {
        NONE("none") {
            public GlobalPos getPosition(ClientWorld world, ItemStack stack, Entity holder) {
                return null;
            }
        },
        NORTH_OR_SPAWN("north_or_spawn") {
            public GlobalPos getPosition(ClientWorld world, ItemStack stack, Entity holder) {
                return Config.INSTANCE.get().navigationConfig.compassPointsNorth() ? new GlobalPos(holder.getWorld().getRegistryKey(), holder.getBlockPos().north(10000)) : GlobalPos.create(world.getRegistryKey(), world.getSpawnPos());
            }
        },
        RECOVERY_LOCKED("recovery_locked") {
            public GlobalPos getPosition(ClientWorld world, ItemStack stack, Entity holder) {
                return UnruffledModClient.lockedDeathPosition.filter((pos) ->
                    stack.contains(UnruffledMod.LOCKED_COMPASS_COMPONENT)).orElse(holder instanceof PlayerEntity playerEntity ? playerEntity.getLastDeathPos().orElse(null) : null);
            }
        };

        public static final Codec<Target> CODEC = StringIdentifiable.createCodec(Target::values);
        private final String name;

        Target(final String name) {
            this.name = name;
        }

        public String asString() {
            return this.name;
        }

        abstract GlobalPos getPosition(ClientWorld world, ItemStack stack, Entity holder);
    }
}
