package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndVoidPhantomSpawns {
    private static final int CHECK_INTERVAL_TICKS = 40; // every 2 seconds
    private static final int NEARBY_RADIUS = 72; // scan radius for existing phantoms
    private static final int HORIZ_SAMPLE = 12; // horizontal radius for void check
    private static final int DOWN_SCAN = 48; // vertical scan depth

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        ServerLevel level = (ServerLevel) event.player.level();
        if (!level.dimension().equals(Level.END))
            return;
        if (event.player.tickCount % CHECK_INTERVAL_TICKS != 0)
            return;

        BlockPos pos = event.player.blockPosition();

        // only in dead zones
        if (!isOverVoid(level, pos, HORIZ_SAMPLE, DOWN_SCAN))
            return;

        // Count existing phantoms near the player
        List<Phantom> nearby = level.getEntitiesOfClass(
                Phantom.class,
                new AABB(
                        event.player.getX() - NEARBY_RADIUS, event.player.getY() - NEARBY_RADIUS, event.player.getZ() - NEARBY_RADIUS,
                        event.player.getX() + NEARBY_RADIUS, event.player.getY() + NEARBY_RADIUS, event.player.getZ() + NEARBY_RADIUS
                )
        );

        int targetTotal = 5; // spawns in 5 phantoms around the player
        int missing = targetTotal - nearby.size();
        if (missing <= 0) return;

        // 65% chance a phantom spawns this tick
        if (level.random.nextFloat() >= 0.65f) return;

        // Spawn 2 per check until we reach 5
        int toSpawnNow = Math.min(missing, 2);

        // Checking how many Phantoms have spawned
        int currentNear = 0;
        for (Phantom p : nearby) {
            double dx = p.getX() - event.player.getX();
            double dz = p.getZ() - event.player.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d <= 24) currentNear++;
        }
        int targetNear = 2;
        int nearMissing = Math.max(0, targetNear - currentNear);

        for (int i = 0; i < toSpawnNow; i++) {
            Phantom phantom = EntityType.PHANTOM.create(level);
            if (phantom == null) break;

            boolean spawnNear = (nearMissing > 0);
            double distance = spawnNear
                    ? (12 + level.random.nextInt(13))   // 12–24 blocks
                    : (36 + level.random.nextInt(25));  // 36–60 blocks
            double angle = level.random.nextDouble() * Math.PI * 2.0;

            double x = event.player.getX() + Math.cos(angle) * distance;
            double z = event.player.getZ() + Math.sin(angle) * distance;
            double y = event.player.getY() + 15 + level.random.nextInt(10);

            phantom.moveTo(x, y, z, level.random.nextFloat() * 360f, 0f);

            // Safety: skip if spawned right above land
            if (!isOverVoid(level, BlockPos.containing(x, y, z), 10, 32)) continue;

            level.addFreshEntity(phantom);
            nearMissing--;
        }
    }

    private static boolean isOverVoid(Level level, BlockPos pos, int r, int down) {
        // 4 compass directions
        BlockPos[] samples = new BlockPos[]{
                pos,
                pos.offset(r, 0, 0),
                pos.offset(-r, 0, 0),
                pos.offset(0, 0, r),
                pos.offset(0, 0, -r)
        };
        for (BlockPos base : samples) {
            BlockPos cursor = base;
            for (int i = 0; i < down; i++) {
                cursor = cursor.below();
                if (!level.getBlockState(cursor).isAir()) {
                    return false; // found terrain nearby
                }
            }
        }
        return true; // all sample columns were air
    }
}
