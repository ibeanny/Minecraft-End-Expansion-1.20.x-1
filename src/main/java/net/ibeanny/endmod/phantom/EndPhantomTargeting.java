package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.ibeanny.endmod.item.EndExpansionItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndPhantomTargeting {

    private static final double AGGRO_RANGE = 22.0;
    private static final double LEASH_RANGE = 28.0;
    private static final double LURE_RANGE = 26.0;

    @SubscribeEvent
    public static void onPhantomChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        Level level = phantom.level();
        if (level.isClientSide() || !level.dimension().equals(Level.END)) return;
        if (!EndPhantomData.isEnhancedEndPhantom(phantom)) return;
        if (EndPhantomData.data(phantom).getBoolean(EndPhantomData.TAMED_TAG)) return;

        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof Phantom) {
            event.setCanceled(true);
            phantom.setTarget(null);
            return;
        }
        if (newTarget instanceof Player player) {
            if (isHoldingVoidMorsel(player) || !isEligibleTarget(player) || phantom.distanceTo(player) > AGGRO_RANGE) {
                event.setCanceled(true);
                phantom.setTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onPhantomTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        Level level = phantom.level();
        if (level.isClientSide() || !level.dimension().equals(Level.END)) return;
        if (!EndPhantomData.isEnhancedEndPhantom(phantom)) return;
        if (EndPhantomData.data(phantom).getBoolean(EndPhantomData.TAMED_TAG)) return;

        LivingEntity currentTarget = phantom.getTarget();
        if (currentTarget instanceof Phantom) {
            phantom.setTarget(null);
            return;
        }
        if (currentTarget instanceof Player currentPlayer) {
            if (isHoldingVoidMorsel(currentPlayer) || !isEligibleTarget(currentPlayer) || phantom.distanceTo(currentPlayer) > LEASH_RANGE) {
                phantom.setTarget(null);
            }
            return;
        }

        if (!(level instanceof ServerLevel server)) return;

        Player luredPlayer = findLuredPlayer(server, phantom);
        if (luredPlayer != null) {
            moveTowardLuredPlayer(phantom, luredPlayer);
            return;
        }

        List<Player> players = server.getEntitiesOfClass(
                Player.class,
                phantom.getBoundingBox().inflate(AGGRO_RANGE, AGGRO_RANGE, AGGRO_RANGE),
                player -> isEligibleTarget(player) && !isHoldingVoidMorsel(player)
        );

        Player nearest = null;
        double bestScore = Double.MAX_VALUE;
        for (Player player : players) {
            double score = phantom.distanceToSqr(player);
            if (isPhantomBait(player)) {
                score *= 0.65D;
            }
            if (score < bestScore) {
                bestScore = score;
                nearest = player;
            }
        }

        if (nearest != null) {
            phantom.setTarget(nearest);
        }
    }

    private static Player findLuredPlayer(ServerLevel level, Phantom phantom) {
        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                phantom.getBoundingBox().inflate(LURE_RANGE, LURE_RANGE, LURE_RANGE),
                player -> player.isAlive() && !player.isSpectator() && isHoldingVoidMorsel(player)
        );

        Player nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Player player : players) {
            double distance = phantom.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private static void moveTowardLuredPlayer(Phantom phantom, Player player) {
        Vec3 goal = player.position().add(0.0D, 2.2D, 0.0D);
        Vec3 toGoal = goal.subtract(phantom.position());
        if (toGoal.lengthSqr() < 4.0D) {
            phantom.setDeltaMovement(phantom.getDeltaMovement().scale(0.85D));
            return;
        }

        Vec3 motion = phantom.getDeltaMovement().scale(0.82D).add(toGoal.normalize().scale(0.09D));
        phantom.setDeltaMovement(motion);
        float yaw = (float) (Math.atan2(motion.z, motion.x) * (180F / Math.PI)) - 90.0F;
        phantom.setYRot(yaw);
        phantom.yBodyRot = yaw;
    }

    public static boolean isHoldingVoidMorsel(Player player) {
        return player.getMainHandItem().is(EndExpansionItems.VOID_MORSEL.get())
                || player.getOffhandItem().is(EndExpansionItems.VOID_MORSEL.get());
    }

    public static boolean isEligibleTarget(Player player) {
        return player.isAlive()
                && !player.isCreative()
                && !player.isSpectator()
                && !isMountedOnPhantom(player)
                && isPhantomBait(player);
    }

    public static boolean isPhantomBait(Player player) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof ArmorItem armor
                    && (armor.getMaterial() == ArmorMaterials.DIAMOND || armor.getMaterial() == ArmorMaterials.NETHERITE)) {
                return true;
            }
        }

        return player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
    }

    private static boolean isMountedOnPhantom(Player player) {
        return player.isPassenger() && player.getVehicle() instanceof Phantom;
    }
}
