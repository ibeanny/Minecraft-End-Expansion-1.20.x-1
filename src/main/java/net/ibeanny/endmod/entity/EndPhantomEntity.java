package net.ibeanny.endmod.entity;

import net.ibeanny.endmod.phantom.EndPhantomData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class EndPhantomEntity extends Phantom {

    private static final EntityDataAccessor<Integer> DATA_COLOR_ID =
            SynchedEntityData.defineId(EndPhantomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HARNESS_ID =
            SynchedEntityData.defineId(EndPhantomEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean needsSpawnValidation;

    public EndPhantomEntity(EntityType<? extends Phantom> entityType, Level level) {
        super(entityType, level);
        EndPhantomData.markEndPhantom(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLOR_ID, -1);
        this.entityData.define(DATA_HARNESS_ID, false);
    }

    @Override
    public void tick() {
        if (EndPhantomData.data(this).getBoolean(EndPhantomData.TAMED_TAG)) {
            this.setPersistenceRequired();
        }
        super.tick();
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.35D;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (EndPhantomData.data(this).getBoolean(EndPhantomData.TAMED_TAG)) {
            return false;
        }
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        if (EndPhantomData.data(this).getBoolean(EndPhantomData.TAMED_TAG)) {
            return false;
        }
        return super.shouldDespawnInPeaceful();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag data = EndPhantomData.data(this);
        tag.putBoolean(EndPhantomData.END_PHANTOM_TAG, data.getBoolean(EndPhantomData.END_PHANTOM_TAG));
        tag.putBoolean(EndPhantomData.ENHANCED_END_PHANTOM_TAG, data.getBoolean(EndPhantomData.ENHANCED_END_PHANTOM_TAG));
        tag.putBoolean(EndPhantomData.TAMED_TAG, data.getBoolean(EndPhantomData.TAMED_TAG));
        tag.putBoolean(EndPhantomData.HARNESS_TAG, isHarnessed());
        if (data.hasUUID(EndPhantomData.OWNER_TAG)) {
            tag.putUUID(EndPhantomData.OWNER_TAG, data.getUUID(EndPhantomData.OWNER_TAG));
        }
        if (data.contains(EndPhantomData.OWNER_ANCHOR_X_TAG)) {
            tag.putDouble(EndPhantomData.OWNER_ANCHOR_X_TAG, data.getDouble(EndPhantomData.OWNER_ANCHOR_X_TAG));
            tag.putDouble(EndPhantomData.OWNER_ANCHOR_Y_TAG, data.getDouble(EndPhantomData.OWNER_ANCHOR_Y_TAG));
            tag.putDouble(EndPhantomData.OWNER_ANCHOR_Z_TAG, data.getDouble(EndPhantomData.OWNER_ANCHOR_Z_TAG));
        }
        getColor().ifPresent(color -> tag.putString(EndPhantomData.COLOR_TAG, color.getName()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag data = EndPhantomData.data(this);
        if (tag.contains(EndPhantomData.END_PHANTOM_TAG)) {
            data.putBoolean(EndPhantomData.END_PHANTOM_TAG, tag.getBoolean(EndPhantomData.END_PHANTOM_TAG));
        }
        if (tag.contains(EndPhantomData.ENHANCED_END_PHANTOM_TAG)) {
            data.putBoolean(EndPhantomData.ENHANCED_END_PHANTOM_TAG, tag.getBoolean(EndPhantomData.ENHANCED_END_PHANTOM_TAG));
        }
        if (tag.contains(EndPhantomData.TAMED_TAG)) {
            data.putBoolean(EndPhantomData.TAMED_TAG, tag.getBoolean(EndPhantomData.TAMED_TAG));
        }
        if (tag.contains(EndPhantomData.HARNESS_TAG)) {
            setHarnessed(tag.getBoolean(EndPhantomData.HARNESS_TAG));
        }
        if (tag.hasUUID(EndPhantomData.OWNER_TAG)) {
            data.putUUID(EndPhantomData.OWNER_TAG, tag.getUUID(EndPhantomData.OWNER_TAG));
        }
        if (tag.contains(EndPhantomData.OWNER_ANCHOR_X_TAG)) {
            data.putDouble(EndPhantomData.OWNER_ANCHOR_X_TAG, tag.getDouble(EndPhantomData.OWNER_ANCHOR_X_TAG));
            data.putDouble(EndPhantomData.OWNER_ANCHOR_Y_TAG, tag.getDouble(EndPhantomData.OWNER_ANCHOR_Y_TAG));
            data.putDouble(EndPhantomData.OWNER_ANCHOR_Z_TAG, tag.getDouble(EndPhantomData.OWNER_ANCHOR_Z_TAG));
        }
        DyeColor color = DyeColor.byName(tag.getString(EndPhantomData.COLOR_TAG), null);
        if (color != null) {
            setColor(color);
        }
        if (data.getBoolean(EndPhantomData.TAMED_TAG)) {
            this.setPersistenceRequired();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            spawnTameParticles(true);
            return;
        }
        if (id == 6) {
            spawnTameParticles(false);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        if (!EndPhantomData.data(this).getBoolean(EndPhantomData.TAMED_TAG)) {
            return false;
        }
        if (!EndPhantomData.data(this).hasUUID(EndPhantomData.OWNER_TAG)) {
            return false;
        }
        return EndPhantomData.data(this).getUUID(EndPhantomData.OWNER_TAG).equals(player.getUUID())
                && this.getFirstPassenger() == null;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return EndPhantomData.data(this).getBoolean(EndPhantomData.TAMED_TAG) || super.requiresCustomPersistence();
    }

    public Optional<DyeColor> getColor() {
        int colorId = this.entityData.get(DATA_COLOR_ID);
        if (colorId < 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(DyeColor.byId(colorId));
    }

    public void setColor(DyeColor color) {
        this.entityData.set(DATA_COLOR_ID, color.getId());
        EndPhantomData.setColor(this, color);
    }

    public boolean isHarnessed() {
        return this.entityData.get(DATA_HARNESS_ID);
    }

    public void setHarnessed(boolean harnessed) {
        this.entityData.set(DATA_HARNESS_ID, harnessed);
        EndPhantomData.data(this).putBoolean(EndPhantomData.HARNESS_TAG, harnessed);
    }

    public void markForSpawnValidation() {
        this.needsSpawnValidation = true;
    }

    public boolean consumeSpawnValidationFlag() {
        boolean flag = this.needsSpawnValidation;
        this.needsSpawnValidation = false;
        return flag;
    }

    private void spawnTameParticles(boolean success) {
        for (int i = 0; i < 7; i++) {
            double x = this.getRandomX(1.0D);
            double y = this.getRandomY() + 0.5D;
            double z = this.getRandomZ(1.0D);
            double vx = this.random.nextGaussian() * 0.02D;
            double vy = this.random.nextGaussian() * 0.02D;
            double vz = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(success ? ParticleTypes.HEART : ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
        }
    }
}
