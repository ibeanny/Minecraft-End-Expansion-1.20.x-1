package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndPhantomTargeting {

    // Distance gates to prevent phantoms attacking from miles away
    private static final double AGGRO_RANGE = 22.0;
    private static final double LEASH_RANGE = 28.0;

    // Block vanilla
    @SubscribeEvent
    public static void onPhantomChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        Level level = phantom.level();
        if (level.isClientSide() || !level.dimension().equals(Level.END)) return;

        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof Player player) {
            // If player lacks diamond armor it prevents targeting
            if (!hasAnyDiamondArmor(player) || phantom.distanceTo(player) > AGGRO_RANGE) {
                event.setCanceled(true);
                phantom.setTarget(null);
            }
        }
    }

    //  only pick a target if the player has any diamond armor.
    @SubscribeEvent
    public static void onPhantomTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        Level level = phantom.level();
        if (level.isClientSide() || !level.dimension().equals(Level.END)) return;

        // if a player removed diamond or moved too far, drop target
        LivingEntity cur = phantom.getTarget();
        if (cur instanceof Player curPlayer) {
            if (!curPlayer.isAlive()
                    || !hasAnyDiamondArmor(curPlayer)
                    || phantom.distanceTo(curPlayer) > LEASH_RANGE) {
                phantom.setTarget(null);
            }
            return; // keep existing (valid) target
        }

        // look for nearest player in range with any diamond armor
        if (level instanceof ServerLevel server) {
            List<Player> players = server.getEntitiesOfClass(
                    Player.class,
                    phantom.getBoundingBox().inflate(AGGRO_RANGE, AGGRO_RANGE, AGGRO_RANGE),
                    p -> p.isAlive() && !p.isCreative() && hasAnyDiamondArmor(p)
            );
            Player nearest = null;
            double best = Double.MAX_VALUE;
            for (Player p : players) {
                double d = phantom.distanceTo(p);
                if (d < best) { best = d; nearest = p; }
            }
            if (nearest != null) {
                phantom.setTarget(nearest);
            }
        }
    }

    // True if the player is wearing at least one piece of diamond armor
    private static boolean hasAnyDiamondArmor(Player player) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof ArmorItem armor
                    && armor.getMaterial() == ArmorMaterials.DIAMOND) {
                return true;
            }
        }
        return false;
    }
}
