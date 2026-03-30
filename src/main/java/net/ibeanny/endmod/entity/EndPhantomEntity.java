package net.ibeanny.endmod.entity;

import net.ibeanny.endmod.phantom.EndPhantomData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EndPhantomEntity extends Phantom {

    public EndPhantomEntity(EntityType<? extends Phantom> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.35D;
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
}
