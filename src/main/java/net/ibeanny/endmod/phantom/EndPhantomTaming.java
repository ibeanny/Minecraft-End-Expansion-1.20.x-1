package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.ibeanny.endmod.item.EndExpansionItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndPhantomTaming {

    private static final double FOLLOW_START_DISTANCE = 8.0D;
    private static final double TELEPORT_DISTANCE = 32.0D;
    private static final double IDLE_ORBIT_RADIUS = 3.5D;
    private static final double IDLE_ORBIT_HEIGHT = 2.5D;
    private static final double RIDE_SPEED = 0.40D;
    private static final double RIDE_ASCENT_SPEED = 0.28D;
    private static final double RIDE_DAMPING = 0.82D;
    private static final int AMBIENT_SOUND_MIN_DELAY = 180;
    private static final int AMBIENT_SOUND_MAX_DELAY = 360;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Phantom phantom)) return;
        if (!EndPhantomEnhancements.isEnhancedEndPhantom(phantom)) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.is(Items.NAME_TAG)) {
            return;
        }

        if (player.level().isClientSide()) {
            if (stack.is(EndExpansionItems.VOID_MORSEL.get())
                    || stack.is(EndExpansionItems.PHANTOM_HARNESS.get())
                    || stack.getItem() instanceof DyeItem
                    || (stack.isEmpty() && canRide(phantom, player))) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        CompoundTag data = EndPhantomData.data(phantom);

        if (stack.getItem() instanceof DyeItem dyeItem) {
            if (isOwnedBy(phantom, player)) {
                EndPhantomData.setColor(phantom, dyeItem.getDyeColor());
                phantom.setPersistenceRequired();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        if (stack.is(EndExpansionItems.PHANTOM_HARNESS.get())) {
            if (isOwnedBy(phantom, player) && !data.getBoolean(EndPhantomData.HARNESS_TAG)) {
                data.putBoolean(EndPhantomData.HARNESS_TAG, true);
                phantom.setPersistenceRequired();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("Your End phantom is now fitted with a harness."), true);
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        if (stack.is(EndExpansionItems.VOID_MORSEL.get())) {
            if (isOwnedBy(phantom, player)) {
                if (phantom.getHealth() >= phantom.getMaxHealth()) {
                    event.setCancellationResult(InteractionResult.PASS);
                    return;
                }
                phantom.heal(6.0F);
            } else if (!data.getBoolean(EndPhantomData.TAMED_TAG)) {
                data.putBoolean(EndPhantomData.TAMED_TAG, true);
                data.putUUID(EndPhantomData.OWNER_TAG, player.getUUID());
                phantom.setTarget(null);
                phantom.setPersistenceRequired();
                phantom.heal(phantom.getMaxHealth());
                scheduleNextAmbientSound(phantom);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("The End phantom recognizes you as its master."), true);
                }
            } else {
                event.setCancellationResult(InteractionResult.PASS);
                return;
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (stack.isEmpty() && canRide(phantom, player)) {
            phantom.setTarget(null);
            player.startRiding(phantom, true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof Player player)) return;
        if (!(event.getEntityBeingMounted() instanceof Phantom phantom)) return;
        if (!EndPhantomEnhancements.isEnhancedEndPhantom(phantom)) return;

        if (event.isMounting()) {
            if (!canRide(phantom, player)) {
                event.setCanceled(true);
            } else {
                phantom.setPersistenceRequired();
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        if (!isTamed(phantom)) return;

        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof Player player && isOwnedBy(phantom, player)) {
            event.setCanceled(true);
            phantom.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onPhantomTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        if (phantom.level().isClientSide() || !isTamed(phantom)) return;

        ServerLevel level = (ServerLevel) phantom.level();
        Player owner = getOwnerPlayer(phantom);
        if (owner == null || !owner.isAlive()) {
            phantom.setTarget(null);
            phantom.getPersistentData().remove(EndPhantomData.HARNESS_TAG);
            if (!phantom.getPassengers().isEmpty()) {
                phantom.ejectPassengers();
            }
            return;
        }

        playIdleAmbientIfReady(level, phantom);

        if (phantom.getTarget() instanceof Player targetPlayer && targetPlayer.getUUID().equals(owner.getUUID())) {
            phantom.setTarget(null);
        }

        Player rider = getRider(phantom);
        if (rider != null) {
            phantom.setTarget(null);
            steerMountedPhantom(phantom, rider);
            positionRider(phantom, rider);
            return;
        }

        LivingEntity target = selectPetTarget(phantom, owner);
        if (target != null) {
            phantom.setTarget(target);
        }

        if (shouldIdleOrbit(owner) && phantom.getTarget() == null) {
            orbitOwner(phantom, owner);
            return;
        }

        double distance = phantom.distanceTo(owner);
        if (distance > TELEPORT_DISTANCE) {
            teleportNearOwner(phantom, owner);
            return;
        }

        if (distance > FOLLOW_START_DISTANCE && phantom.getTarget() == null) {
            Vec3 toOwner = owner.position().add(0.0D, 2.5D, 0.0D).subtract(phantom.position());
            Vec3 motion = phantom.getDeltaMovement().scale(0.85D).add(toOwner.normalize().scale(0.08D));
            phantom.setDeltaMovement(motion);
            faceMotion(phantom, motion);
        }
    }

    @SubscribeEvent
    public static void onPetHurtsTarget(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Phantom phantom)) return;
        if (!isTamed(phantom)) return;

        LivingEntity victim = event.getEntity();
        Player owner = getOwnerPlayer(phantom);
        if (!isValidPetTarget(phantom, owner, victim)) return;

        if (victim instanceof Mob mob) {
            mob.setLastHurtByMob(phantom);
            mob.setTarget(phantom);
        }
    }

    private static LivingEntity selectPetTarget(Phantom phantom, Player owner) {
        LivingEntity ownerThreat = owner.getLastHurtByMob();
        if (isValidPetTarget(phantom, owner, ownerThreat) && phantom.distanceTo(ownerThreat) < 32.0D) {
            return ownerThreat;
        }

        LivingEntity ownerVictim = owner.getLastHurtMob();
        if (isValidPetTarget(phantom, owner, ownerVictim) && phantom.distanceTo(ownerVictim) < 32.0D) {
            return ownerVictim;
        }

        LivingEntity currentTarget = phantom.getTarget();
        if (isValidPetTarget(phantom, owner, currentTarget)) {
            return currentTarget;
        }

        return null;
    }

    private static void teleportNearOwner(Phantom phantom, Player owner) {
        for (int i = 0; i < 8; i++) {
            BlockPos pos = owner.blockPosition().offset(
                    owner.level().random.nextInt(7) - 3,
                    6 + owner.level().random.nextInt(4),
                    owner.level().random.nextInt(7) - 3
            );
            if (canOccupyAir(owner.level(), pos)) {
                phantom.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, phantom.getYRot(), phantom.getXRot());
                phantom.setDeltaMovement(Vec3.ZERO);
                return;
            }
        }
    }

    private static boolean canOccupyAir(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        return state.isAir() && above.isAir();
    }

    private static void steerMountedPhantom(Phantom phantom, Player rider) {
        Vec3 look = rider.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            horizontalLook = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3 right = new Vec3(-horizontalLook.z, 0.0D, horizontalLook.x);
        double forward = rider.zza;
        double strafe = rider.xxa * 0.65D;
        double vertical = Mth.clamp(-look.y * Math.max(0.2D, Math.abs(forward)), -1.0D, 1.0D);
        if (rider.isCrouching()) {
            vertical -= 0.85D;
        }

        Vec3 desired = horizontalLook.scale(forward).add(right.scale(strafe)).add(0.0D, vertical, 0.0D);
        if (desired.lengthSqr() > 1.0D) {
            desired = desired.normalize();
        }

        Vec3 motion = phantom.getDeltaMovement().scale(RIDE_DAMPING)
                .add(desired.x * RIDE_SPEED, desired.y * RIDE_ASCENT_SPEED, desired.z * RIDE_SPEED);
        phantom.setDeltaMovement(motion);
        phantom.fallDistance = 0.0F;
        phantom.setTarget(null);
        phantom.setYRot(rider.getYRot());
        phantom.setXRot(rider.getXRot() * 0.5F);
        phantom.yBodyRot = phantom.getYRot();
    }

    private static void positionRider(Phantom phantom, Player rider) {
        double yawRad = Math.toRadians(phantom.getYRot());
        double x = phantom.getX() - Math.sin(yawRad) * 0.2D;
        double y = phantom.getY() + 0.35D;
        double z = phantom.getZ() + Math.cos(yawRad) * 0.2D;
        rider.setPos(x, y, z);
    }

    private static boolean isValidPetTarget(Phantom phantom, Player owner, LivingEntity target) {
        if (target == null || !target.isAlive() || target == phantom) {
            return false;
        }
        if (owner != null && target.getUUID().equals(owner.getUUID())) {
            return false;
        }
        if (target instanceof Phantom otherPhantom && isTamed(otherPhantom)) {
            if (owner != null && isOwnedBy(otherPhantom, owner)) {
                return false;
            }
        }
        return true;
    }

    private static boolean shouldIdleOrbit(Player owner) {
        Vec3 movement = owner.getDeltaMovement();
        return owner.onGround()
                && movement.horizontalDistanceSqr() < 0.0025D
                && Math.abs(movement.y) < 0.02D
                && !owner.swinging;
    }

    private static void orbitOwner(Phantom phantom, Player owner) {
        double angle = (phantom.level().getGameTime() % 80L) / 80.0D * (Math.PI * 2.0D);
        double x = owner.getX() + Math.cos(angle) * IDLE_ORBIT_RADIUS;
        double z = owner.getZ() + Math.sin(angle) * IDLE_ORBIT_RADIUS;
        double y = owner.getY() + IDLE_ORBIT_HEIGHT + 0.35D * Math.sin(angle * 2.0D);

        Vec3 target = new Vec3(x, y, z);
        Vec3 toTarget = target.subtract(phantom.position());
        if (toTarget.lengthSqr() < 0.01D) {
            phantom.setDeltaMovement(phantom.getDeltaMovement().scale(0.8D));
            return;
        }

        Vec3 motion = phantom.getDeltaMovement().scale(0.78D).add(toTarget.normalize().scale(0.10D));
        phantom.setDeltaMovement(motion);
        faceMotion(phantom, motion);
    }

    private static void faceMotion(Phantom phantom, Vec3 motion) {
        if (motion.lengthSqr() < 1.0E-5D) {
            return;
        }
        float yaw = (float) (Mth.atan2(motion.z, motion.x) * (180F / Math.PI)) - 90.0F;
        phantom.setYRot(yaw);
        phantom.yBodyRot = yaw;
    }

    private static void playIdleAmbientIfReady(ServerLevel level, Phantom phantom) {
        if (phantom.getTarget() != null || getRider(phantom) != null) {
            return;
        }

        CompoundTag data = EndPhantomData.data(phantom);
        long now = level.getGameTime();
        long nextSound = data.getLong(EndPhantomData.AMBIENT_SOUND_COOLDOWN_TAG);
        if (nextSound == 0L) {
            scheduleNextAmbientSound(phantom);
            return;
        }
        if (now < nextSound) {
            return;
        }

        level.playSound(
                null,
                phantom.getX(),
                phantom.getY(),
                phantom.getZ(),
                SoundEvents.PHANTOM_AMBIENT,
                SoundSource.NEUTRAL,
                0.4F,
                0.72F + level.random.nextFloat() * 0.12F
        );
        scheduleNextAmbientSound(phantom);
    }

    private static void scheduleNextAmbientSound(Phantom phantom) {
        if (!(phantom.level() instanceof ServerLevel level)) {
            return;
        }
        int delay = AMBIENT_SOUND_MIN_DELAY + level.random.nextInt(AMBIENT_SOUND_MAX_DELAY - AMBIENT_SOUND_MIN_DELAY + 1);
        EndPhantomData.data(phantom).putLong(EndPhantomData.AMBIENT_SOUND_COOLDOWN_TAG, level.getGameTime() + delay);
    }

    private static boolean isTamed(Phantom phantom) {
        CompoundTag data = EndPhantomData.data(phantom);
        return data.getBoolean(EndPhantomData.TAMED_TAG) && getOwnerUuid(phantom).isPresent();
    }

    private static boolean isOwnedBy(Phantom phantom, Player player) {
        return getOwnerUuid(phantom).map(uuid -> uuid.equals(player.getUUID())).orElse(false);
    }

    private static boolean canRide(Phantom phantom, Player player) {
        return isOwnedBy(phantom, player)
                && EndPhantomData.data(phantom).getBoolean(EndPhantomData.HARNESS_TAG)
                && (phantom.getPassengers().isEmpty() || phantom.getFirstPassenger() == player);
    }

    private static Optional<UUID> getOwnerUuid(Phantom phantom) {
        CompoundTag data = EndPhantomData.data(phantom);
        if (!data.hasUUID(EndPhantomData.OWNER_TAG)) {
            return Optional.empty();
        }
        return Optional.of(data.getUUID(EndPhantomData.OWNER_TAG));
    }

    private static Player getOwnerPlayer(Phantom phantom) {
        if (!(phantom.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        UUID ownerId = getOwnerUuid(phantom).orElse(null);
        return ownerId == null ? null : serverLevel.getPlayerByUUID(ownerId);
    }

    private static Player getRider(Phantom phantom) {
        if (phantom.getFirstPassenger() instanceof Player player && isOwnedBy(phantom, player)) {
            return player;
        }
        return null;
    }
}
